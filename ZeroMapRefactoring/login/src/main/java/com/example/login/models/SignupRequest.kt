package com.example.login.models

import com.google.gson.annotations.SerializedName

data class SignupRequest(
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("postalCode") val postalCode: String?,
    @SerializedName("registrationNumber") val registrationNumber: String,
    @SerializedName("nationality") val nationality: String,
    @SerializedName("company") val company: String,
    @SerializedName("position") val position: String,
    @SerializedName("role") val role: String,
    @SerializedName("adminCode") val adminCode: String? = null
)
