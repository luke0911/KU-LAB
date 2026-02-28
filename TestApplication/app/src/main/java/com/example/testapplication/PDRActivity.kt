package com.example.testapplication

import android.view.MotionEvent
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.heropdr.HeroPDR
import com.kircherelectronics.fsensor.observer.SensorSubject.SensorObserver
import com.kircherelectronics.fsensor.sensor.FSensor
import com.kircherelectronics.fsensor.sensor.gyroscope.GyroscopeSensor
import kotlin.math.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PDRActivity : AppCompatActivity(), SensorEventListener {
    @Volatile private var stepFlagShared: Int = 0

    private var updatedAngle: Float = 0.0f

    // Constants
    private val HTML_FILE = "file:///android_asset/index.html"
    private val COMPASS_UPDATE_INTERVAL = 200L

    // Sensors and system services
    private val mSensorManager by lazy { getSystemService(SENSOR_SERVICE) as SensorManager }
    private val vibrator by lazy { getSystemService(VIBRATOR_SERVICE) as Vibrator }
    private lateinit var fSensor: FSensor
    private val mHandler: Handler = Handler(Looper.myLooper()!!)

    // UI components
    private lateinit var webView: WebView
    private lateinit var mapUpdateButton: Button
    private lateinit var debugView: TextView

    // PDR related variables
    val heroPDR: HeroPDR = HeroPDR()
    var stepCount: Int = 0
    var step_length: Float = 0.0f
    private var cur_PDR_position: Array<Float> = arrayOf(0.0f, 0.0f)
    var history_of_data: ArrayList<Array<String>> = arrayListOf()

    // Sensor data
    private var magMatrix: FloatArray = floatArrayOf()
    private var accMatrix: FloatArray = floatArrayOf()
    private var fusedOrientation = FloatArray(3)
    private var yaw_angle = 0.0f
    private var compassDirection = 0.0f
    private var input_angle: Float = 0.0f

    // State variables
    private var gyro_get_cnt = 0
    private var gyro_stabilized = false
    private var gyro_cali_value = 0.0f
    private var isFirstInit: Boolean = true
    private var cur_floor: String = ""

    // 센서 데이터 보관용
    private var lastPressure: Float = 0f

    // 영점 기준 시간
    private var startTime: Long = 0

    // 계단 버튼 상태 및 현재 층
    private var stairsPressed: Boolean = false

    // 버튼 참조
    private lateinit var btnAscend: Button
    private lateinit var btnDescend: Button

    private var cur_floor_int: Int = 1

    // PDR 로그 파일 관련
    private var pdrLogFile: File? = null
    private var pdrLogWriter: BufferedWriter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        webView = findViewById(R.id.webView)
        mapUpdateButton = findViewById(R.id.mapUpdateButton)
        debugView = findViewById(R.id.debugView)
        startTime = System.currentTimeMillis()

        btnAscend = findViewById(R.id.btnAscend)
        btnDescend = findViewById(R.id.btnDescend)

        // Setup
        checkPermission()
        setupWebView()

        // Get data from intent
        cur_PDR_position[0] = intent.getStringExtra("input_pos_x")!!.toFloat()
        cur_PDR_position[1] = intent.getStringExtra("input_pos_y")!!.toFloat()
        cur_floor = intent.getStringExtra("cur_floor")!!
        input_angle = intent.getStringExtra("cur_angle")!!.toFloat()

        cur_floor_int = if (cur_floor.startsWith("B")) -cur_floor.substring(1).toInt() else cur_floor.toInt()

        startTime = System.currentTimeMillis()

        // Initialize PDR log writer
        initPdrLogWriter()

        // Arrow direction update coroutine
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                updateArrowDirection()
                delay(300)
            }
        }

        // Debug view update coroutine
        CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                updateDebugView()
                delay(10)
            }
        }

        // Map update button
        mapUpdateButton.setOnClickListener {
            closePdrLogWriter()
            startActivity(Intent(this, SettingActivity::class.java))
        }

        // btnAscend: 누르는 동안 계단유무=1, 손을 떼면 cur_floor 갱신 (상승)
        btnAscend.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    stairsPressed = true
                    v.setBackgroundColor(Color.RED)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    stairsPressed = false
                    v.setBackgroundColor(Color.parseColor("#B6DAF3"))
                    cur_floor_int = if (cur_floor_int == -1) 1 else cur_floor_int + 1
                    Toast.makeText(this, "상승 버튼 release - 현재 층: $cur_floor_int", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // btnDescend: 누르는 동안 계단유무=1, 손을 떼면 cur_floor_int 갱신 (하강)
        btnDescend.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    stairsPressed = true
                    v.setBackgroundColor(Color.RED)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    stairsPressed = false
                    v.setBackgroundColor(Color.parseColor("#B6DAF3"))
                    cur_floor_int = if (cur_floor_int == 1) -1 else cur_floor_int - 1
                    Toast.makeText(this, "하강 버튼 release - 현재 층: $cur_floor_int", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    // 별도의 걸음 인식 리스너 정의
    private val stepDetectionListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                // SENSOR_DELAY_GAME로 들어온 센서값으로 heroPDR.isStep() 호출
                if (heroPDR.isStep(it.values.clone(), accMatrix)) {
                    // 걸음이 감지되면 공유 변수 업데이트 및 관련 처리
                    stepFlagShared = 1
                    val pdrResult = heroPDR.getStatus()
                    stepCount = pdrResult.totalStepCount
                    step_length = pdrResult.stepLength.toFloat()
                    handleStepDetection()
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun setupWebView() {
        webView.apply {
            // Setup webview client
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    change_floor(cur_floor)
                }
            }

            // Clear cache
            clearCache(true)
            clearHistory()
            clearFormData()

            // Load initial URL
            loadUrl(HTML_FILE)
            scrollTo(1690, 480)
            isScrollbarFadingEnabled = true
            setInitialScale(160)

            // Configure settings
            settings.apply {
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
                useWideViewPort = true
                builtInZoomControls = true
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = false
                setSupportMultipleWindows(false)
                setSupportZoom(true)
                domStorageEnabled = true
            }
        }
    }

    private fun updateDebugView() {
        if (gyro_stabilized) {
            val roundedYaw = yaw_angle.roundToInt()
            val roundedCompass = compassDirection.roundToInt()
            debugView.text = "자이로: ${roundedYaw}° / 나침반: ${roundedCompass}°"
        }
    }

    private fun updateArrowDirection() {
        mHandler.post {
            webView.loadUrl("javascript:rotateArrow(${input_angle + yaw_angle})")
        }
    }

    private fun initPdrLogWriter() {
        try {
            val fmt = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val ts = fmt.format(System.currentTimeMillis())
            val dir = filesDir  // 내부 저장소로 변경 (권한 불필요)
            if (!dir.exists()) dir.mkdirs()
            val name = "pdr_log_${ts}.txt"
            val file = File(dir, name)
            pdrLogFile = file
            pdrLogWriter = BufferedWriter(FileWriter(file, true))
            Log.d("PDR_LOG", "PDR logging to: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("PDR_LOG", "initPdrLogWriter failed: ${e.message}", e)
        }
    }

    private fun writePdrLog(x: Float, y: Float, gyro: Float, compass: Float, stepLen: Float, floor: Int) {
        pdrLogWriter?.let { writer ->
            try {
                writer.append("$x\t$y\t$gyro\t$compass\t$stepLen\t$floor\t-\t-\n")
                writer.flush()
            } catch (e: IOException) {
                Log.e("PDR_LOG", "Error writing PDR data", e)
            }
        }
    }

    private fun closePdrLogWriter() {
        try {
            pdrLogWriter?.close()
            pdrLogWriter = null
            Log.d("PDR_LOG", "PDR log writer closed")
        } catch (e: IOException) {
            Log.e("PDR_LOG", "Error closing PDR log writer", e)
        }
    }

    private fun calculateCompassDirection() {
        if (magMatrix.isNotEmpty() && accMatrix.isNotEmpty()) {
            try {
                val rotationMatrix = FloatArray(9)
                val orientationAngles = FloatArray(3)

                SensorManager.getRotationMatrix(rotationMatrix, null, accMatrix, magMatrix)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                compassDirection = ((Math.toDegrees(orientationAngles[0].toDouble()) + 360) % 360).toFloat()
            } catch (e: Exception) {
                Log.e("MainActivity", "나침반 방향 계산 오류: ${e.message}")
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    accMatrix = it.values.clone()
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    magMatrix = it.values.clone()
                }
                Sensor.TYPE_PRESSURE -> {
                    lastPressure = it.values[0]
                }
                Sensor.TYPE_ROTATION_VECTOR -> {
                    heroPDR.setQuaternion(it.values.clone())
                }
                Sensor.TYPE_GYROSCOPE -> {
                    // Skip if not stabilized
                    if (!gyro_stabilized) {
                        gyro_stabilized = gyroStabilize()
                        return
                    }

                    // Calculate yaw angle
                    yaw_angle = (((Math.toDegrees(fusedOrientation[0].toDouble()).toFloat() + 360) % 360 - gyro_cali_value) + 360) % 360
                    updatedAngle = ((input_angle + yaw_angle) + 360) % 360
                }
            }

            // Init completed
            if (isFirstInit && gyro_stabilized) {
                Toast.makeText(this, "지금부터 걸어주세요.", Toast.LENGTH_SHORT).show()
                vibrator.vibrate(160)
                isFirstInit = false

                // Draw initial position
                updatePositionOnMap(cur_PDR_position[0], cur_PDR_position[1])
            }
        }
    }

    private fun handleStepDetection() {
        vibrator.vibrate(30)

        // Calculate compass direction
        calculateCompassDirection()

        // Calculate new position
        val nextPosition = calculateNextPosition(cur_PDR_position, step_length, updatedAngle)

        // Write PDR log
        writePdrLog(
            x = nextPosition[0],
            y = nextPosition[1],
            gyro = updatedAngle,
            compass = compassDirection,
            stepLen = step_length,
            floor = cur_floor_int
        )

        // Update position on map
        updatePositionOnMap(nextPosition[0], nextPosition[1])
        cur_PDR_position = nextPosition
    }

    private fun calculateNextPosition(position: Array<Float>, stepLength: Float, angle: Float): Array<Float> {
        val radians = angle * PI / 180
        return arrayOf(
            position[0] - (stepLength * 10 * sin(-radians)).toFloat(),
            position[1] + (stepLength * 10 * cos(-radians)).toFloat()
        )
    }

    private fun updatePositionOnMap(x: Float, y: Float) {
        mHandler.post {
            webView.loadUrl("javascript:show_my_position_with_history($x, $y)")
        }
    }

    private fun change_floor(floor: String) {
        mHandler.post {
            webView.loadUrl("javascript:allReset()")
            webView.loadUrl("javascript:setTestbed('hansando', floor='${floor}', mode='history')")
        }
    }

    private fun gyroStabilize(): Boolean {
        if (++gyro_get_cnt > 200) {
            gyro_cali_value = ((Math.toDegrees(fusedOrientation[0].toDouble()) + 360) % 360).toFloat()
            return true
        }
        return false
    }

    private fun checkPermission() {
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (requiredPermissions.any { ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, requiredPermissions, 101)
        }
    }

    override fun onResume() {
        super.onResume()

        // Register all sensors
        val sensorTypes = arrayOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_ROTATION_VECTOR,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_GAME_ROTATION_VECTOR
        )

        sensorTypes.forEach { type ->
            mSensorManager.getDefaultSensor(type)?.let { sensor ->
                val rate = if (type == Sensor.TYPE_PRESSURE ||
                    type == Sensor.TYPE_MAGNETIC_FIELD ||
                    type == Sensor.TYPE_ACCELEROMETER)
                    SensorManager.SENSOR_DELAY_FASTEST
                else
                    SensorManager.SENSOR_DELAY_GAME
                mSensorManager.registerListener(this, sensor, rate)
            }
        }

        // Linear Acceleration 센서를 걸음 인식용으로 등록
        mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.let { sensor ->
            mSensorManager.registerListener(stepDetectionListener, sensor, SensorManager.SENSOR_DELAY_GAME)
        }

        // Initialize and register gyroscope sensor
        fSensor = GyroscopeSensor(this).apply {
            register(sensorObserver)
            start()
        }
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
        mSensorManager.unregisterListener(stepDetectionListener)
        (fSensor as? GyroscopeSensor)?.unregister(sensorObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        closePdrLogWriter()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }

    private val sensorObserver = object : SensorObserver {
        override fun onSensorChanged(values: FloatArray?) {
            values?.let { fusedOrientation = it }
        }
    }
}