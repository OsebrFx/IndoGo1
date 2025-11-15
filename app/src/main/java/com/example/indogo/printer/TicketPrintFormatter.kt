package com.example.indogo.printer

import com.example.indogo.models.PaperWidth
import com.example.indogo.models.Ticket
import com.example.indogo.printer.ESCIP05Commands as CMD

/**
 * Barcode types supported by the printer
 */
enum class BarcodeType(val code: Int) {
    UPC_A(65),      // UPC-A
    UPC_E(66),      // UPC-E
    EAN13(67),      // EAN13 (JAN13)
    EAN8(68),       // EAN8 (JAN8)
    CODE39(69),     // CODE39
    ITF(70),        // ITF (Interleaved 2 of 5)
    CODABAR(71),    // CODABAR
    CODE93(72),     // CODE93
    CODE128(73)     // CODE128 (default)
}

/**
 * Formats ticket data for thermal printing using ESCIP05 commands
 */
class TicketPrintFormatter(
    private val paperWidth: PaperWidth = PaperWidth.WIDTH_80MM
) {

    // Character width based on paper size
    private val charWidth: Int = when (paperWidth) {
        PaperWidth.WIDTH_58MM -> 32
        PaperWidth.WIDTH_80MM -> 48
    }

    /**
     * Format complete ticket for printing with barcode
     * Optimized compact professional design like real airline boarding passes
     */
    fun formatTicket(ticket: Ticket): ByteArray {
        val commands = mutableListOf<ByteArray>()

        // Initialize printer
        commands.add(CMD.initPrinter())
        commands.add(CMD.setPrintDensity(15))

        // Compact header with horizontal title
        commands.add(formatCompactHeader(ticket))

        // Essential flight and passenger information in compact layout
        commands.add(formatEssentialInfo(ticket))

        // Prominent barcode
        commands.add(formatProminentBarcode(ticket))

        // Minimal footer
        commands.add(formatMinimalFooter())

        // Feed and cut
        commands.add(CMD.feedAndCut())

        return commands.reduce { acc, bytes -> acc + bytes }
    }

    /**
     * Format complete ticket for printing with QR code (alternative)
     */
    fun formatTicketWithQRCode(ticket: Ticket): ByteArray {
        val commands = mutableListOf<ByteArray>()

        // Initialize printer
        commands.add(CMD.initPrinter())
        commands.add(CMD.setPrintDensity(15))

        // Header
        commands.add(formatHeader(ticket))

        // Flight information
        commands.add(formatFlightInfo(ticket))

        // Passenger information
        commands.add(formatPassengerInfo(ticket))

        // Boarding information
        commands.add(formatBoardingInfo(ticket))

        // Baggage information
        commands.add(formatBaggageInfo(ticket))

        // Payment information
        commands.add(formatPaymentInfo(ticket))

        // QR Code instead of barcode
        commands.add(formatQRCode(ticket))

        // Footer
        commands.add(formatFooter(ticket))

        // Feed and cut
        commands.add(CMD.feedAndCut())

        return commands.reduce { acc, bytes -> acc + bytes }
    }

    /**
     * Format header section
     */
    private fun formatHeader(ticket: Ticket): ByteArray {
        val commands = mutableListOf<ByteArray>()

        // Professional Logo ASCII Art - Airplane
        commands.add(CMD.alignCenter())
        commands.add(CMD.emptyLine())
        commands.add(CMD.printLine("        __|__        "))
        commands.add(CMD.printLine("  --@--@--(_)--@--@--"))
        commands.add(CMD.printLine("                     "))
        commands.add(CMD.printLine("  * INDOGO AIRLINES *"))
        commands.add(CMD.emptyLine())

        // Decorative border
        commands.add(CMD.printLine("====================="))
        commands.add(CMD.emptyLine())

        // Airline name (large, centered)
        commands.add(CMD.setBold(true))
        commands.add(CMD.textSizeDouble())
        commands.add(CMD.printLine("INDOGO"))
        commands.add(CMD.textSizeNormal())
        commands.add(CMD.setBold(false))

        // Boarding pass title
        commands.add(CMD.emptyLine())
        commands.add(CMD.setBold(true))
        commands.add(CMD.printLine("BOARDING PASS"))
        commands.add(CMD.setBold(false))
        commands.add(CMD.alignLeft())

        // Divider
        commands.add(CMD.emptyLine())
        commands.add(CMD.printDoubleDivider(charWidth))

        return commands.reduce { acc, bytes -> acc + bytes }
    }

    /**
     * Format flight information section
     */
    private fun formatFlightInfo(ticket: Ticket): ByteArray {
        val commands = mutableListOf<ByteArray>()

        commands.add(CMD.emptyLine())

        // Flight route (large, centered)
        commands.add(CMD.alignCenter())
        commands.add(CMD.setBold(true))
        commands.add(CMD.textSizeTriple())
        commands.add(CMD.printLine(ticket.flight.departureCode))

        commands.add(CMD.textSizeNormal())
        commands.add(CMD.printLine("TO"))

        commands.add(CMD.textSizeTriple())
        commands.add(CMD.printLine(ticket.flight.arrivalCode))
        commands.add(CMD.textSizeNormal())
        commands.add(CMD.setBold(false))
        commands.add(CMD.alignLeft())

        commands.add(CMD.emptyLine())

        // Flight details
        commands.add(formatKeyValue("Flight", ticket.flightNumber))
        commands.add(formatKeyValue("Date", ticket.travelDate))
        commands.add(formatKeyValue("Departure", ticket.flight.departureTime))
        commands.add(formatKeyValue("Arrival", ticket.flight.arrivalTime))
        commands.add(formatKeyValue("Duration", ticket.flight.duration))

        commands.add(CMD.printDivider(charWidth))

        return commands.reduce { acc, bytes -> acc + bytes }
    }

    /**
     * Format passenger information section
     */
    private fun formatPassengerInfo(ticket: Ticket): ByteArray {
        val commands = mutableListOf<ByteArray>()

        commands.add(CMD.emptyLine())
        commands.add(CMD.setBold(true))
        commands.add(CMD.printLine("PASSENGER DETAILS"))
        commands.add(CMD.setBold(false))

        commands.add(formatKeyValue("Name", ticket.passengerName))
        commands.add(formatKeyValue("PNR", ticket.pnr))
        commands.add(formatKeyValue("Booking Ref", ticket.bookingReference))

        if (ticket.passengerPhone.isNotEmpty()) {
            commands.add(formatKeyValue("Phone", ticket.passengerPhone))
        }

        commands.add(CMD.printDivider(charWidth))

        return commands.reduce { acc, bytes -> acc + bytes }
    }

    /**
     * Format boarding information section
     */
    private fun formatBoardingInfo(ticket: Ticket): ByteArray {
        val commands = mutableListOf<ByteArray>()

        commands.add(CMD.emptyLine())
        commands.add(CMD.setBold(true))
        commands.add(CMD.printLine("BOARDING INFORMATION"))
        commands.add(CMD.setBold(false))

        commands.add(formatKeyValue("Seat", ticket.seatNumber))
        commands.add(formatKeyValue("Class", ticket.className))
        commands.add(formatKeyValue("Terminal", ticket.terminal))
        commands.add(formatKeyValue("Gate", ticket.gate))
        commands.add(formatKeyValue("Boarding Time", ticket.boardingTime))

        commands.add(CMD.printDivider(charWidth))

        return commands.reduce { acc, bytes -> acc + bytes }
    }

    /**
     * Format baggage information section
     */
    private fun formatBaggageInfo(ticket: Ticket): ByteArray {
        val commands = mutableListOf<ByteArray>()

        commands.add(CMD.emptyLine())
        commands.add(CMD.setBold(true))
        commands.add(CMD.printLine("BAGGAGE ALLOWANCE"))
        commands.add(CMD.setBold(false))

        commands.add(formatKeyValue("Check-in", ticket.baggageAllowance))
        commands.add(formatKeyValue("Cabin", ticket.cabinBaggage))

        commands.add(CMD.printDivider(charWidth))

        return commands.reduce { acc, bytes -> acc + bytes }
    }

    /**
     * Format payment information section
     */
    private fun formatPaymentInfo(ticket: Ticket): ByteArray {
        val commands = mutableListOf<ByteArray>()

        commands.add(CMD.emptyLine())
        commands.add(CMD.setBold(true))
        commands.add(CMD.printLine("PAYMENT DETAILS"))
        commands.add(CMD.setBold(false))

        val amount = "${ticket.currency} ${ticket.totalAmount}"
        commands.add(formatKeyValue("Total Amount", amount))
        commands.add(formatKeyValue("Status", ticket.paymentStatus))
        commands.add(formatKeyValue("Booked On", ticket.bookingDate))

        commands.add(CMD.printDivider(charWidth))

        return commands.reduce { acc, bytes -> acc + bytes }
    }

    /**
     * Automatically detect the best barcode type for the data
     */
    private fun detectBarcodeType(data: String): BarcodeType {
        return when {
            // UPC-A: 12 digits exactly
            data.matches(Regex("^\\d{12}$")) -> BarcodeType.UPC_A

            // UPC-E: 8 digits exactly
            data.matches(Regex("^\\d{8}$")) -> BarcodeType.UPC_E

            // EAN13: 13 digits exactly
            data.matches(Regex("^\\d{13}$")) -> BarcodeType.EAN13

            // EAN8: 8 digits and starts with specific prefixes
            data.matches(Regex("^\\d{8}$")) && data.startsWith("0") -> BarcodeType.EAN8

            // CODE39: Alphanumeric with special chars
            data.matches(Regex("^[A-Z0-9 \\-.$/%+]+$")) -> BarcodeType.CODE39

            // ITF: Even number of digits
            data.matches(Regex("^\\d+$")) && data.length % 2 == 0 -> BarcodeType.ITF

            // CODABAR: Starts with A,B,C,D and ends with same
            data.matches(Regex("^[A-D][0-9\\-$:/.+]+[A-D]$")) -> BarcodeType.CODABAR

            // CODE93: Alphanumeric
            data.matches(Regex("^[A-Z0-9]+$")) -> BarcodeType.CODE93

            // CODE128: Default for everything else (most versatile)
            else -> BarcodeType.CODE128
        }
    }

    /**
     * Format Barcode section with automatic type detection
     */
    private fun formatBarcode(ticket: Ticket): ByteArray {
        val commands = mutableListOf<ByteArray>()

        commands.add(CMD.emptyLine())
        commands.add(CMD.alignCenter())

        // Barcode - Using booking reference as barcode data
        val barcodeData = ticket.bookingReference
        val barcodeType = detectBarcodeType(barcodeData)

        // Print barcode with detected type
        commands.add(CMD.printBarcode(barcodeData, type = barcodeType.code, height = 100))

        commands.add(CMD.emptyLine())
        commands.add(CMD.setBold(true))
        commands.add(CMD.printLine("SCAN BARCODE"))
        commands.add(CMD.setBold(false))
        commands.add(CMD.printLine(barcodeData))

        // Show barcode type for reference
        commands.add(CMD.printLine("Type: ${barcodeType.name}"))

        commands.add(CMD.alignLeft())
        commands.add(CMD.printDivider(charWidth))

        return commands.reduce { acc, bytes -> acc + bytes }
    }

    /**
     * Format QR Code section (alternative to barcode)
     */
    private fun formatQRCode(ticket: Ticket): ByteArray {
        val commands = mutableListOf<ByteArray>()

        commands.add(CMD.emptyLine())
        commands.add(CMD.alignCenter())

        // QR Code with ticket data
        commands.add(CMD.printQRCode(ticket.qrCodeData, size = 6))

        commands.add(CMD.emptyLine())
        commands.add(CMD.setBold(true))
        commands.add(CMD.printLine("SCAN QR CODE"))
        commands.add(CMD.setBold(false))
        commands.add(CMD.printLine(ticket.bookingReference))

        commands.add(CMD.alignLeft())
        commands.add(CMD.printDivider(charWidth))

        return commands.reduce { acc, bytes -> acc + bytes }
    }

    /**
     * Format footer section
     */
    private fun formatFooter(ticket: Ticket): ByteArray {
        val commands = mutableListOf<ByteArray>()

        commands.add(CMD.emptyLine())
        commands.add(CMD.alignCenter())

        // Important notices
        commands.add(CMD.textSizeNormal())
        commands.add(CMD.printLine("IMPORTANT NOTICES"))
        commands.add(CMD.emptyLine())

        commands.add(CMD.alignLeft())
        commands.add(CMD.printText("* Report at gate 45 mins before"))
        commands.add(CMD.emptyLine())
        commands.add(CMD.printText("* Carry valid photo ID proof"))
        commands.add(CMD.emptyLine())
        commands.add(CMD.printText("* Check-in closes 60 mins prior"))
        commands.add(CMD.emptyLine())

        commands.add(CMD.emptyLine())

        // Thank you message
        commands.add(CMD.alignCenter())
        commands.add(CMD.setBold(true))
        commands.add(CMD.printLine("Thank you for choosing IndoGo!"))
        commands.add(CMD.setBold(false))

        commands.add(CMD.emptyLine())
        commands.add(CMD.printLine("Have a pleasant journey!"))
        commands.add(CMD.alignLeft())

        commands.add(CMD.emptyLines(2))

        return commands.reduce { acc, bytes -> acc + bytes }
    }

    /**
     * Helper to format key-value pairs
     */
    private fun formatKeyValue(key: String, value: String): ByteArray {
        val padding = 20 // Adjust based on paper width
        val paddedKey = key.padEnd(padding, ' ')
        val line = "$paddedKey: $value"
        return CMD.printLine(line)
    }

    /**
     * Format compact header with horizontal title - Professional design
     */
    private fun formatCompactHeader(ticket: Ticket): ByteArray {
        val commands = mutableListOf<ByteArray>()

        commands.add(CMD.emptyLine())

        // Compact airline logo
        commands.add(CMD.alignCenter())
        commands.add(CMD.printLine("      __|__      "))
        commands.add(CMD.printLine("  --@-(_)-@--    "))
        commands.add(CMD.emptyLine())

        // Airline name and horizontal boarding pass title
        commands.add(CMD.setBold(true))
        commands.add(CMD.textSizeDouble())
        commands.add(CMD.printLine("INDOGO"))
        commands.add(CMD.textSizeNormal())
        commands.add(CMD.emptyLine())
        commands.add(CMD.printLine("--- BOARDING PASS ---"))
        commands.add(CMD.setBold(false))
        commands.add(CMD.alignLeft())

        commands.add(CMD.emptyLine())
        commands.add(CMD.printDivider(charWidth))

        return commands.reduce { acc, bytes -> acc + bytes }
    }

    /**
     * Format essential information in compact layout - Only important details
     */
    private fun formatEssentialInfo(ticket: Ticket): ByteArray {
        val commands = mutableListOf<ByteArray>()

        commands.add(CMD.emptyLine())

        // Route - Large and prominent
        commands.add(CMD.alignCenter())
        commands.add(CMD.setBold(true))
        commands.add(CMD.textSizeTriple())
        commands.add(CMD.printLine("${ticket.flight.departureCode} → ${ticket.flight.arrivalCode}"))
        commands.add(CMD.textSizeNormal())
        commands.add(CMD.setBold(false))
        commands.add(CMD.alignLeft())

        commands.add(CMD.emptyLine())

        // Passenger name - Prominent
        commands.add(CMD.setBold(true))
        commands.add(CMD.textSizeWide())
        commands.add(CMD.printLine("PASSENGER"))
        commands.add(CMD.textSizeNormal())
        commands.add(CMD.setBold(false))
        commands.add(CMD.printLine(ticket.passengerName.uppercase()))

        commands.add(CMD.emptyLine())

        // Flight details in compact 2-column layout
        commands.add(CMD.printLine("Flight: ${ticket.flightNumber}    Date: ${ticket.travelDate}"))
        commands.add(CMD.printLine("Depart: ${ticket.flight.departureTime}  Class: ${ticket.className}"))

        commands.add(CMD.emptyLine())

        // Boarding information - Critical details
        commands.add(CMD.setBold(true))
        commands.add(CMD.printLine("BOARDING INFO"))
        commands.add(CMD.setBold(false))
        commands.add(CMD.printLine("Seat: ${ticket.seatNumber}      Gate: ${ticket.gate}"))
        commands.add(CMD.printLine("Terminal: ${ticket.terminal}"))
        commands.add(CMD.printLine("Board: ${ticket.boardingTime}"))

        commands.add(CMD.emptyLine())

        // PNR and Booking Reference
        commands.add(CMD.printLine("PNR: ${ticket.pnr}"))
        commands.add(CMD.printLine("Ref: ${ticket.bookingReference}"))

        commands.add(CMD.emptyLine())
        commands.add(CMD.printDivider(charWidth))

        return commands.reduce { acc, bytes -> acc + bytes }
    }

    /**
     * Format prominent barcode - Large and clearly visible
     */
    private fun formatProminentBarcode(ticket: Ticket): ByteArray {
        val commands = mutableListOf<ByteArray>()

        commands.add(CMD.emptyLine())
        commands.add(CMD.alignCenter())

        // Large barcode with auto-detection
        val barcodeData = ticket.bookingReference
        val barcodeType = detectBarcodeType(barcodeData)

        // Larger barcode for better scanning
        commands.add(CMD.printBarcode(barcodeData, type = barcodeType.code, height = 120))

        commands.add(CMD.emptyLine())
        commands.add(CMD.setBold(true))
        commands.add(CMD.textSizeWide())
        commands.add(CMD.printLine(barcodeData))
        commands.add(CMD.textSizeNormal())
        commands.add(CMD.setBold(false))

        commands.add(CMD.alignLeft())
        commands.add(CMD.emptyLine())
        commands.add(CMD.printDivider(charWidth))

        return commands.reduce { acc, bytes -> acc + bytes }
    }

    /**
     * Format minimal footer - Just airline branding
     */
    private fun formatMinimalFooter(): ByteArray {
        val commands = mutableListOf<ByteArray>()

        commands.add(CMD.emptyLine())
        commands.add(CMD.alignCenter())

        commands.add(CMD.printLine("IndoGo Airlines"))
        commands.add(CMD.printLine("Have a pleasant journey!"))

        commands.add(CMD.alignLeft())
        commands.add(CMD.emptyLine())

        return commands.reduce { acc, bytes -> acc + bytes }
    }

    /**
     * Format a simple receipt (minimal version)
     */
    fun formatSimpleReceipt(ticket: Ticket): ByteArray {
        val commands = mutableListOf<ByteArray>()

        commands.add(CMD.initPrinter())
        commands.add(CMD.alignCenter())

        // Compact professional logo
        commands.add(CMD.emptyLine())
        commands.add(CMD.printLine("    __|__    "))
        commands.add(CMD.printLine(" --@-(_)-@-- "))
        commands.add(CMD.printLine("   INDOGO    "))
        commands.add(CMD.printLine("============="))
        commands.add(CMD.emptyLine())

        commands.add(CMD.setBold(true))
        commands.add(CMD.textSizeDouble())
        commands.add(CMD.printLine("INDOGO"))
        commands.add(CMD.textSizeNormal())
        commands.add(CMD.printLine("BOARDING PASS"))
        commands.add(CMD.setBold(false))
        commands.add(CMD.emptyLine())

        commands.add(CMD.alignLeft())
        commands.add(CMD.printLine("${ticket.flight.departureCode} -> ${ticket.flight.arrivalCode}"))
        commands.add(CMD.printLine("Flight: ${ticket.flightNumber}"))
        commands.add(CMD.printLine("Passenger: ${ticket.passengerName}"))
        commands.add(CMD.printLine("Seat: ${ticket.seatNumber}"))
        commands.add(CMD.printLine("Gate: ${ticket.gate}"))
        commands.add(CMD.emptyLine())

        commands.add(CMD.alignCenter())
        // Barcode with auto-detection
        val barcodeType = detectBarcodeType(ticket.bookingReference)
        commands.add(CMD.printBarcode(ticket.bookingReference, type = barcodeType.code, height = 80))
        commands.add(CMD.emptyLine())
        commands.add(CMD.printLine(ticket.bookingReference))

        commands.add(CMD.feedAndCut())

        return commands.reduce { acc, bytes -> acc + bytes }
    }

    /**
     * Test print to check printer connectivity
     */
    fun formatTestPrint(): ByteArray {
        val commands = mutableListOf<ByteArray>()

        commands.add(CMD.initPrinter())
        commands.add(CMD.alignCenter())

        // Professional Logo
        commands.add(CMD.emptyLine())
        commands.add(CMD.printLine("        __|__        "))
        commands.add(CMD.printLine("  --@--@--(_)--@--@--"))
        commands.add(CMD.printLine("                     "))
        commands.add(CMD.printLine("  * INDOGO AIRLINES *"))
        commands.add(CMD.emptyLine())
        commands.add(CMD.printLine("====================="))
        commands.add(CMD.emptyLine())

        commands.add(CMD.setBold(true))
        commands.add(CMD.textSizeDouble())
        commands.add(CMD.printLine("TEST PRINT"))
        commands.add(CMD.textSizeNormal())
        commands.add(CMD.setBold(false))
        commands.add(CMD.emptyLine())

        commands.add(CMD.alignLeft())
        commands.add(CMD.printLine("Printer: Gainsha GA-E200I"))
        commands.add(CMD.printLine("Protocol: ESCIP05"))
        commands.add(CMD.printLine("Paper Width: ${paperWidth.widthMm}mm"))
        commands.add(CMD.printLine("Status: OK"))
        commands.add(CMD.emptyLine())

        // Test all barcode types
        commands.add(CMD.setBold(true))
        commands.add(CMD.printLine("Supported Barcode Types:"))
        commands.add(CMD.setBold(false))
        commands.add(CMD.printLine("- UPC-A / UPC-E"))
        commands.add(CMD.printLine("- EAN8 / EAN13 (JAN)"))
        commands.add(CMD.printLine("- CODE39 / CODE93"))
        commands.add(CMD.printLine("- CODE128 / ITF"))
        commands.add(CMD.printLine("- CODABAR"))
        commands.add(CMD.emptyLine())

        commands.add(CMD.alignCenter())
        commands.add(CMD.printLine("Printer is ready!"))
        commands.add(CMD.emptyLines(2))

        commands.add(CMD.feedAndCut())

        return commands.reduce { acc, bytes -> acc + bytes }
    }
}
