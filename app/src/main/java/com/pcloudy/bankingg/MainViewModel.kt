package com.pcloudy.bankingg

import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.airbnb.epoxy.databinding.BuildConfig

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

    init {
        // Mock server is now started in MainActivity
        Log.d("MainViewModel", "MainViewModel initialized")
    }

    enum class OperationType {
        BANKING_AND_ATM,  // For transfers, bill payments, and ATM searches
        NETWORK          // For network monitoring only
    }

    private fun logNetworkCall(call: NetworkCall) {
        val currentCalls = _networkCalls.value.orEmpty()
        _networkCalls.postValue((currentCalls + call).takeLast(20))
    }

    fun logout() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    RetrofitClient.authApiService.logout()
                } catch (_: Exception) {
                    // best-effort — proceed with local cleanup regardless
                }
                RetrofitClient.authToken = null
            }
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
                type = "credit",
                amount = 1000.0,
                date = java.time.LocalDate.now().toString(),
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

    // New API integration properties
    private val _balanceResponse = MutableLiveData<BalanceResponse>()
    val balanceResponse: LiveData<BalanceResponse> = _balanceResponse

    private val _walletBalance = MutableLiveData<WalletBalanceResponse>()
    val walletBalance: LiveData<WalletBalanceResponse> = _walletBalance

    private val _upiResult = MutableLiveData<UpiResponse>()
    val upiResult: LiveData<UpiResponse> = _upiResult

    private val _transactionsPage = MutableStateFlow<TransactionPage?>(null)
    val transactionsPage: StateFlow<TransactionPage?> = _transactionsPage.asStateFlow()

    private val _notifications = MutableLiveData<List<NotificationItem>>()
    val notifications: LiveData<List<NotificationItem>> = _notifications

    private val _failedTransactions = MutableLiveData<MutableList<Transaction>>(mutableListOf())
    val failedTransactions: LiveData<MutableList<Transaction>> = _failedTransactions



    fun addFailedTransaction(upiId: String, amount: Double, reason: String) {
        val failed = Transaction(
            id = java.util.UUID.randomUUID().toString(),
            type = "debit",
            amount = -amount,
            description = "Failed: UPI to $upiId",
            date = java.time.Instant.now().toString(),
            status = "FAILED",
            referenceId = "TXN${System.currentTimeMillis()}"
        )
        val current = _failedTransactions.value ?: mutableListOf()
        current.add(0, failed)
        _failedTransactions.postValue(current)
    }

    // API wrapper methods
    suspend fun registerUser(username: String, password: String, name: String): RegisterResponse =
        withContext(Dispatchers.IO) {
            val resp = RetrofitClient.authApiService.register(RegisterRequest(username, password, name))
            if (resp.isSuccessful) {
                val body = resp.body()!!
                RetrofitClient.authToken = body.token
                body
            } else throw Exception(resp.errorBody()?.string() ?: "Registration failed")
        }

    suspend fun loginUser(username: String, password: String): LoginResponse =
        withContext(Dispatchers.IO) {
            val resp = RetrofitClient.authApiService.login(LoginRequest(username, password))
            if (resp.isSuccessful) {
                val body = resp.body()!!
                RetrofitClient.authToken = body.token  // store token for all subsequent requests
                body
            } else throw Exception(resp.errorBody()?.string() ?: "login failed")
        }

    suspend fun fetchBalance() = withContext(Dispatchers.IO) {
        val resp = RetrofitClient.bankingApiService.getBalance()
        if (resp.isSuccessful) _balanceResponse.postValue(resp.body())
        else _error.postValue("balance error")
    }

    suspend fun submitUpiPayment(id: String, amount: Double) = withContext(Dispatchers.IO) {
        val resp = RetrofitClient.bankingApiService.payUpi(UpiRequest(id, amount))
        if (resp.isSuccessful) {
            _upiResult.postValue(resp.body())
            resp.body()?.newBalance?.let { newBal ->
                _balanceResponse.postValue(BalanceResponse(newBal))
            }
        } else throw Exception(resp.errorBody()?.string() ?: "Payment failed")
    }

    suspend fun loadTransactions(page: Int) = withContext(Dispatchers.IO) {
        val resp = RetrofitClient.bankingApiService.listTransactions(page)
        if (resp.isSuccessful) _transactionsPage.value = resp.body()
        else _error.postValue("transactions error")
    }

    suspend fun fetchWalletBalance() = withContext(Dispatchers.IO) {
        val resp = RetrofitClient.bankingApiService.getWalletBalance()
        if (resp.isSuccessful) _walletBalance.postValue(resp.body())
        else _error.postValue("wallet balance error")
    }

    suspend fun topupWallet(amount: Double) = withContext(Dispatchers.IO) {
        val resp = RetrofitClient.bankingApiService.topup(TopupRequest(amount))
        if (!resp.isSuccessful) throw Exception(resp.errorBody()?.string() ?: "Top-up failed")
        // don't update _balanceResponse here — WalletFragment calls fetchBalance() after topup
    }

    suspend fun fetchNotifications() = withContext(Dispatchers.IO) {
        val resp = RetrofitClient.bankingApiService.getNotifications()
        if (resp.isSuccessful) _notifications.postValue(resp.body()?.notifications)
        else _error.postValue("notifications error")
    }

    suspend fun transfer(recipientName: String, amount: Double, description: String) = withContext(Dispatchers.IO) {
        _isLoading.postValue(true)
        try {
            val resp = RetrofitClient.bankingApiService.transfer(TransferRequest(recipientName, amount, description))
            if (resp.isSuccessful) {
                val newBal = resp.body()?.newBalance
                if (newBal != null) _balanceResponse.postValue(BalanceResponse(newBal))
            } else {
                val msg = resp.errorBody()?.string() ?: "Transfer failed"
                _error.postValue(msg)
                throw Exception(msg)
            }
        } finally {
            _isLoading.postValue(false)
        }
    }

    suspend fun payBill(billType: String, amount: Double) = withContext(Dispatchers.IO) {
        _isLoading.postValue(true)
        try {
            val resp = RetrofitClient.bankingApiService.payBill(BillPayRequest(billType, amount))
            if (resp.isSuccessful) {
                val newBal = resp.body()?.newBalance
                if (newBal != null) _balanceResponse.postValue(BalanceResponse(newBal))
            } else {
                val msg = resp.errorBody()?.string() ?: "Bill payment failed"
                _error.postValue(msg)
                throw Exception(msg)
            }
        } finally {
            _isLoading.postValue(false)
        }
    }

    suspend fun mobileRecharge(mobileNumber: String, operator: String, circle: String, rechargeType: String, amount: Double) = withContext(Dispatchers.IO) {
        _isLoading.postValue(true)
        try {
            val resp = RetrofitClient.bankingApiService.mobileRecharge(
                RechargeRequest(mobileNumber, operator, circle, rechargeType, amount)
            )
            if (resp.isSuccessful) {
                val newBal = resp.body()?.newBalance
                if (newBal != null) _balanceResponse.postValue(BalanceResponse(newBal))
            } else {
                val msg = resp.errorBody()?.string() ?: "Recharge failed"
                _error.postValue(msg)
                throw Exception(msg)
            }
        } finally {
            _isLoading.postValue(false)
        }
    }
}