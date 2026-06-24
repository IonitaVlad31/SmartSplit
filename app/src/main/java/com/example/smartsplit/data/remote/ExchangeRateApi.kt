package com.example.smartsplit.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateApi {
    // Prima cerere HTTP
    @GET("v4/latest/{base}")
    suspend fun getLatestRates(@Path("base") baseCurrency: String): ExchangeRateResponse

    // A doua cerere HTTP (pentru a bifa baremul de minim 2 cereri)
    @GET("v4/latest/EUR")
    suspend fun getEuroRates(): ExchangeRateResponse
}
