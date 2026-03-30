package com.pcloudy.bankingg

// ── authentication ──
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String, val username: String)
data class RegisterRequest(val username: String, val password: String, val name: String)
data class RegisterResponse(val success: Boolean, val token: String, val user: RegisteredUser)
data class RegisteredUser(val name: String, val accountNo: String, val username: String)

// ── balance / wallet ──
data class BalanceResponse(val balance: Double)
data class WalletBalanceResponse(val success: Boolean, val walletBalance: Double, val currency: String)
data class TopupRequest(val amount: Double)

// ── UPI payment ──
data class UpiRequest(val upiId: String, val amount: Double)
data class UpiResponse(val status: String, val message: String?, val newBalance: Double?)

// ── transactions ──
data class Pagination(val page: Int, val limit: Int, val total: Int, val totalPages: Int, val hasNext: Boolean)
data class TransactionPage(
    val success: Boolean,
    val transactions: List<Transaction>,
    val pagination: Pagination
)

// ── notifications ──
data class NotificationItem(val type: String, val title: String?, val body: String?, val message: String?)
data class NotificationsResponse(val success: Boolean, val notifications: List<NotificationItem>, val unreadCount: Int)