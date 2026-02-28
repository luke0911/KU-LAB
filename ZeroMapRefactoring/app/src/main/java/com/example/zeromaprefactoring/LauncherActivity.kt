package com.example.zeromaprefactoring

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.login.AuthManager
import com.example.login.LoginActivity
import com.example.zeromaprefactoring.network.api.ApiConstants
import com.example.zeromaprefactoring.network.preferences.PreferenceHelper
import com.example.zeromaprefactoring.ui.home.MainPage

/**
 * SplashActivity (LauncherActivity 이름 변경)
 * - 앱 시작 시 스플래시 화면 표시
 * - 로그인 상태 확인
 * - 로그인 되어있으면 MainPage로, 아니면 LoginActivity로 이동
 */
class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 스플래시 화면 설정 (선택사항)
        // setContentView(R.layout.activity_loading)

        // AuthManager 초기화
        AuthManager.init(applicationContext)

        // 짧은 딜레이 후 다음 화면으로 이동
        Handler(Looper.getMainLooper()).postDelayed({
            if (AuthManager.isLoggedIn()) {
                // 이미 로그인되어 있으면 MainPage로 이동
                PreferenceHelper.setLaunchedFrom(this, "LAUNCHER")
                startActivity(Intent(this, MainPage::class.java))
                finish()
            } else {
                // 로그인 안 되어있으면 LoginActivity로 이동
                val intent = Intent(this, LoginActivity::class.java).apply {
                    putExtra("API_BASE_URL", ApiConstants.SPRING_BASE_URL)
                }
                startActivity(intent)
                finish()
            }
        }, 1000) // 1초 딜레이
    }
}
