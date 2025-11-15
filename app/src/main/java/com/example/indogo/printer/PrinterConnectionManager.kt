package com.example.indogo.printer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import com.example.indogo.models.ConnectionType
import com.example.indogo.models.PrinterConfig
import com.example.indogo.models.PrinterStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.UUID

/**
 * Manages printer connections (WiFi and Bluetooth)
 * Handles connection lifecycle and data transmission
 */
class PrinterConnectionManager(private val context: Context) {

    companion object {
        private const val TAG = "PrinterConnectionMgr"
        // Standard Serial Port Profile UUID for Bluetooth printers
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val WIFI_TIMEOUT = 10000 // 10 seconds - increased for WiFi stability
    }

    // Connection state
    private var currentStatus: PrinterStatus = PrinterStatus.DISCONNECTED
    private var outputStream: OutputStream? = null
    private var wifiSocket: Socket? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var currentConnectionType: ConnectionType? = null

    // Status callback
    private var statusCallback: ((PrinterStatus) -> Unit)? = null

    /**
     * Set status change callback
     */
    fun setStatusCallback(callback: (PrinterStatus) -> Unit) {
        statusCallback = callback
    }

    /**
     * Update and notify status
     */
    private fun updateStatus(status: PrinterStatus) {
        currentStatus = status
        statusCallback?.invoke(status)
        Log.d(TAG, "Printer status: $status")
    }

    /**
     * Get current printer status
     */
    fun getStatus(): PrinterStatus = currentStatus

    /**
     * Check if printer is connected
     */
    fun isConnected(): Boolean = currentStatus == PrinterStatus.CONNECTED

    /**
     * Connect to printer based on configuration
     */
    suspend fun connect(config: PrinterConfig): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            disconnect() // Disconnect any existing connection

            updateStatus(PrinterStatus.CONNECTING)

            when (config.connectionType) {
                ConnectionType.WIFI -> connectWiFi(config)
                ConnectionType.BLUETOOTH -> connectBluetooth(config)
                ConnectionType.USB -> Result.failure(Exception("USB not yet supported"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed", e)
            updateStatus(PrinterStatus.ERROR)
            Result.failure(e)
        }
    }

    /**
     * Connect via WiFi
     */
    private suspend fun connectWiFi(config: PrinterConfig): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Connecting to WiFi printer at ${config.wifiIpAddress}:${config.wifiPort}")

            val socket = Socket()

            // Configure socket options for better reliability
            socket.soTimeout = WIFI_TIMEOUT
            socket.keepAlive = true
            socket.tcpNoDelay = true  // Disable Nagle's algorithm for real-time data

            socket.connect(
                InetSocketAddress(config.wifiIpAddress, config.wifiPort),
                WIFI_TIMEOUT
            )

            if (socket.isConnected) {
                wifiSocket = socket
                outputStream = socket.getOutputStream()
                currentConnectionType = ConnectionType.WIFI
                updateStatus(PrinterStatus.CONNECTED)
                Log.d(TAG, "WiFi connection successful to ${config.wifiIpAddress}:${config.wifiPort}")
                Result.success(Unit)
            } else {
                throw IOException("Failed to connect to WiFi printer at ${config.wifiIpAddress}:${config.wifiPort}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "WiFi connection failed to ${config.wifiIpAddress}:${config.wifiPort}", e)
            Log.e(TAG, "Error details: ${e.message}")
            updateStatus(PrinterStatus.ERROR)
            Result.failure(e)
        }
    }

    /**
     * Connect via Bluetooth
     */
    private suspend fun connectBluetooth(config: PrinterConfig): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Connecting to Bluetooth printer: ${config.bluetoothDeviceName}")

            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                ?: throw Exception("Bluetooth not available on this device")

            if (!bluetoothAdapter.isEnabled) {
                throw Exception("Bluetooth is not enabled")
            }

            // Find the device by address or name
            val device = if (config.bluetoothDeviceAddress.isNotEmpty()) {
                bluetoothAdapter.getRemoteDevice(config.bluetoothDeviceAddress)
            } else {
                bluetoothAdapter.bondedDevices.find {
                    it.name == config.bluetoothDeviceName
                } ?: throw Exception("Bluetooth device not found: ${config.bluetoothDeviceName}")
            }

            // Create socket and connect
            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)

            // Cancel discovery to improve connection speed
            bluetoothAdapter.cancelDiscovery()

            socket.connect()

            if (socket.isConnected) {
                bluetoothSocket = socket
                outputStream = socket.outputStream
                currentConnectionType = ConnectionType.BLUETOOTH
                updateStatus(PrinterStatus.CONNECTED)
                Log.d(TAG, "Bluetooth connection successful")
                Result.success(Unit)
            } else {
                throw IOException("Failed to connect to Bluetooth printer")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Bluetooth connection failed", e)
            updateStatus(PrinterStatus.ERROR)
            Result.failure(e)
        }
    }

    /**
     * Send data to printer
     */
    suspend fun sendData(data: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isConnected()) {
                throw Exception("Printer is not connected")
            }

            val stream = outputStream ?: throw Exception("Output stream is null")

            updateStatus(PrinterStatus.PRINTING)

            stream.write(data)
            stream.flush()

            // Give printer time to process
            Thread.sleep(100)

            updateStatus(PrinterStatus.CONNECTED)
            Log.d(TAG, "Data sent successfully: ${data.size} bytes")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send data", e)
            updateStatus(PrinterStatus.ERROR)
            Result.failure(e)
        }
    }

    /**
     * Print data (convenience method)
     */
    suspend fun print(data: ByteArray, config: PrinterConfig): Result<Unit> {
        return try {
            // Connect if not already connected
            if (!isConnected()) {
                val connectResult = connect(config)
                if (connectResult.isFailure) {
                    return connectResult
                }
            }

            // Send data
            sendData(data)
        } catch (e: Exception) {
            Log.e(TAG, "Print failed", e)
            Result.failure(e)
        }
    }

    /**
     * Disconnect from printer
     */
    fun disconnect() {
        try {
            outputStream?.close()
            wifiSocket?.close()
            bluetoothSocket?.close()

            outputStream = null
            wifiSocket = null
            bluetoothSocket = null
            currentConnectionType = null

            updateStatus(PrinterStatus.DISCONNECTED)
            Log.d(TAG, "Disconnected from printer")
        } catch (e: Exception) {
            Log.e(TAG, "Error during disconnect", e)
        }
    }

    /**
     * Test printer connection
     */
    suspend fun testConnection(config: PrinterConfig): Result<Boolean> {
        return try {
            val connectResult = connect(config)
            if (connectResult.isSuccess) {
                // Send a simple test command
                val testData = ESCIP05Commands.initPrinter()
                sendData(testData)
                disconnect()
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed", e)
            Result.success(false)
        }
    }

    /**
     * Get list of paired Bluetooth devices
     */
    fun getPairedBluetoothDevices(): List<BluetoothDevice> {
        return try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter?.isEnabled == true) {
                bluetoothAdapter.bondedDevices.toList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get paired Bluetooth devices", e)
            emptyList()
        }
    }

    /**
     * Check if Bluetooth is available and enabled
     */
    fun isBluetoothAvailable(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        disconnect()
        statusCallback = null
    }
}
