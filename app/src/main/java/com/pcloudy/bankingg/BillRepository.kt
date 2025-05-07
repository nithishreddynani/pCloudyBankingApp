package com.pcloudy.bankingg

class BillRepository {
    private val bills = mutableListOf<Bill>()

    fun getAllBills(): List<Bill> = bills.toList()

    fun addBill(bill: Bill): Boolean {
        return bills.add(bill)
    }

    fun payBill(billId: String): Boolean {
        val billIndex = bills.indexOfFirst { it.id == billId }
        return if (billIndex != -1) {
            val existingBill = bills[billIndex]
            bills[billIndex] = existingBill.copy(isPaid = true)
            true
        } else {
            false
        }
    }

    fun deleteBill(billId: String): Boolean {
        return bills.removeIf { it.id == billId }
    }
}