package com.example.indogo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var flightAdapter: FlightAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRecyclerView()
        loadFlights()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rvFlightList)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadFlights() {
        val flights = listOf(
            Flight(
                airlineName = "Indigo",
                airlineLogo = R.drawable.ic_airline_placeholder,
                departureCode = "DEL",
                departureTime = "06:30",
                arrivalCode = "BLR",
                arrivalTime = "10:45",
                duration = "04h 15m",
                price = 7319,
                hasFreeMeal = true,
                promoCode = "Use Code : Flyaway60 and get 60% instant cashback",
                promoBackgroundColor = "#E8F5E9"
            ),
            Flight(
                airlineName = "Air India",
                airlineLogo = R.drawable.ic_airline_placeholder,
                departureCode = "DEL",
                departureTime = "09:15",
                arrivalCode = "BLR",
                arrivalTime = "13:30",
                duration = "04h 15m",
                price = 8500,
                hasFreeMeal = true,
                promoCode = "",
                promoBackgroundColor = "#E8F5E9"
            ),
            Flight(
                airlineName = "SpiceJet",
                airlineLogo = R.drawable.ic_airline_placeholder,
                departureCode = "DEL",
                departureTime = "14:00",
                arrivalCode = "BLR",
                arrivalTime = "18:15",
                duration = "04h 15m",
                price = 6999,
                hasFreeMeal = false,
                promoCode = "Use Code : SPICE50 and get 50% instant cashback",
                promoBackgroundColor = "#FFF3E0"
            ),
            Flight(
                airlineName = "Vistara",
                airlineLogo = R.drawable.ic_airline_placeholder,
                departureCode = "DEL",
                departureTime = "18:30",
                arrivalCode = "BLR",
                arrivalTime = "22:45",
                duration = "04h 15m",
                price = 9200,
                hasFreeMeal = true,
                promoCode = "",
                promoBackgroundColor = "#E8F5E9"
            ),
            Flight(
                airlineName = "GoAir",
                airlineLogo = R.drawable.ic_airline_placeholder,
                departureCode = "DEL",
                departureTime = "11:45",
                arrivalCode = "BLR",
                arrivalTime = "16:00",
                duration = "04h 15m",
                price = 7150,
                hasFreeMeal = false,
                promoCode = "Use Code : GOFLY40 and get 40% instant cashback",
                promoBackgroundColor = "#E3F2FD"
            )
        )

        flightAdapter = FlightAdapter(flights) { flight ->
            onFlightClick(flight)
        }
        recyclerView.adapter = flightAdapter
    }


    private fun onFlightClick(flight: Flight) {
        val message = "Selected: ${flight.airlineName}\n" +
                "${flight.departureCode} → ${flight.arrivalCode}\n" +
                "Price: ₹${flight.price}"

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        // TODO: Navigate to booking details or payment screen
        // Example: startActivity(Intent(this, BookingDetailsActivity::class.java).apply {
        //     putExtra("flight", flight)
        // })
    }
}