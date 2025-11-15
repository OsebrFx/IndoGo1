package com.example.indogo.printer

import com.example.indogo.models.PaperWidth
import com.example.indogo.models.Ticket
import com.example.indogo.printer.ESCIP05Commands as CMD

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
     * Format complete ticket for printing
     */
    fun formatTicket(ticket: Ticket): ByteArray {
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

        // Barcode
        commands.add(formatBarcode(ticket))

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

        // Logo ASCII Art
        commands.add(CMD.alignCenter())
        commands.add(CMD.emptyLine())
        commands.add(CMD.printLine("    ___    "))
        commands.add(CMD.printLine("   (o o)   "))
        commands.add(CMD.printLine("  (  V  )  "))
        commands.add(CMD.printLine("  /|||||\\  "))
        commands.add(CMD.printLine(" _|||||||||_"))
        commands.add(CMD.emptyLine())

        // Airline name (large, centered)
        commands.add(CMD.setBold(true))
        commands.add(CMD.textSizeDouble())
        commands.add(CMD.printLine("INDOGO AIRLINES"))
        commands.add(CMD.textSizeNormal())
        commands.add(CMD.setBold(false))

        // Boarding pass title
        commands.add(CMD.emptyLine())
        commands.add(CMD.printLine("BOARDING PASS"))
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
     * Format Barcode section
     */
    private fun formatBarcode(ticket: Ticket): ByteArray {
        val commands = mutableListOf<ByteArray>()

        commands.add(CMD.emptyLine())
        commands.add(CMD.alignCenter())

        // Barcode - Using booking reference as barcode data
        val barcodeData = ticket.bookingReference
        commands.add(CMD.printBarcode(barcodeData, type = 73, height = 100))

        commands.add(CMD.emptyLine())
        commands.add(CMD.setBold(true))
        commands.add(CMD.printLine("SCAN BARCODE"))
        commands.add(CMD.setBold(false))
        commands.add(CMD.printLine(barcodeData))

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
     * Format a simple receipt (minimal version)
     */
    fun formatSimpleReceipt(ticket: Ticket): ByteArray {
        val commands = mutableListOf<ByteArray>()

        commands.add(CMD.initPrinter())
        commands.add(CMD.alignCenter())

        // Simple logo
        commands.add(CMD.printLine("  ___  "))
        commands.add(CMD.printLine(" (o o) "))
        commands.add(CMD.printLine(" _|||_ "))
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
        // Barcode instead of QR code
        commands.add(CMD.printBarcode(ticket.bookingReference, type = 73, height = 80))
        commands.add(CMD.emptyLine())

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

        // Logo
        commands.add(CMD.emptyLine())
        commands.add(CMD.printLine("    ___    "))
        commands.add(CMD.printLine("   (o o)   "))
        commands.add(CMD.printLine("  (  V  )  "))
        commands.add(CMD.printLine("  /|||||\\  "))
        commands.add(CMD.printLine(" _|||||||||_"))
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

        commands.add(CMD.alignCenter())
        commands.add(CMD.printLine("Printer is ready!"))
        commands.add(CMD.emptyLines(2))

        commands.add(CMD.feedAndCut())

        return commands.reduce { acc, bytes -> acc + bytes }
    }
}
