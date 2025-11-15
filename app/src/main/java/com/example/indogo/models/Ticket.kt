package com.example.indogo.models

import com.example.indogo.Flight
import java.util.UUID

/**
 * Ticket data model for thermal printing
 * Extends Flight information with additional booking and passenger details
 */
data class Ticket(
    // Flight information (inherited from Flight model)
    val flight: Flight,

    // Ticket identification
    val ticketId: String = UUID.randomUUID().toString(),
    val bookingReference: String,
    val pnr: String, // Passenger Name Record

    // Passenger information
    val passengerName: String,
    val passengerGender: String = "Mr", // Mr, Ms, Mrs
    val passengerPhone: String = "",
    val passengerEmail: String = "",

    // Booking details
    val bookingDate: String,
    val travelDate: String,
    val flightNumber: String,

    // Seat information
    val seatNumber: String,
    val className: String, // Economy, Business, First
    val terminal: String = "T1",
    val gate: String = "A1",

    // Boarding details
    val boardingTime: String,
    val checkInStatus: String = "Not Checked In", // Not Checked In, Checked In, Boarded

    // Baggage
    val baggageAllowance: String = "15 KG",
    val cabinBaggage: String = "7 KG",

    // Payment
    val totalAmount: Int,
    val currency: String = "INR",
    val paymentStatus: String = "PAID",

    // Additional information
    val specialRequests: String = "",
    val notes: String = "",

    // QR Code data (will be generated from ticket details)
    val qrCodeData: String = generateQRCodeData(
        ticketId, bookingReference, passengerName, flightNumber, seatNumber
    )
) {
    companion object {
        /**
         * Generates QR code data string from ticket information
         */
        private fun generateQRCodeData(
            ticketId: String,
            bookingRef: String,
            passengerName: String,
            flightNo: String,
            seat: String
        ): String {
            return "TICKET:$ticketId|BOOKING:$bookingRef|PASSENGER:$passengerName|FLIGHT:$flightNo|SEAT:$seat"
        }

        /**
         * Create a sample ticket for testing
         */
        fun createSampleTicket(flight: Flight): Ticket {
            return Ticket(
                flight = flight,
                bookingReference = "IND${System.currentTimeMillis().toString().takeLast(6)}",
                pnr = "PNR${System.currentTimeMillis().toString().takeLast(8)}",
                passengerName = "JOHN DOE",
                passengerGender = "Mr",
                passengerPhone = "+91 9876543210",
                passengerEmail = "john.doe@example.com",
                bookingDate = "14 Nov 2025",
                travelDate = "15 Nov 2025",
                flightNumber = "6E-${(1000..9999).random()}",
                seatNumber = "${('A'..'F').random()}${(1..30).random()}",
                className = "Economy",
                terminal = "T${(1..3).random()}",
                gate = "${('A'..'E').random()}${(1..15).random()}",
                boardingTime = flight.departureTime,
                totalAmount = flight.price,
                currency = "INR",
                paymentStatus = "PAID",
                baggageAllowance = "15 KG",
                cabinBaggage = "7 KG"
            )
        }
    }
}
