package com.example.zeromaprefactoring.network.models

data class FCMTokenDto(
    val token: String,
    val platform: String = "ANDROID"
)
