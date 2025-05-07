package com.pcloudy.bankingg

import java.util.UUID
import java.util.Date

data class Recharge(
    val id: String = UUID.randomUUID().toString(),
    val mobileNumber: String,
    val operator: String,
    val amount: Double,
    val circleType: String,
    val rechargeType: RechargeType,
    val date: Date = Date(),
    val status: RechargeStatus = RechargeStatus.PENDING
)

enum class RechargeType {
    PREPAID,
    POSTPAID
}

enum class RechargeStatus {
    PENDING,
    SUCCESSFUL,
    FAILED
}