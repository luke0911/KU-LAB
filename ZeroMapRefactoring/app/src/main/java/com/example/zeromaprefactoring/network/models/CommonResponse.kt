package com.example.zeromaprefactoring.network.models

data class CommonResponse<T>(
    val statusCode: Int,
    val data: T,
    val message: String
)
