package com.example.login

import android.content.Context
import android.content.SharedPreferences

/**
 * AuthManager: 인증 관련 통합 관리 클래스
 * - 토큰 관리
 * - 로그인 상태 확인
 * - 사용자 정보 조회
 */
object AuthManager {

    private const val PREF_NAME = "USER_PREF"
    private const val KEY_USER_ID = "USER_ID"
    private const val KEY_ACCESS_TOKEN = "ACCESS_TOKEN"
    private const val KEY_REFRESH_TOKEN = "REFRESH_TOKEN"
    private const val KEY_USER_NAME = "USER_NAME"
    private const val KEY_ACCOUNT_ID = "ACCOUNT_ID"
    private const val KEY_ACCOUNT_PW = "ACCOUNT_PW"

    private var context: Context? = null

    /**
     * AuthManager 초기화 (앱 시작 시 호출)
     */
    fun init(appContext: Context) {
        context = appContext.applicationContext
    }

    private fun getPreferences(): SharedPreferences? {
        return context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 로그인 여부 확인
     */
    fun isLoggedIn(): Boolean {
        val accessToken = getAccessToken()
        return !accessToken.isNullOrEmpty()
    }

    /**
     * Access Token 가져오기
     */
    fun getAccessToken(): String? {
        return getPreferences()?.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Refresh Token 가져오기
     */
    fun getRefreshToken(): String? {
        return getPreferences()?.getString(KEY_REFRESH_TOKEN, null)
    }

    /**
     * User ID 가져오기
     */
    fun getUserId(): String? {
        return getPreferences()?.getString(KEY_USER_ID, null)
    }

    /**
     * User Name 가져오기
     */
    fun getUserName(): String? {
        return getPreferences()?.getString(KEY_USER_NAME, null)
    }

    /**
     * Account ID 가져오기
     */
    fun getAccountId(): String? {
        return getPreferences()?.getString(KEY_ACCOUNT_ID, null)
    }

    /**
     * 저장된 비밀번호 가져오기 (보안 주의!)
     */
    fun getSavedPassword(): String? {
        return getPreferences()?.getString(KEY_ACCOUNT_PW, null)
    }

    /**
     * Access Token 업데이트
     */
    fun updateAccessToken(newToken: String) {
        getPreferences()?.edit()?.apply {
            putString(KEY_ACCESS_TOKEN, newToken)
            apply()
        }
    }

    /**
     * 로그아웃 (모든 인증 정보 삭제)
     */
    fun logout() {
        getPreferences()?.edit()?.apply {
            remove(KEY_USER_ID)
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_NAME)
            remove(KEY_ACCOUNT_ID)
            remove(KEY_ACCOUNT_PW)
            apply()
        }
    }

    /**
     * 모든 정보 삭제 (회원 탈퇴 시 등)
     */
    fun clearAll() {
        getPreferences()?.edit()?.clear()?.apply()
    }

    /**
     * Authorization Header 생성
     */
    fun getAuthorizationHeader(): String? {
        val token = getAccessToken()
        return if (token != null) "Bearer $token" else null
    }
}
