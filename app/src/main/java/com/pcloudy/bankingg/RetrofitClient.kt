package com.pcloudy.bankingg

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // ─── Set the server URL here ───────────────────────────────────────────────
    //
    //  SAME WiFi  →  use your PC's local IP
    //    e.g.  "http://172.16.114.211:8080/"
    //
    //  DIFFERENT WiFi / different network  →  use ngrok public URL
    //    1. Run: ngrok http 8080
    //    2. Copy the https URL from ngrok output
    //    e.g.  "https://abc123.ngrok-free.app/"
    //
    //  Android Emulator  →  use emulator alias
    //    e.g.  "http://10.0.2.2:8080/"
    //
    const val BASE_URL = "https://demo-banking-server-production.up.railway.app";

    // Stored after successful login — auto-sent as Bearer token on every request
    var authToken: String? = null

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor { chain ->
            val request = authToken?.let { token ->
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } ?: chain.request()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)
    val bankingApiService: BankingApiService = retrofit.create(BankingApiService::class.java)
}
