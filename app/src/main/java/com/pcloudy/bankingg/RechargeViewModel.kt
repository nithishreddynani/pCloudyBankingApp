package com.pcloudy.bankingg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RechargeViewModel : ViewModel() {
    private val rechargeRepository = RechargeRepository()

    private val _rechargeState = MutableStateFlow<RechargeUiState>(RechargeUiState.Initial)
    val rechargeState: StateFlow<RechargeUiState> = _rechargeState.asStateFlow()

    fun performRecharge(
        mobileNumber: String,
        operator: String,
        amount: Double,
        circleType: String,
        rechargeType: RechargeType
    ) {
        viewModelScope.launch {
            _rechargeState.value = RechargeUiState.Loading

            val recharge = Recharge(
                mobileNumber = mobileNumber,
                operator = operator,
                amount = amount,
                circleType = circleType,
                rechargeType = rechargeType
            )

            try {
                val result = rechargeRepository.addRecharge(recharge)

                _rechargeState.value = if (result.isSuccess) {
                    RechargeUiState.Success(result.getOrNull()!!)
                } else {
                    RechargeUiState.Error(
                        result.exceptionOrNull()?.message ?: "Recharge failed"
                    )
                }
            } catch (e: Exception) {
                _rechargeState.value = RechargeUiState.Error(
                    "Error processing recharge: ${e.localizedMessage}"
                )
            }
        }
    }
}

// Update RechargeUiState to include Recharge in Success state
sealed class RechargeUiState {
    object Initial : RechargeUiState()
    object Loading : RechargeUiState()
    data class Success(val recharge: Recharge) : RechargeUiState()
    data class Error(val message: String) : RechargeUiState()
}