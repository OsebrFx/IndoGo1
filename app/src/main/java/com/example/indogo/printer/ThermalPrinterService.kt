package com.example.indogo.printer

import android.content.Context
import android.util.Log
import com.example.indogo.models.*
import com.example.indogo.utils.PrinterPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Main service for thermal printer operations
 * Provides high-level API for printing tickets
 */
class ThermalPrinterService(private val context: Context) {

    companion object {
        private const val TAG = "ThermalPrinterService"

        @Volatile
        private var INSTANCE: ThermalPrinterService? = null

        fun getInstance(context: Context): ThermalPrinterService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThermalPrinterService(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val connectionManager = PrinterConnectionManager(context)
    private val preferences = PrinterPreferences(context)
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Printer status flow
    private val _printerStatus = MutableStateFlow(PrinterStatus.DISCONNECTED)
    val printerStatus: StateFlow<PrinterStatus> = _printerStatus.asStateFlow()

    // Print queue
    private val printQueue = mutableListOf<PrintJob>()
    private var isProcessingQueue = false

    init {
        // Set up status callback
        connectionManager.setStatusCallback { status ->
            _printerStatus.value = status
        }
    }

    /**
     * Get current printer configuration
     */
    fun getConfig(): PrinterConfig {
        return preferences.getPrinterConfig()
    }

    /**
     * Save printer configuration
     */
    fun saveConfig(config: PrinterConfig) {
        preferences.savePrinterConfig(config)
    }

    /**
     * Get current printer status
     */
    fun getCurrentStatus(): PrinterStatus {
        return _printerStatus.value
    }

    /**
     * Check if printer is connected
     */
    fun isConnected(): Boolean {
        return connectionManager.isConnected()
    }

    /**
     * Connect to printer using saved configuration
     */
    suspend fun connect(): Result<Unit> {
        val config = getConfig()
        return connect(config)
    }

    /**
     * Connect to printer with specific configuration
     */
    suspend fun connect(config: PrinterConfig): Result<Unit> {
        return try {
            Log.d(TAG, "Connecting to printer: ${config.printerModel}")
            val result = connectionManager.connect(config)

            if (result.isSuccess) {
                // Save config if connection successful
                saveConfig(config)
                Log.d(TAG, "Connected successfully")
            } else {
                Log.e(TAG, "Connection failed")
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Connection error", e)
            Result.failure(e)
        }
    }

    /**
     * Disconnect from printer
     */
    fun disconnect() {
        connectionManager.disconnect()
        Log.d(TAG, "Disconnected from printer")
    }

    /**
     * Print a ticket
     */
    suspend fun printTicket(
        ticket: Ticket,
        fullFormat: Boolean = true
    ): PrintResult {
        return withContext(Dispatchers.IO) {
            try {
                val config = getConfig()
                val formatter = TicketPrintFormatter(config.paperWidth)

                // Format ticket
                val printData = if (fullFormat) {
                    formatter.formatTicket(ticket)
                } else {
                    formatter.formatSimpleReceipt(ticket)
                }

                // Ensure connected
                if (!isConnected()) {
                    val connectResult = connect(config)
                    if (connectResult.isFailure) {
                        return@withContext PrintResult.Error(
                            "Failed to connect to printer",
                            connectResult.exceptionOrNull() as? Exception
                        )
                    }
                }

                // Send to printer
                val sendResult = connectionManager.sendData(printData)

                if (sendResult.isSuccess) {
                    Log.d(TAG, "Ticket printed successfully")
                    PrintResult.Success
                } else {
                    PrintResult.Error(
                        "Failed to print ticket",
                        sendResult.exceptionOrNull() as? Exception
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Print error", e)
                PrintResult.Error("Print error: ${e.message}", e)
            }
        }
    }

    /**
     * Print multiple copies of a ticket
     */
    suspend fun printTicketMultiple(
        ticket: Ticket,
        copies: Int = 1,
        fullFormat: Boolean = true
    ): PrintResult {
        return withContext(Dispatchers.IO) {
            try {
                var successCount = 0
                var lastError: Exception? = null

                repeat(copies) { index ->
                    Log.d(TAG, "Printing copy ${index + 1} of $copies")
                    val result = printTicket(ticket, fullFormat)

                    when (result) {
                        is PrintResult.Success -> successCount++
                        is PrintResult.Error -> {
                            lastError = result.exception
                            return@withContext result
                        }
                        PrintResult.Cancelled -> return@withContext result
                    }

                    // Small delay between prints
                    if (index < copies - 1) {
                        delay(500)
                    }
                }

                if (successCount == copies) {
                    PrintResult.Success
                } else {
                    PrintResult.Error(
                        "Only $successCount of $copies printed successfully",
                        lastError
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Multiple print error", e)
                PrintResult.Error("Print error: ${e.message}", e)
            }
        }
    }

    /**
     * Print test page
     */
    suspend fun printTestPage(): PrintResult {
        return withContext(Dispatchers.IO) {
            try {
                val config = getConfig()
                val formatter = TicketPrintFormatter(config.paperWidth)
                val testData = formatter.formatTestPrint()

                // Ensure connected
                if (!isConnected()) {
                    val connectResult = connect(config)
                    if (connectResult.isFailure) {
                        return@withContext PrintResult.Error(
                            "Failed to connect to printer",
                            connectResult.exceptionOrNull() as? Exception
                        )
                    }
                }

                // Send to printer
                val sendResult = connectionManager.sendData(testData)

                if (sendResult.isSuccess) {
                    Log.d(TAG, "Test page printed successfully")
                    PrintResult.Success
                } else {
                    PrintResult.Error(
                        "Failed to print test page",
                        sendResult.exceptionOrNull() as? Exception
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Test print error", e)
                PrintResult.Error("Test print error: ${e.message}", e)
            }
        }
    }

    /**
     * Test printer connection
     */
    suspend fun testConnection(config: PrinterConfig? = null): Result<Boolean> {
        val testConfig = config ?: getConfig()
        return connectionManager.testConnection(testConfig)
    }

    /**
     * Add print job to queue
     */
    fun addToQueue(ticket: Ticket, copies: Int = 1) {
        val job = PrintJob(
            jobId = System.currentTimeMillis().toString(),
            ticket = ticket,
            copies = copies
        )
        printQueue.add(job)
        Log.d(TAG, "Added job to queue: ${job.jobId}")

        // Start processing if not already running
        if (!isProcessingQueue) {
            serviceScope.launch {
                processQueue()
            }
        }
    }

    /**
     * Process print queue
     */
    private suspend fun processQueue() {
        isProcessingQueue = true

        while (printQueue.isNotEmpty()) {
            val job = printQueue.removeAt(0)
            Log.d(TAG, "Processing job: ${job.jobId}")

            val result = printTicketMultiple(job.ticket, job.copies)

            when (result) {
                is PrintResult.Success -> {
                    Log.d(TAG, "Job completed: ${job.jobId}")
                }
                is PrintResult.Error -> {
                    Log.e(TAG, "Job failed: ${job.jobId} - ${result.message}")
                }
                PrintResult.Cancelled -> {
                    Log.w(TAG, "Job cancelled: ${job.jobId}")
                    break
                }
            }

            // Small delay between jobs
            delay(1000)
        }

        isProcessingQueue = false
    }

    /**
     * Clear print queue
     */
    fun clearQueue() {
        printQueue.clear()
        Log.d(TAG, "Print queue cleared")
    }

    /**
     * Get queue size
     */
    fun getQueueSize(): Int = printQueue.size

    /**
     * Get paired Bluetooth devices
     */
    fun getPairedBluetoothDevices() = connectionManager.getPairedBluetoothDevices()

    /**
     * Check if Bluetooth is available
     */
    fun isBluetoothAvailable() = connectionManager.isBluetoothAvailable()

    /**
     * Cleanup resources
     */
    fun cleanup() {
        serviceScope.cancel()
        connectionManager.cleanup()
        clearQueue()
    }
}
