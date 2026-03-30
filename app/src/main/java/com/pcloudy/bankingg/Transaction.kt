package com.pcloudy.bankingg

data class Transaction(
    val id: String,
    val type: String,           // "debit" or "credit"
    val amount: Double,
    val date: String,           // ISO date string
    val description: String = "",
    val status: String = "SUCCESS",
    val referenceId: String = ""
)