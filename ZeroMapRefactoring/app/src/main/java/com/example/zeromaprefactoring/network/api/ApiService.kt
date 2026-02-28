package com.example.zeromaprefactoring.network.api

import android.util.Log
import org.json.JSONObject
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import com.example.zeromaprefactoring.network.models.*

/**
 * API 호출을 담당하는 서비스 객체
 * - 센서 데이터 전송
 * - 위치 데이터 전송
 * - CSV 파일 전송
 */
object ApiService {
    private const val TAG = "ApiService"
    private val gson = Gson()
    private val jsonMediaType = "application/json".toMediaType()
    private val csvMediaType = "text/csv".toMediaType()

    /**
     * 센서 데이터 전송
     */
    fun sendSensorData(
        sensorDto: SensorDto,
        accessToken: String,
        onUnauthorized: () -> Unit = {},
        onSuccess: ((Response) -> Unit)? = null,
        onFailure: ((IOException) -> Unit)? = null
    ) {
        val json = gson.toJson(sensorDto)
        Log.d(TAG, "센서 요청 바디: $json")

        val requestBody = json.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("${ApiConstants.EXPRESS_BASE_URL}/sensors")
            .post(requestBody)
            .header("Authorization", "Bearer $accessToken")
            .build()

        ApiClient.timeoutClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "센서 전송 실패: ${e.message}")
                onFailure?.invoke(e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "센서 전송 응답 코드: ${response.code}")
                response.body?.string()?.let { body ->
                    Log.d(TAG, "센서 전송 응답 바디: $body")
                }

                when (response.code) {
                    401 -> onUnauthorized()
                    in 200..299 -> onSuccess?.invoke(response)
                }
            }
        })
    }

    /**
     * 위치 데이터 전송
     */
    fun sendLocationData(
        locationDto: LocationDto,
        accessToken: String,
        onUnauthorized: () -> Unit = {},
        onSuccess: ((Response) -> Unit)? = null,
        onFailure: ((IOException) -> Unit)? = null
    ) {
        val json = gson.toJson(locationDto)
        Log.d(TAG, "위치 로그 요청 바디: $json")

        val requestBody = json.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("${ApiConstants.EXPRESS_BASE_URL}/locations")
            .post(requestBody)
            .header("Authorization", "Bearer $accessToken")
            .build()

        ApiClient.timeoutClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "위치 로그 전송 실패: ${e.message}")
                onFailure?.invoke(e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "위치 로그 응답 코드: ${response.code}")
                response.body?.string()?.let { body ->
                    Log.d(TAG, "위치 로그 응답 바디: $body")
                }

                when (response.code) {
                    401 -> onUnauthorized()
                    in 200..299 -> onSuccess?.invoke(response)
                }
            }
        })
    }

    /**
     * CSV 파일 전송
     */
    fun sendCsvData(
        csvFile: File,
        accessToken: String,
        onUnauthorized: () -> Unit = {},
        onSuccess: ((Response) -> Unit)? = null,
        onFailure: ((IOException) -> Unit)? = null
    ) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                csvFile.name,
                csvFile.asRequestBody(csvMediaType)
            )
            .build()

        Log.d(TAG, "CSV 파일 전송: ${csvFile.name}")

        val request = Request.Builder()
            .url("${ApiConstants.EXPRESS_BASE_URL}/csvs")
            .post(requestBody)
            .header("Authorization", "Bearer $accessToken")
            .build()

        ApiClient.timeoutClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "CSV 전송 실패: ${e.message}")
                onFailure?.invoke(e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "CSV 전송 응답 코드: ${response.code}")
                response.body?.string()?.let { body ->
                    Log.d(TAG, "CSV 전송 응답 바디: $body")
                }

                when (response.code) {
                    401 -> onUnauthorized()
                    in 200..299 -> onSuccess?.invoke(response)
                }
            }
        })
    }

    /**
     * FCM 토큰 저장
     */
    fun saveFCMToken(
        fcmTokenDto: FCMTokenDto,
        accessToken: String,
        onUnauthorized: () -> Unit = {},
        onSuccess: ((Response) -> Unit)? = null,
        onFailure: ((IOException) -> Unit)? = null
    ) {
        val jsonBody = JSONObject().apply {
            put("token", fcmTokenDto.token)
            put("platform", fcmTokenDto.platform)
        }

        val requestBody = jsonBody.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("${ApiConstants.SPRING_BASE_URL}/fcm-token")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $accessToken")
            .post(requestBody)
            .build()

        ApiClient.timeoutClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "FCM Token 저장 실패: ${e.message}")
                onFailure?.invoke(e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "FCM Token 저장 응답 코드: ${response.code}")
                response.body?.string()?.let { body ->
                    Log.d(TAG, "FCM Token 저장 응답 바디: $body")
                }

                when (response.code) {
                    401 -> onUnauthorized()
                    in 200..299 -> onSuccess?.invoke(response)
                }
            }
        })
    }
}
