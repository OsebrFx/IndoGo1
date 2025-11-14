package com.example.indogo

import androidx.annotation.DrawableRes

data class Flight(
    val airlineName: String,
    @DrawableRes val airlineLogo: Int,
    val departureCode: String,
    val departureTime: String,
    val arrivalCode: String,
    val arrivalTime: String,
    val duration: String,
    val price: Int,
    val hasFreeMeal: Boolean = false,
    val promoCode: String = "",
    val promoBackgroundColor: String = "#E8F5E9"
)