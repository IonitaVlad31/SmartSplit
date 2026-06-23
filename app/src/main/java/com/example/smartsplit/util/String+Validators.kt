package com.example.smartsplit.util

import android.util.Patterns

fun String.isValidEmail() = isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
fun String.isValidPassword() = this.length > 4
