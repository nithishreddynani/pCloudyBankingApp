package com.pcloudy.bankingg

data class NetworkCall(
    val endpoint: String,
    val duration: Long,
    val isSuccess: Boolean
)