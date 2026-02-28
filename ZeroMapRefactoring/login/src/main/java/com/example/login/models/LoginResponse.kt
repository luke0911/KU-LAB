package com.example.login.models

data class LoginResponse(
    val statusCode: Int,
    val message: String?,
    val data: LoginData?
)
