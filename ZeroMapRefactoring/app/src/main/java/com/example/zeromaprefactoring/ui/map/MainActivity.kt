package com.example.zeromaprefactoring.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.zeromaprefactoring.R
import com.example.zeromaprefactoring.ui.base.BaseActivity

/**
 * MainActivity: 지도 기반 메인 화면
 * - 아파트 평면도 표시 (현재는 단순화된 버전)
 * - 실시간 위치 추적
 * - 하단 네비게이션 바 (홈, 지도, 사고, 통계)
 * - 다른 모듈(위치, 센서 등)과 통합되는 중심 화면
 */
class MainActivity : BaseActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private lateinit var mapContainer: FrameLayout
    private lateinit var locationInfoText: TextView
    private lateinit var statusText: TextView

    private var lastLocation: Location? = null
    private val DEFAULT_LAT = 37.3908
    private val DEFAULT_LNG = 126.9823

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flatmap)

        setupBottomNavigation(MainActivity::class.java)

        initViews()
        initLocationServices()
        requestLocationPermissions()
    }

    private fun initViews() {
        mapContainer = findViewById(R.id.unitySurfaceView2)

        // 디버그 정보 표시용 TextView (기존 레이아웃 활용)
        val debugView = findViewById<androidx.appcompat.widget.LinearLayoutCompat>(R.id.debugView)
        locationInfoText = findViewById(R.id.flatmapdebug)

        // 초기 메시지
        locationInfoText.text = "위치 정보를 가져오는 중..."
        debugView.visibility = android.view.View.VISIBLE

        // 맵 컨테이너에 임시 안내 메시지 추가
        val tempTextView = TextView(this).apply {
            text = "지도 화면\n(Unity 미구현 버전)\n\n현재 위치 추적 중..."
            textSize = 18f
            gravity = android.view.Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        }
        mapContainer.addView(tempTextView)
    }

    private fun initLocationServices() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = LocationListener { location ->
            if (location != null) {
                lastLocation = location
                updateLocationDisplay(location)
                Log.d(TAG, "Location updated: ${location.latitude}, ${location.longitude}")
            }
        }
    }

    private fun updateLocationDisplay(location: Location) {
        val lat = location.latitude
        val lng = location.longitude
        val acc = if (location.hasAccuracy()) location.accuracy else 5f

        locationInfoText.text = buildString {
            append("위치 정보\n")
            append("위도: ${"%.6f".format(lat)}\n")
            append("경도: ${"%.6f".format(lng)}\n")
            append("정확도: ${"%.1f".format(acc)}m\n")

            if (location.hasAltitude()) {
                append("고도: ${"%.1f".format(location.altitude)}m\n")
            }

            if (location.hasBearing()) {
                append("방향: ${"%.1f".format(location.bearing)}°\n")
            }

            if (location.hasSpeed()) {
                append("속도: ${"%.1f".format(location.speed)}m/s\n")
            }

            append("\n마지막 업데이트: ${System.currentTimeMillis()}")
        }
    }

    private fun requestLocationPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val needsRequest = permissions.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needsRequest) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Location permission not granted")
            return
        }

        try {
            // GPS 위치 업데이트 요청 (300ms 마다, 0.5m 이동 시)
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                300L,
                0.5f,
                locationListener
            )

            // 네트워크 위치도 함께 사용
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000L,
                1f,
                locationListener
            )

            // 마지막 알려진 위치 가져오기
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (lastKnownLocation != null) {
                lastLocation = lastKnownLocation
                updateLocationDisplay(lastKnownLocation)
            } else {
                // 기본 위치 표시
                locationInfoText.text = buildString {
                    append("위치 정보 대기 중...\n")
                    append("기본 위치: ${"%.6f".format(DEFAULT_LAT)}, ${"%.6f".format(DEFAULT_LNG)}\n")
                }
            }

            Log.d(TAG, "Location updates started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start location updates: ${e.message}")
            Toast.makeText(this, "위치 업데이트 시작 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    startLocationUpdates()
                    Toast.makeText(this, "위치 권한이 허용되었습니다", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "위치 권한이 거부되었습니다", Toast.LENGTH_LONG).show()
                    locationInfoText.text = "위치 권한이 없습니다.\n설정에서 위치 권한을 허용해주세요."
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            locationManager.removeUpdates(locationListener)
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to remove location updates: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }
}
