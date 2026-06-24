package com.example.smartsplit.data.remote

data class ExchangeRateResponse(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)
