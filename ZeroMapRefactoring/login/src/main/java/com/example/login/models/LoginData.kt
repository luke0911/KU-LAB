package com.example.login.models

import com.google.gson.annotations.SerializedName

data class LoginData(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("userName") val userName: String
)
