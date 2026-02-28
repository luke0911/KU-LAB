package com.example.zeromaprefactoring.network.api

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * OkHttpClient 싱글톤 객체
 * 모든 네트워크 요청에서 공통으로 사용되는 HTTP 클라이언트
 */
object ApiClient {

    /**
     * 기본 OkHttpClient (타임아웃 없음)
     * - 일반적인 API 호출에 사용
     */
    val defaultClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .build()
    }

    /**
     * 타임아웃이 설정된 OkHttpClient (10초)
     * - Worker info, 센서/위치 로그 등 시간 제한이 필요한 API에 사용
     */
    val timeoutClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    /**
     * 커스텀 타임아웃 클라이언트 생성
     */
    fun createCustomClient(
        connectTimeout: Long = 10,
        readTimeout: Long = 10,
        writeTimeout: Long = 10,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(connectTimeout, timeUnit)
            .readTimeout(readTimeout, timeUnit)
            .writeTimeout(writeTimeout, timeUnit)
            .build()
    }
}
