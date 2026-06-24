package com.example.smartsplit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currency_rates")
data class CurrencyRate(
    @PrimaryKey val currencyCode: String,
    val rate: Double
)
