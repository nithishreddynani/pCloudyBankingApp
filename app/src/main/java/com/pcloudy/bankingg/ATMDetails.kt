package com.pcloudy.bankingg

data class ATMDetails(
    val name: String,
    val operator: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Double = 0.0,
    val isOpen24Hours: Boolean = false
)