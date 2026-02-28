package com.example.zeromaprefactoring.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.auth0.android.jwt.JWT
import com.example.zeromaprefactoring.network.api.ApiConstants
import com.example.zeromaprefactoring.R
import com.example.zeromaprefactoring.ui.main.MainPage
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

data class LoginRequest(
    val accountId: String,
    val password: String
)

data class LoginResponse(
    val statusCode: Int,
    val message: String?,
    val data: LoginData?
)

data class LoginData(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("userName") val userName: String
)

class LoginActivity : AppCompatActivity() {

    private lateinit var idField: EditText
    private lateinit var pwField: EditText
    private lateinit var loginBtn: Button
    private lateinit var registerBtn: Button

    private val client = OkHttpClient()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        loadSavedCredentials()
        setupListeners()
    }

    private fun initViews() {
        idField = findViewById(R.id.editTextAccountId)
        pwField = findViewById(R.id.editTextPassword)
        loginBtn = findViewById(R.id.buttonLogin)
        registerBtn = findViewById(R.id.buttonGoRegister)
    }

    private fun loadSavedCredentials() {
        val pref = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)
        pref.getString("ACCOUNT_ID", null)?.let { idField.setText(it) }
        pref.getString("ACCOUNT_PW", null)?.let { pwField.setText(it) }
    }

    private fun setupListeners() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                loginBtn.isEnabled = idField.text.isNotEmpty() && pwField.text.isNotEmpty()
            }
        }

        idField.addTextChangedListener(textWatcher)
        pwField.addTextChangedListener(textWatcher)

        loginBtn.setOnClickListener {
            val id = idField.text.toString()
            val pw = pwField.text.toString()

            if (id.isEmpty() || pw.isEmpty()) {
                showToast("아이디와 비밀번호를 입력하세요")
            } else {
                performLogin(id, pw)
            }
        }

        registerBtn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin(accountId: String, password: String) {
        val requestBody = gson.toJson(LoginRequest(accountId, password))
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${ApiConstants.SPRING_BASE_URL}/auth/login")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Network error: ${e.message}")
                runOnUiThread { showToast("서버 연결 실패") }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}, body: $body")

                if (response.isSuccessful && body != null) {
                    handleSuccessResponse(body, accountId, password)
                } else {
                    handleErrorResponse(body, response.code)
                }
            }
        })
    }

    private fun handleSuccessResponse(body: String, accountId: String, password: String) {
        try {
            val type = object : TypeToken<LoginResponse>() {}.type
            val result = gson.fromJson<LoginResponse>(body, type)

            if (result.statusCode == 200 && result.data != null) {
                val userId = extractUserIdFromToken(result.data.accessToken)
                if (userId != null) {
                    saveLoginInfo(userId, result.data, accountId, password)
                    navigateToMainPage()
                } else {
                    runOnUiThread { showToast("사용자 정보를 확인할 수 없습니다") }
                }
            } else {
                runOnUiThread { showToast("로그인 실패: ${result.message}") }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse error: ${e.message}", e)
            runOnUiThread { showToast("응답 처리 중 오류 발생") }
        }
    }

    private fun handleErrorResponse(body: String?, code: Int) {
        val errorMsg = try {
            val type = object : TypeToken<LoginResponse>() {}.type
            val result = gson.fromJson<LoginResponse>(body, type)
            result.message ?: "알 수 없는 오류"
        } catch (e: Exception) {
            "서버 오류 (코드: $code)"
        }
        runOnUiThread { showToast(errorMsg) }
    }

    private fun extractUserIdFromToken(token: String): String? {
        return try {
            JWT(token).getClaim("sub").asString()
        } catch (e: Exception) {
            Log.e(TAG, "JWT decode error: ${e.message}")
            null
        }
    }

    private fun saveLoginInfo(
        userId: String,
        loginData: LoginData,
        accountId: String,
        password: String
    ) {
        getSharedPreferences("USER_PREF", Context.MODE_PRIVATE).edit().apply {
            putString("USER_ID", userId)
            putString("ACCESS_TOKEN", loginData.accessToken)
            putString("REFRESH_TOKEN", loginData.refreshToken)
            putString("USER_NAME", loginData.userName)
            putString("ACCOUNT_ID", accountId)
            putString("ACCOUNT_PW", password)
            apply()
        }
        Log.d(TAG, "Login info saved: userId=$userId, userName=${loginData.userName}")
    }

    private fun navigateToMainPage() {
        runOnUiThread {
            showToast("로그인 성공")
             val intent = Intent(this, MainPage::class.java)
             startActivity(intent)
             finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
