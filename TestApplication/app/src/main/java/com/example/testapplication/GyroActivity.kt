// GyroActivity.kt
package com.example.testapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.kircherelectronics.fsensor.observer.SensorSubject.SensorObserver
import com.kircherelectronics.fsensor.sensor.FSensor
import com.kircherelectronics.fsensor.sensor.gyroscope.GyroscopeSensor
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class GyroActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var yawRealtimeView: TextView
    private lateinit var yawLogView: TextView
    private lateinit var measureIntervalSeekBar: SeekBar
    private lateinit var recordIntervalSeekBar: SeekBar
    private lateinit var measureIntervalLabel: TextView
    private lateinit var recordIntervalLabel: TextView

    private lateinit var sensorManager: SensorManager
    private lateinit var fSensor: FSensor

    private var startTime: Long = 0L
    private var yawAngle = 0f
    private var fusedOrientation = FloatArray(3)
    private var gyroCount = 0
    private var gyroCalibrated = false
    private var gyroCaliValue = 0f

    private var measureIntervalMs = 500L
    private var recordIntervalMs = 10000L
    private lateinit var logFileName: String

    private val measurementList = mutableListOf<Pair<Float, Float>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gyro)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        startTime = System.currentTimeMillis()

        // 로그 파일 이름: 날짜_실행시간
        val runTimestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date(startTime))
        logFileName = "YawLog_${runTimestamp}.txt"

        yawRealtimeView       = findViewById(R.id.yawRealtimeView)
        yawLogView            = findViewById(R.id.yawLogView)
        measureIntervalSeekBar = findViewById(R.id.seekMeasureInterval)
        recordIntervalSeekBar  = findViewById(R.id.seekRecordInterval)
        measureIntervalLabel   = findViewById(R.id.labelMeasureInterval)
        recordIntervalLabel    = findViewById(R.id.labelRecordInterval)

        checkPermission()
        initializeLogFile()

        // SeekBar 설정
        measureIntervalSeekBar.max = 2000
        measureIntervalSeekBar.progress = measureIntervalMs.toInt()
        measureIntervalSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                measureIntervalMs = p.coerceAtLeast(100).toLong()
                measureIntervalLabel.text = "측정 간격: ${measureIntervalMs}ms"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        recordIntervalSeekBar.max = 30000
        recordIntervalSeekBar.progress = recordIntervalMs.toInt()
        recordIntervalSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                recordIntervalMs = p.coerceAtLeast(1000).toLong()
                recordIntervalLabel.text = "기록 간격: ${recordIntervalMs / 1000.0}s"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // 측정 루프 (Calibration 후 시작)
        CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                if (!gyroCalibrated) {
                    delay(measureIntervalMs)
                    continue
                }
                val elapsedSec = (System.currentTimeMillis() - startTime) / 1000f
                measurementList.add(elapsedSec to yawAngle)
                yawRealtimeView.text = "Yaw: ${yawAngle.roundToInt()}°"
                delay(measureIntervalMs)
            }
        }

        // 기록 루프
        CoroutineScope(Dispatchers.IO).launch {
            val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            while (isActive) {
                delay(recordIntervalMs)
                try {
                    openFileOutput(logFileName, Context.MODE_APPEND).bufferedWriter().use { w ->
                        measurementList.forEach { (t, y) ->
                            w.write("${"%.3f".format(t)}\t${y.roundToInt()}\n")
                        }
                    }
                    measurementList.clear()
                    withContext(Dispatchers.Main) {
                        yawLogView.text = "${timeFmt.format(Date())} - 기록 완료"
                    }
                } catch (e: Exception) {
                    Log.e("GyroActivity", "파일 기록 오류: ${e.message}")
                }
            }
        }
    }

    private fun initializeLogFile() {
        val header = "time\tyaw\n"
        openFileOutput(logFileName, Context.MODE_PRIVATE).use { it.write(header.toByteArray()) }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.also { g ->
            sensorManager.registerListener(this, g, SensorManager.SENSOR_DELAY_GAME)
        }
        fSensor = GyroscopeSensor(this).apply {
            register(sensorObserver)
            start()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        (fSensor as? GyroscopeSensor)?.unregister(sensorObserver)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.takeIf { it.sensor.type == Sensor.TYPE_GYROSCOPE }?.let {
            if (!gyroCalibrated) {
                gyroCalibrated = (++gyroCount > 200)
                if (gyroCalibrated) {
                    gyroCaliValue = ((Math.toDegrees(fusedOrientation[0].toDouble()) + 360) % 360).toFloat()
                }
                return
            }
            val raw = (((Math.toDegrees(fusedOrientation[0].toDouble()) + 360) % 360) - gyroCaliValue).toFloat().let { (it + 360) % 360 }
            yawAngle = raw
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private val sensorObserver = object : SensorObserver {
        override fun onSensorChanged(values: FloatArray?) {
            values?.let { fusedOrientation = it }
        }
    }

    private fun checkPermission() {
        val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (perms.any { ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, perms, 0)
        }
    }
}
