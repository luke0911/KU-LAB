package com.example.zeromaprefactoring

import android.content.Context
import android.content.Intent
import com.example.login.AuthManager
import com.example.login.LoginActivity
import com.example.zeromaprefactoring.network.api.ApiConstants

/**
 * LoginHelper: app 모듈에서 login 모듈 사용을 위한 헬퍼
 */
object LoginHelper {

    /**
     * LoginActivity 시작
     */
    fun startLoginActivity(context: Context) {
        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra("API_BASE_URL", ApiConstants.SPRING_BASE_URL)
            putExtra("TARGET_ACTIVITY", "com.example.zeromaprefactoring.ui.home.MainPage")
        }
        context.startActivity(intent)
    }

    /**
     * 로그인 여부 확인
     */
    fun isLoggedIn(): Boolean {
        return AuthManager.isLoggedIn()
    }

    /**
     * AuthManager 초기화
     */
    fun initAuthManager(context: Context) {
        AuthManager.init(context)
    }

    /**
     * 로그아웃
     */
    fun logout() {
        AuthManager.logout()
    }

    /**
     * Access Token 가져오기
     */
    fun getAccessToken(): String? {
        return AuthManager.getAccessToken()
    }

    /**
     * User ID 가져오기
     */
    fun getUserId(): String? {
        return AuthManager.getUserId()
    }

    /**
     * User Name 가져오기
     */
    fun getUserName(): String? {
        return AuthManager.getUserName()
    }
}
