package com.pcloudy.bankingg

import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.ui.window.application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random
import android.os.Process
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _balance = MutableLiveData(1000.0)
    val balance: LiveData<Double> = _balance

    private val _networkCalls = MutableLiveData<List<NetworkCall>>(emptyList())
    val networkCalls: LiveData<List<NetworkCall>> = _networkCalls

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    var simulatedDelay: Long = 0
    var crashProbability: Float = 0f

    enum class OperationType {
        BANKING_AND_ATM,  // For transfers, bill payments, and ATM searches
        NETWORK          // For network monitoring only
    }

    private fun logNetworkCall(call: NetworkCall) {
        val currentCalls = _networkCalls.value.orEmpty()
        _networkCalls.postValue((currentCalls + call).takeLast(20))
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.Main) {
            _isLoading.value = false
            _error.value = null
            _balance.value = 1000.0
            _networkCalls.value = emptyList()
        }
    }

    suspend fun simulateApiCall(
        endpoint: String,
        operationType: OperationType = OperationType.BANKING_AND_ATM,
        action: suspend () -> Unit
    ) = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            _isLoading.postValue(true)
            _error.postValue(null)

            if (operationType == OperationType.BANKING_AND_ATM) {
                if (Random.nextFloat() < crashProbability) {
                    // Generate crash message
                    val crashMessage = when (Random.nextInt(3)) {
                        0 -> """
                        FATAL EXCEPTION: ${Thread.currentThread().name}
                        Process: com.pcloudy.bankingg, PID: ${Process.myPid()}
                        java.lang.RuntimeException: Simulated crash in banking application
                        Crash probability: ${crashProbability * 100}%
                        Thread: ${Thread.currentThread().id}
                        at com.pcloudy.bankingg.MainViewModel.simulateApiCall(MainViewModel.kt:${Random.nextInt(100, 999)})
                        at com.pcloudy.bankingg.MainActivity.performOperation(MainActivity.kt:${Random.nextInt(100, 999)})
                        at android.app.ActivityThread.handleMessage(ActivityThread.java:${Random.nextInt(1000, 9999)})
                    """.trimIndent()

                        1 -> """
                        AndroidRuntime: FATAL EXCEPTION: main
                        Process: com.pcloudy.bankingg
                        java.lang.IllegalStateException: Critical error during banking operation
                        Operation: $endpoint
                        Time: ${System.currentTimeMillis()}
                        at com.pcloudy.bankingg.MainViewModel.simulateApiCall(MainViewModel.kt:${Random.nextInt(100, 999)})
                        at com.pcloudy.bankingg.TransactionFragment.processTransaction(TransactionFragment.kt:${Random.nextInt(100, 999)})
                    """.trimIndent()

                        else -> """
                        Exception in thread "${Thread.currentThread().name}":
                        java.lang.SecurityException: Authentication failed during operation
                        Process ID: ${Process.myPid()}
                        Operation Type: $operationType
                        at com.pcloudy.bankingg.security.TransactionValidator.validate(TransactionValidator.kt:${Random.nextInt(100, 999)})
                        at com.pcloudy.bankingg.MainViewModel.simulateApiCall(MainViewModel.kt:${Random.nextInt(100, 999)})
                    """.trimIndent()
                    }

                    // Log the crash
                    Log.e("BankingApp", crashMessage)

                    // Show error in UI and close app after a short delay
                    withContext(Dispatchers.Main) {
                        _error.value = "System Error: Application crashed"

                        // Delay to show the error message
                        delay(1000)

                        // Get activity context and close the app
                        getApplication<Application>().let { app ->
                            val intent = app.packageManager.getLaunchIntentForPackage(app.packageName)?.apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            app.startActivity(intent)

                            // Force close the app
                            Handler(Looper.getMainLooper()).postDelayed({
                                android.os.Process.killProcess(android.os.Process.myPid())
                            }, 100)
                        }
                    }

                    throw RuntimeException(crashMessage)
                }

                if (simulatedDelay > 0) {
                    delay(simulatedDelay)
                }
            }

            action()

            val duration = System.currentTimeMillis() - startTime
            addNetworkCall(NetworkCall(endpoint, duration, true))
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            addNetworkCall(NetworkCall(endpoint, duration, false))
            throw e
        } finally {
            _isLoading.postValue(false)
        }
    }

    private fun addNetworkCall(call: NetworkCall) {
        val currentCalls = _networkCalls.value.orEmpty()
        _networkCalls.postValue(currentCalls + call)
    }

    private var billPaymentCount = 0
    private var transferCount = 0

    fun updateBalance(amount: Double) {
        val currentBalance = _balance.value ?: 0.0
        _balance.value = currentBalance + amount

        // Track bill payments and transfers
        if (amount < 0) {
            if (amount < 0 && transactions.value.firstOrNull()?.type == "bill") {
                billPaymentCount++
            } else if (amount < 0 && transactions.value.firstOrNull()?.type == "transfer") {
                transferCount++
            }

            // Check if salary needs to be credited
            checkAndCreditSalary()
        }
    }

    private fun checkAndCreditSalary() {
        if (billPaymentCount >= 4 || transferCount >= 3) {
            // Credit salary
            val salaryTransaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = 1000.0,
                type = "deposit",
                description = "Salary Deposit",
                date = System.currentTimeMillis(),
                status = "Completed"
            )

            // Add salary transaction
            addTransaction(salaryTransaction)

            // Update balance
            _balance.value = (_balance.value ?: 0.0) + 1000.0

            // Reset counters
            billPaymentCount = 0
            transferCount = 0
        }
    }

    fun addTransaction(transaction: Transaction) {
        val currentTransactions = _transactions.value.toMutableList()
        currentTransactions.add(0, transaction)
        val updatedTransactions = currentTransactions.take(3)
        _transactions.value = updatedTransactions
    }
}