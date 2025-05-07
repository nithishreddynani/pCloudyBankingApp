package com.pcloudy.bankingg

import java.util.Date
import java.util.UUID

data class Bill(
    val id: String = UUID.randomUUID().toString(),
    val billerName: String,
    val amount: Double,
    val dueDate: Date,
    val isPaid: Boolean = false,
    val billType: BillType
)

// Bill Type Enum
enum class BillType {
    ELECTRICITY,
    WATER,
    INTERNET,
    PHONE,
    CREDIT_CARD,
    OTHER
}
