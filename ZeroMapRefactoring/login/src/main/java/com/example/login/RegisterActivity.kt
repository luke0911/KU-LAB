package com.example.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.login.models.SignupRequest
import com.example.login.models.SignupResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private lateinit var phoneField: EditText
    private lateinit var nameField: EditText
    private lateinit var rrnFrontField: EditText
    private lateinit var rrnBackField: EditText
    private lateinit var companyField: EditText
    private lateinit var positionField: EditText
    private lateinit var registerBtn: Button
    private lateinit var backButton: ImageButton

    private val client = OkHttpClient()
    private val gson = Gson()

    private var apiBaseUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // API URL을 Intent로 받아옴
        apiBaseUrl = intent.getStringExtra("API_BASE_URL") ?: ""

        initViews()
        setupPhoneFormatter()
        setupTextWatchers()
        setupListeners()
    }

    private fun initViews() {
        phoneField = findViewById(R.id.editTextAccountId)
        nameField = findViewById(R.id.editTextName)
        rrnFrontField = findViewById(R.id.editTextRRN_Front)
        rrnBackField = findViewById(R.id.editTextRRN_Back)
        companyField = findViewById(R.id.editTextCompany)
        positionField = findViewById(R.id.textViewPositionLabel)
        registerBtn = findViewById(R.id.buttonRegisterConfirm)
        backButton = findViewById(R.id.buttonBack)
    }

    private fun setupPhoneFormatter() {
        phoneField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val digits = s?.toString()?.replace(Regex("\\D"), "") ?: ""
                if (digits != s.toString()) {
                    phoneField.removeTextChangedListener(this)
                    phoneField.setText(digits)
                    phoneField.setSelection(digits.length)
                    phoneField.addTextChangedListener(this)
                }
            }
        })
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateRegisterButtonState()
            }
        }

        nameField.addTextChangedListener(textWatcher)
        phoneField.addTextChangedListener(textWatcher)
        rrnFrontField.addTextChangedListener(textWatcher)
        rrnBackField.addTextChangedListener(textWatcher)
        companyField.addTextChangedListener(textWatcher)
        positionField.addTextChangedListener(textWatcher)
    }

    private fun setupListeners() {
        registerBtn.setOnClickListener { performRegister() }
        backButton.setOnClickListener { finish() }
    }

    private fun updateRegisterButtonState() {
        registerBtn.isEnabled = nameField.text.isNotEmpty() &&
                phoneField.text.isNotEmpty() &&
                rrnFrontField.text.isNotEmpty() &&
                rrnBackField.text.isNotEmpty() &&
                companyField.text.isNotEmpty() &&
                positionField.text.isNotEmpty()
    }

    private fun performRegister() {
        val phoneDigits = phoneField.text.toString().replace(Regex("\\D"), "")
        val name = nameField.text.toString().trim()
        val rrnFront = rrnFrontField.text.toString().replace(Regex("\\D"), "").take(6)
        val rrnBack = rrnBackField.text.toString().replace(Regex("\\D"), "").take(1)
        val company = companyField.text.toString().trim()
        val position = positionField.text.toString().trim()

        if (!validateInputs(phoneDigits, name, rrnFront, rrnBack, company, position)) {
            return
        }

        val requestData = SignupRequest(
            phoneNumber = phoneDigits,
            name = name,
            email = "",
            address = "",
            postalCode = "",
            registrationNumber = "$rrnFront-$rrnBack",
            nationality = "KR",
            company = company,
            position = position,
            role = "USER",
            adminCode = null
        )

        sendRegisterRequest(requestData)
    }

    private fun validateInputs(
        phone: String,
        name: String,
        rrnFront: String,
        rrnBack: String,
        company: String,
        position: String
    ): Boolean {
        if (name.isBlank() || phone.isBlank() || rrnFront.length != 6 ||
            rrnBack.length != 1 || company.isBlank() || position.isBlank()) {
            showToast("필수 항목을 모두 입력하세요")
            return false
        }

        if (!phone.matches(Regex("^010\\d{8}$"))) {
            showToast("전화번호는 010으로 시작하는 11자리여야 합니다")
            return false
        }

        return true
    }

    private fun sendRegisterRequest(requestData: SignupRequest) {
        val json = gson.toJson(requestData)
        Log.d(TAG, "Request: $json")

        val requestBody = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$apiBaseUrl/v2/users/signup")
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

                if (response.isSuccessful) {
                    handleSuccessResponse()
                } else {
                    handleErrorResponse(body, response.code)
                }
            }
        })
    }

    private fun handleSuccessResponse() {
        runOnUiThread {
            showToast("회원가입 성공! 로그인 해주세요")
            finish()
        }
    }

    private fun handleErrorResponse(body: String?, code: Int) {
        val errorMsg = try {
            val type = object : TypeToken<SignupResponse>() {}.type
            val result = gson.fromJson<SignupResponse>(body, type)
            result.message ?: "알 수 없는 오류"
        } catch (e: Exception) {
            "회원가입 실패 (코드: $code)"
        }
        runOnUiThread { showToast(errorMsg) }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "RegisterActivity"
    }
}
