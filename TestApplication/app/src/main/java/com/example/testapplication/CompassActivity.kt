package com.example.testapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.*
import android.os.*
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.heropdr.HeroPDR
import com.kircherelectronics.fsensor.observer.SensorSubject.SensorObserver
import com.kircherelectronics.fsensor.sensor.FSensor
import com.kircherelectronics.fsensor.sensor.gyroscope.GyroscopeSensor
import kotlinx.coroutines.*
import java.io.DataOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class CompassActivity : AppCompatActivity(), SensorEventListener {

    private val mSensorManager by lazy { getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private lateinit var fSensor: FSensor
    private lateinit var vibrator: Vibrator
    private val heroPDR = HeroPDR()

    private var accMatrix: FloatArray = floatArrayOf()
    private var magMatrix: FloatArray = floatArrayOf()
    private var fusedOrientation = FloatArray(3)

    private var yaw_angle: Float = 0f
    private var compassDirection: Float = 0f
    private var updatedAngle: Float = 0f
    private var input_angle: Float = 0f
    private var gyro_cali_value = 0f
    private var gyro_get_cnt = 0
    private var gyro_stabilized = false
    private var isFirstInit = true

    private var startTime = 0L
    private val logData = ArrayList<String>()

    private lateinit var mapUpdateButton: Button

    private val stepDetectionListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                if (heroPDR.isStep(it.values.clone(), accMatrix)) {
                    vibrator.vibrate(30)
                    handleStepDetection()
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val sensorObserver = object : SensorObserver {
        override fun onSensorChanged(values: FloatArray?) {
            values?.let { fusedOrientation = it }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass) // 필요시 layout 수정
        mapUpdateButton = findViewById(R.id.mapUpdateButton)
        checkPermission()
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        input_angle = 0f // 필요시 intent에서 가져올 수 있음
        startTime = System.currentTimeMillis()

        // Map update button
        mapUpdateButton.setOnClickListener {
            saveDataToFile()
            startActivity(Intent(this, SettingActivity::class.java))
        }

    }

    private fun handleStepDetection() {
        calculateCompassDirection()
        updatedAngle = ((input_angle + yaw_angle) + 360) % 360

        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0
        val logLine = String.format(
            Locale.getDefault(),
            "%.3f\t%.3f\t%.3f",
            elapsedTime,
            updatedAngle,
            compassDirection
        )
        logData.add(logLine)
        Log.d("CompassLog", logLine)
    }

    private fun calculateCompassDirection() {
        if (magMatrix.isNotEmpty() && accMatrix.isNotEmpty()) {
            try {
                val R = FloatArray(9)
                val orientation = FloatArray(3)
                SensorManager.getRotationMatrix(R, null, accMatrix, magMatrix)
                SensorManager.getOrientation(R, orientation)
                compassDirection = ((Math.toDegrees(orientation[0].toDouble()) + 360) % 360).toFloat()
            } catch (e: Exception) {
                Log.e("CompassActivity", "Compass error: ${e.message}")
            }
        }
    }

    private fun gyroStabilize(): Boolean {
        if (++gyro_get_cnt > 200) {
            gyro_cali_value = ((Math.toDegrees(fusedOrientation[0].toDouble()) + 360) % 360).toFloat()
            return true
        }
        return false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> accMatrix = it.values.clone()
                Sensor.TYPE_MAGNETIC_FIELD -> magMatrix = it.values.clone()
                Sensor.TYPE_ROTATION_VECTOR -> heroPDR.setQuaternion(it.values.clone())
                Sensor.TYPE_GYROSCOPE -> {
                    if (!gyro_stabilized) {
                        gyro_stabilized = gyroStabilize()
                        return
                    }
                    yaw_angle = (((Math.toDegrees(fusedOrientation[0].toDouble()).toFloat() + 360) % 360 - gyro_cali_value) + 360) % 360
                }
            }

            if (isFirstInit && gyro_stabilized) {
                isFirstInit = false
                Toast.makeText(this, "지금부터 걸어주세요.", Toast.LENGTH_SHORT).show()
                vibrator.vibrate(160)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Register all sensors
        val sensorTypes = arrayOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_LINEAR_ACCELERATION,
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

        // Linear Acceleration 센서를 두 번 등록합니다.
        mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.let { sensor ->
            // 글로벌 좌표계 처리는 FASTEST로
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
            // heroPDR.isStep() 처리는 GAME 딜레이 리스너로
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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun checkPermission() {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permissions.any {
                ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(this, permissions, 101)
        }
    }

    private fun saveDataToFile() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "SensorLog_$timestamp.txt"
        try {
            DataOutputStream(openFileOutput(fileName, MODE_APPEND)).use { dos ->
                // 첫 번째 행에 컬럼명 기록
                val header = "시간\t자이로\t나침반\n"
                dos.write(header.toByteArray())

                // 그 이후에 센서 데이터 기록
                logData.forEach { line ->
                    dos.write((line + "\n").toByteArray())
                }
            }
            Toast.makeText(this, "데이터 저장 완료: $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "데이터 저장 실패", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}
