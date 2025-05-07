package com.pcloudy.bankingg

class RechargeRepository {
    private val recharges = mutableListOf<Recharge>()

    fun addRecharge(recharge: Recharge): Result<Recharge> {
        // Validate recharge details
        return try {
            validateRecharge(recharge)
            recharges.add(recharge)
            Result.success(recharge)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun validateRecharge(recharge: Recharge) {
        require(recharge.mobileNumber.length == 10) { "Invalid mobile number" }
        require(recharge.amount > 0) { "Recharge amount must be positive" }
    }

    fun getRechargeHistory(): List<Recharge> = recharges.toList()
}