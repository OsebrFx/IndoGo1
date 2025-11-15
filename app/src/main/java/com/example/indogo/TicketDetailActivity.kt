package com.example.indogo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.indogo.databinding.ActivityTicketDetailBinding
import com.example.indogo.models.*
import com.example.indogo.printer.ThermalPrinterService
import com.example.indogo.models.ConnectionType
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch

class TicketDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketDetailBinding
    private lateinit var printerService: ThermalPrinterService
    private lateinit var ticket: Ticket

    companion object {
        const val EXTRA_FLIGHT = "extra_flight"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        printerService = ThermalPrinterService.getInstance(this)

        // Get flight from intent and create ticket
        val flight = intent.getSerializableExtra(EXTRA_FLIGHT) as? Flight
        if (flight == null) {
            Toast.makeText(this, "Error loading ticket", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Create sample ticket from flight
        ticket = Ticket.createSampleTicket(flight)

        setupUI()
        displayTicket()
        observePrinterStatus()
        autoConnectToPrinter()
    }

    private fun setupUI() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Settings button
        binding.btnSettings.setOnClickListener {
            openPrinterSettings()
        }

        // Print FAB
        binding.fabPrint.setOnClickListener {
            showPrintOptions()
        }
    }

    private fun displayTicket() {
        // Display airline name
        binding.tvAirlineName.text = ticket.flight.airlineName

        // Display travel date
        binding.tvTravelDate.text = ticket.travelDate

        // Display flight route
        binding.tvDepartureCode.text = ticket.flight.departureCode
        binding.tvDepartureTime.text = ticket.flight.departureTime
        binding.tvArrivalCode.text = ticket.flight.arrivalCode
        binding.tvArrivalTime.text = ticket.flight.arrivalTime
        binding.tvDuration.text = ticket.flight.duration

        // Display passenger details
        binding.tvPassengerName.text = ticket.passengerName
        binding.tvPNR.text = ticket.pnr

        // Display flight details
        binding.tvFlightNumber.text = ticket.flightNumber
        binding.tvFlightClass.text = ticket.className

        // Display boarding details
        binding.tvSeatNumber.text = ticket.seatNumber
        binding.tvGate.text = ticket.gate
        binding.tvTerminal.text = ticket.terminal

        // Display booking reference
        binding.tvBookingRef.text = ticket.bookingReference

        // Generate and display QR code
        generateQRCode(ticket.qrCodeData)
    }

    private fun generateQRCode(data: String) {
        try {
            val hints = hashMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.MARGIN] = 1

            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(
                data,
                BarcodeFormat.QR_CODE,
                512,
                512,
                hints
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                    )
                }
            }

            binding.ivQRCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPrintOptions() {
        val options = arrayOf(
            getString(R.string.print_full_ticket),
            getString(R.string.print_simple_receipt),
            getString(R.string.print_test_page)
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.print_options)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> printTicket(fullFormat = true)
                    1 -> printTicket(fullFormat = false)
                    2 -> printTestPage()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun printTicket(fullFormat: Boolean) {
        // Check if printer is configured
        if (!printerService.getConfig().let {
            when (it.connectionType) {
                ConnectionType.WIFI -> it.wifiIpAddress.isNotEmpty()
                ConnectionType.BLUETOOTH -> it.bluetoothDeviceAddress.isNotEmpty()
                else -> false
            }
        }) {
            showConfigureDialog()
            return
        }

        // Ask for number of copies
        val copiesOptions = arrayOf("1 copy", "2 copies", "3 copies")
        AlertDialog.Builder(this)
            .setTitle(R.string.number_of_copies)
            .setItems(copiesOptions) { _, which ->
                val copies = which + 1
                confirmAndPrint(copies, fullFormat)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun confirmAndPrint(copies: Int, fullFormat: Boolean) {
        val message = getString(R.string.confirm_print_message, copies)
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_print)
            .setMessage(message)
            .setPositiveButton(R.string.print_ticket) { _, _ ->
                executePrint(copies, fullFormat)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun executePrint(copies: Int, fullFormat: Boolean) {
        binding.fabPrint.isEnabled = false
        binding.fabPrint.text = getString(R.string.printing_ticket)

        lifecycleScope.launch {
            val result = printerService.printTicketMultiple(ticket, copies, fullFormat)

            binding.fabPrint.isEnabled = true
            binding.fabPrint.text = getString(R.string.print_ticket)

            when (result) {
                is PrintResult.Success -> {
                    Toast.makeText(
                        this@TicketDetailActivity,
                        R.string.print_success,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is PrintResult.Error -> {
                    val errorMsg = "${getString(R.string.print_failed)}: ${result.message}"
                    Toast.makeText(
                        this@TicketDetailActivity,
                        errorMsg,
                        Toast.LENGTH_LONG
                    ).show()

                    // Offer to open settings if connection failed
                    if (errorMsg.contains("connect", ignoreCase = true)) {
                        showConfigureDialog()
                    }
                }
                PrintResult.Cancelled -> {
                    Toast.makeText(
                        this@TicketDetailActivity,
                        "Print cancelled",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun printTestPage() {
        binding.fabPrint.isEnabled = false

        lifecycleScope.launch {
            val result = printerService.printTestPage()

            binding.fabPrint.isEnabled = true

            when (result) {
                is PrintResult.Success -> {
                    Toast.makeText(
                        this@TicketDetailActivity,
                        R.string.test_print_success,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is PrintResult.Error -> {
                    Toast.makeText(
                        this@TicketDetailActivity,
                        "${getString(R.string.test_print_failed)}: ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                PrintResult.Cancelled -> {}
            }
        }
    }

    private fun showConfigureDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.printer_settings)
            .setMessage(R.string.error_no_printer_configured)
            .setPositiveButton(R.string.settings) { _, _ ->
                openPrinterSettings()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun openPrinterSettings() {
        val intent = Intent(this, PrinterSettingsActivity::class.java)
        startActivity(intent)
    }

    private fun observePrinterStatus() {
        lifecycleScope.launch {
            printerService.printerStatus.collect { status ->
                updateStatusUI(status)
            }
        }
    }

    private fun autoConnectToPrinter() {
        lifecycleScope.launch {
            // Check if printer is already connected
            if (printerService.isConnected()) {
                return@launch
            }

            // Get printer config
            val config = printerService.getConfig()

            // Only auto-connect for WiFi with configured IP
            if (config.connectionType == ConnectionType.WIFI && config.wifiIpAddress.isNotEmpty()) {
                try {
                    printerService.connect(config)
                } catch (e: Exception) {
                    // Silent fail on auto-connect, user can manually connect later
                }
            }
        }
    }

    private fun updateStatusUI(status: PrinterStatus) {
        val (statusText, statusColor) = when (status) {
            PrinterStatus.DISCONNECTED -> getString(R.string.status_disconnected) to getColor(android.R.color.holo_red_dark)
            PrinterStatus.CONNECTING -> getString(R.string.status_connecting) to getColor(android.R.color.holo_orange_dark)
            PrinterStatus.CONNECTED -> getString(R.string.status_connected) to getColor(android.R.color.holo_green_dark)
            PrinterStatus.PRINTING -> getString(R.string.status_printing) to getColor(android.R.color.holo_blue_dark)
            PrinterStatus.ERROR -> getString(R.string.status_error) to getColor(android.R.color.holo_red_dark)
            PrinterStatus.PAPER_OUT -> getString(R.string.status_paper_out) to getColor(android.R.color.holo_orange_dark)
            PrinterStatus.OFFLINE -> getString(R.string.status_offline) to getColor(android.R.color.holo_red_dark)
        }

        binding.tvPrinterStatusIndicator.text = statusText
        binding.tvPrinterStatusIndicator.setTextColor(statusColor)

        // Show/hide status card based on connection
        binding.cardPrinterStatus.visibility = if (status == PrinterStatus.DISCONNECTED) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't disconnect if we're just rotating the screen
        if (!isChangingConfigurations && printerService.getQueueSize() == 0) {
            // printerService.disconnect() // Keep connection for multiple prints
        }
    }
}
