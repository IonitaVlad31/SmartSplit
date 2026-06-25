package com.example.smartsplit.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateApi {
    
    @GET("v4/latest/{base}")
    suspend fun getLatestRates(@Path("base") baseCurrency: String): ExchangeRateResponse

    
    @GET("v4/latest/EUR")
    suspend fun getEuroRates(): ExchangeRateResponse
}
