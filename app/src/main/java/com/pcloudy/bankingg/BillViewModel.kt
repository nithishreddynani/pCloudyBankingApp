package com.pcloudy.bankingg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillViewModel : ViewModel() {
    private val billRepository = BillRepository()

    // State management without sealed classes
    private val _bills = MutableStateFlow<List<Bill>>(emptyList())
    val bills: StateFlow<List<Bill>> = _bills.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _operationResult = MutableStateFlow<OperationResult?>(null)
    val operationResult: StateFlow<OperationResult?> = _operationResult.asStateFlow()

    // Enum to represent operation results
    enum class OperationResult {
        SUCCESS, FAILURE
    }

    // Fetch all bills
    fun fetchBills() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetchedBills = billRepository.getAllBills()
                _bills.value = fetchedBills
                _isLoading.value = false

                // Clear any previous error
                _errorMessage.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Failed to fetch bills: ${e.localizedMessage}"
            }
        }
    }

    // Add a new bill
    fun addBill(bill: Bill) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = billRepository.addBill(bill)
                if (result) {
                    fetchBills()
                    _operationResult.value = OperationResult.SUCCESS
                    _errorMessage.value = null
                } else {
                    _operationResult.value = OperationResult.FAILURE
                    _errorMessage.value = "Failed to add bill"
                }
            } catch (e: Exception) {
                _operationResult.value = OperationResult.FAILURE
                _errorMessage.value = "Error adding bill: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Pay a bill
    fun payBill(bill: Bill) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = billRepository.payBill(bill.id)
                if (result) {
                    fetchBills()
                    _operationResult.value = OperationResult.SUCCESS
                    _errorMessage.value = null
                } else {
                    _operationResult.value = OperationResult.FAILURE
                    _errorMessage.value = "Payment processing failed"
                }
            } catch (e: Exception) {
                _operationResult.value = OperationResult.FAILURE
                _errorMessage.value = "Payment error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Delete a bill
    fun deleteBill(billId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = billRepository.deleteBill(billId)
                if (result) {
                    fetchBills()
                    _operationResult.value = OperationResult.SUCCESS
                    _errorMessage.value = null
                } else {
                    _operationResult.value = OperationResult.FAILURE
                    _errorMessage.value = "Failed to delete bill"
                }
            } catch (e: Exception) {
                _operationResult.value = OperationResult.FAILURE
                _errorMessage.value = "Error deleting bill: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}