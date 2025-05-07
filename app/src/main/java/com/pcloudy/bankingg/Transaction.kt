package com.pcloudy.bankingg

import java.util.UUID

//data class Transaction(
//    val id: String,
//    val amount: Double,
//    val type: String, // "bill", "transfer", "deposit", etc.
//    val description: String,
//    val date: Long,
//    val status: String
//)

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val type: String, // Change to support "recharge"
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val status: String = "Completed"
)