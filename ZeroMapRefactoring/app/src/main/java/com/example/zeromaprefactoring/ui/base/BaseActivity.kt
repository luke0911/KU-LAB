package com.example.zeromaprefactoring.ui.base

import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.zeromaprefactoring.R
import com.example.login.AuthManager
import com.google.android.material.button.MaterialButton

abstract class BaseActivity : AppCompatActivity() {

    override fun onDestroy() {
        super.onDestroy()
        // 앱이 완전히 종료될 때 로그아웃
        if (isFinishing && isTaskRoot) {
            AuthManager.logout()
            Log.d(TAG, "앱 종료 - 자동 로그아웃 완료")
        }
    }

    protected fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.navigationBars() or WindowInsets.Type.statusBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }
    }

    protected fun setupBottomNavigation(currentActivityClass: Class<*>) {
        try {
            val btnNavHome = findViewById<MaterialButton>(R.id.btnNavHome)
            val btnNavMap = findViewById<MaterialButton>(R.id.btnNavMap)
            val btnNavAlert = findViewById<MaterialButton>(R.id.btnNavAlert)
            val btnNavStats = findViewById<MaterialButton>(R.id.btnNavStats)

            if (currentActivityClass != com.example.zeromaprefactoring.ui.home.MainPage::class.java) {
                btnNavHome.setOnClickListener { navigateTo(com.example.zeromaprefactoring.ui.home.MainPage::class.java) }
            }

            if (currentActivityClass != com.example.zeromaprefactoring.ui.map.MainActivity::class.java) {
                btnNavMap.setOnClickListener { navigateTo(com.example.zeromaprefactoring.ui.map.MainActivity::class.java) }
            }

            if (currentActivityClass != com.example.zeromaprefactoring.ui.incident.IncidentActivity::class.java) {
                btnNavAlert.setOnClickListener { navigateTo(com.example.zeromaprefactoring.ui.incident.IncidentActivity::class.java) }
            }

            if (currentActivityClass != com.example.zeromaprefactoring.ui.chart.ChartActivity::class.java) {
                btnNavStats.setOnClickListener { navigateTo(com.example.zeromaprefactoring.ui.chart.ChartActivity::class.java) }
            }

            btnNavHome.isChecked = (currentActivityClass == com.example.zeromaprefactoring.ui.home.MainPage::class.java)
            btnNavMap.isChecked = (currentActivityClass == com.example.zeromaprefactoring.ui.map.MainActivity::class.java)
            btnNavAlert.isChecked = (currentActivityClass == com.example.zeromaprefactoring.ui.incident.IncidentActivity::class.java)
            btnNavStats.isChecked = (currentActivityClass == com.example.zeromaprefactoring.ui.chart.ChartActivity::class.java)

        } catch (e: Exception) {
            Log.e(TAG, "Navigation setup error", e)
            runOnUiThread {
                Toast.makeText(this, "네비게이션 설정 실패: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateTo(targetActivityClass: Class<*>) {
        val intent = Intent(this, targetActivityClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
        overridePendingTransition(0, 0)
    }

    companion object {
        private const val TAG = "BaseActivity"
    }
}
