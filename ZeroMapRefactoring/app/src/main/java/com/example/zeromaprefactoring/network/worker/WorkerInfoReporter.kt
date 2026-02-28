package com.example.zeromaprefactoring.network.worker

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import com.example.zeromaprefactoring.network.api.ApiClient
import com.example.zeromaprefactoring.network.api.ApiConstants

object WorkerInfoReporter {
    private const val TAG = "WorkerInfoReporter"
    private const val PREF_USER = "USER_PREF"
    private const val KEY_USER_ID = "USER_ID"
    private const val KEY_ACCESS_TOKEN = "ACCESS_TOKEN"

    private val client: OkHttpClient
        get() = ApiClient.timeoutClient

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun nowIso8601(offsetMs: Long = 0): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.KOREA)
        formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val now = System.currentTimeMillis() - offsetMs
        return formatter.format(Date(now))
    }

    private fun getUserPrefs(context: Context) =
        context.getSharedPreferences(PREF_USER, Context.MODE_PRIVATE)

    private fun getAccessToken(context: Context): String? =
        getUserPrefs(context).getString(KEY_ACCESS_TOKEN, null)

    private fun getUserId(context: Context): String? =
        getUserPrefs(context).getString(KEY_USER_ID, null)

    fun sendEntryTime(context: Context) {
        val token = getAccessToken(context)
        if (token.isNullOrBlank()) {
            Log.w(TAG, "Access token missing. Skip entryTime reporting.")
            return
        }

        val userId = getUserId(context)
        val bodyJson = JSONObject().apply {
            put("entryTime", nowIso8601())
            userId?.let { put("userId", it) }
        }.toString()

        val request = Request.Builder()
            .url("${ApiConstants.EXPRESS_BASE_URL}/worker-info")
            .post(bodyJson.toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to record entryTime", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.w(TAG, "EntryTime update returned HTTP ${it.code}")
                    } else {
                        Log.d(TAG, "EntryTime recorded successfully.")
                    }
                }
            }
        })
    }

    fun sendExitTime(context: Context, offsetTime: Long = 0, onComplete: (() -> Unit)? = null) {
        val token = getAccessToken(context)
        val userId = getUserId(context)
        if (token.isNullOrBlank() || userId.isNullOrBlank()) {
            Log.w(TAG, "Credentials missing. Skip exitTime reporting.")
            onComplete?.invoke()
            return
        }

        val bodyJson = JSONObject().apply {
            put("exitTime", nowIso8601(offsetTime))
        }.toString()

        val request = Request.Builder()
            .url("${ApiConstants.EXPRESS_BASE_URL}/worker-info/$userId")
            .put(bodyJson.toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Bearer $token")
            .build()

        if (onComplete == null) {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Failed to record exitTime", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!it.isSuccessful) {
                            Log.w(TAG, "ExitTime update returned HTTP ${it.code}")
                        } else {
                            Log.d(TAG, "ExitTime recorded successfully.")
                        }
                    }
                }
            })
            return
        }

        val completed = AtomicBoolean(false)
        val handler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (completed.compareAndSet(false, true)) {
                Log.w(TAG, "ExitTime request timeout fallback executed.")
                onComplete()
            }
        }
        handler.postDelayed(timeoutRunnable, 1_500L)

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to record exitTime", e)
                if (completed.compareAndSet(false, true)) {
                    handler.removeCallbacks(timeoutRunnable)
                    handler.post { onComplete() }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.w(TAG, "ExitTime update returned HTTP ${it.code}")
                    } else {
                        Log.d(TAG, "ExitTime recorded successfully.")
                    }
                }
                if (completed.compareAndSet(false, true)) {
                    handler.removeCallbacks(timeoutRunnable)
                    handler.post { onComplete() }
                }
            }
        })
    }

    fun recordOutingStart(context: Context) {
        updateOutingTimes(context) { entryArray, _ ->
            entryArray.put(nowIso8601())
        }
    }

    fun recordOutingReturn(context: Context) {
        updateOutingTimes(context) { _, exitArray ->
            exitArray.put(nowIso8601())
        }
    }

    private fun updateOutingTimes(
        context: Context,
        mutate: (JSONArray, JSONArray) -> Unit
    ) {
        val token = getAccessToken(context)
        val userId = getUserId(context)
        if (token.isNullOrBlank() || userId.isNullOrBlank()) {
            Log.w(TAG, "Credentials missing. Skip outing update.")
            return
        }

        val getRequest = Request.Builder()
            .url("${ApiConstants.EXPRESS_BASE_URL}/worker-info/$userId")
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(getRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to fetch worker info for outing update", e)
                sendOutingUpdateWithFreshArrays(token, userId, mutate)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string()
                    if (!it.isSuccessful || body.isNullOrBlank()) {
                        Log.w(TAG, "Fetch worker info failed for outing update. code=${it.code}")
                        sendOutingUpdateWithFreshArrays(token, userId, mutate)
                        return
                    }
                    try {
                        val root = JSONObject(body)
                        val data = root.optJSONObject("data") ?: JSONObject()
                        val entryArray = data.optJSONArray("outingEntryTime")?.let { arr ->
                            JSONArray(arr.toString())
                        } ?: JSONArray()
                        val exitArray = data.optJSONArray("outingExitTime")?.let { arr ->
                            JSONArray(arr.toString())
                        } ?: JSONArray()

                        mutate(entryArray, exitArray)

                        val payload = JSONObject().apply {
                            put("outingEntryTime", entryArray)
                            put("outingExitTime", exitArray)
                        }
                        sendOutingUpdate(token, userId, payload)
                    } catch (t: Throwable) {
                        Log.e(TAG, "Failed to parse worker info for outing update", t)
                        sendOutingUpdateWithFreshArrays(token, userId, mutate)
                    }
                }
            }
        })
    }

    private fun sendOutingUpdateWithFreshArrays(
        token: String,
        userId: String,
        mutate: (JSONArray, JSONArray) -> Unit
    ) {
        val entryArray = JSONArray()
        val exitArray = JSONArray()
        mutate(entryArray, exitArray)
        val payload = JSONObject()
        if (entryArray.length() > 0) {
            payload.put("outingEntryTime", entryArray)
        }
        if (exitArray.length() > 0) {
            payload.put("outingExitTime", exitArray)
        }
        if (payload.length() == 0) return
        sendOutingUpdate(token, userId, payload)
    }

    private fun sendOutingUpdate(token: String, userId: String, payload: JSONObject) {
        val putRequest = Request.Builder()
            .url("${ApiConstants.EXPRESS_BASE_URL}/worker-info/$userId")
            .put(payload.toString().toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(putRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to update outing info", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.w(TAG, "Outing update returned HTTP ${it.code}")
                    } else {
                        Log.d(TAG, "Outing info updated successfully.")
                    }
                }
            }
        })
    }
}
