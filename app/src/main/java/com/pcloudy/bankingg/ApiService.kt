package com.pcloudy.bankingg

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("auth/biometric-login")
    suspend fun biometricLogin(): Response<LoginResponse>
}

interface BankingApiService {
    @GET("banking/balance")
    suspend fun getBalance(): Response<BalanceResponse>

    @POST("banking/upi/pay")
    suspend fun payUpi(@Body request: UpiRequest): Response<UpiResponse>

    @GET("banking/transactions")
    suspend fun listTransactions(@Query("page") page: Int = 1): Response<TransactionPage>

    @POST("banking/topup")
    suspend fun topup(@Body request: TopupRequest): Response<BalanceResponse>

    @GET("wallet/balance")
    suspend fun getWalletBalance(): Response<WalletBalanceResponse>

    @GET("banking/notifications")
    suspend fun getNotifications(): Response<NotificationsResponse>

    @POST("banking/transfer")
    suspend fun transfer(@Body request: TransferRequest): Response<TransferResponse>

    @POST("banking/bills/pay")
    suspend fun payBill(@Body request: BillPayRequest): Response<BillPayResponse>

    @POST("banking/recharge")
    suspend fun mobileRecharge(@Body request: RechargeRequest): Response<RechargeResponse>
}