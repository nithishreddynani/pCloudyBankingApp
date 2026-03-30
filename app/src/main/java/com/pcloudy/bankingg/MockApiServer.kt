package com.pcloudy.bankingg

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.QueueDispatcher
import okhttp3.mockwebserver.RecordedRequest
import java.util.concurrent.TimeUnit

import android.util.Log

object MockApiServer {
    private var started = false

    private val server = MockWebServer().apply {
        dispatcher = object : QueueDispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                Log.d("MockApiServer", "Received request: ${request.method} ${request.path}")
                val path = request.path?.substringBefore('?') ?: ""  // Remove query parameters
                return when (path) {
                    "/auth/login"        -> handleLogin(request)
                    "/banking/balance"   -> handleBalance()
                    "/banking/upi/pay"   -> handleUpi(request)
                    "/banking/transactions" -> handleTransactions(request)
                    "/banking/topup"     -> handleTopup(request)
                    "/banking/notifications" -> handleNotifications()
                    else                 -> {
                        Log.w("MockApiServer", "Unknown path: ${request.path}")
                        MockResponse().setResponseCode(404)
                    }
                }
            }
        }
    }

    fun start() {
        if (started) {
            Log.d("MockApiServer", "Mock server already started, skipping")
            return
        }
        try {
            Log.d("MockApiServer", "Attempting to start mock server on port 8080")
            server.start(8080)
            started = true
            Log.d("MockApiServer", "Mock server started successfully on port 8080")
        } catch (e: java.net.BindException) {
            started = true
            Log.d("MockApiServer", "Mock server already running on port 8080")
        } catch (e: Exception) {
            Log.e("MockApiServer", "Failed to start mock server", e)
            throw e
        }
    }

    fun shutdown() {
        try {
            server.shutdown()
            Log.d("MockApiServer", "Mock server shut down")
        } catch (e: Exception) {
            Log.e("MockApiServer", "Failed to shutdown mock server", e)
        }
    }

    private fun handleLogin(req: RecordedRequest): MockResponse {
        val body = req.body.readUtf8()
        return if (body.contains("\"username\":\"test\"") && body.contains("\"password\":\"test\"")) {
            MockResponse().setResponseCode(200)
                .setBody("""{"token":"mock-token","username":"test"}""")
        } else {
            MockResponse().setResponseCode(401)
                .setBody("""{"error":"invalid credentials"}""")
        }
    }

    private fun handleBalance(): MockResponse {
        val field = if ((System.currentTimeMillis() / 1000) % 2 == 0L) "balance" else "amount"
        return MockResponse().setResponseCode(200)
            .setBody("""{"$field":24350.00}""")
    }

    private fun handleUpi(req: RecordedRequest): MockResponse {
        val body = req.body.readUtf8()
        return when {
            body.contains("\"upiId\":\"fail@test\"") ->
                MockResponse().setResponseCode(400)
                    .setBody("""{"status":"failure","message":"UPI ID not found"}""")
            body.contains("\"upiId\":\"timeout@test\"") ->
                MockResponse().setResponseCode(200).setBodyDelay(10, TimeUnit.SECONDS)
                    .setBody("""{"status":"success"}""")
            else ->
                MockResponse().setResponseCode(200).setBody("""{"status":"success"}""")
        }
    }

    private fun handleTransactions(req: RecordedRequest): MockResponse {
        val page = req.requestUrl?.queryParameter("page")?.toIntOrNull() ?: 1
        val transactions = listOf(
            """{"id":"1","type":"credit","amount":150.0,"date":"2023-10-01"}""",
            """{"id":"2","type":"debit","amount":200.0,"date":"2023-10-02"}""",
            """{"id":"3","type":"credit","amount":100.0,"date":"2023-10-03"}"""
        )
        val hasMore = page < 3
        return MockResponse().setResponseCode(200)
            .setBody("""{"page":$page,"transactions":[${transactions.joinToString(",")}],"hasMore":$hasMore}""")
    }

    private fun handleTopup(req: RecordedRequest): MockResponse {
        return MockResponse().setResponseCode(200)
            .setBody("""{"balance":25000.00}""")
    }

    private fun handleNotifications(): MockResponse {
        return MockResponse().setResponseCode(200)
            .setBody("""[{"type":"low_balance","message":"Your balance is below ₹500"}]""")
    }
}