package com.example.testapplication

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.Runnable
import kotlin.math.*

class SettingActivity2 : AppCompatActivity(), SensorEventListener {
    private var cur_floor: String = "B1"
    private val mSensorManager by lazy {
        getSystemService(SENSOR_SERVICE) as SensorManager
    }
    private var is_popup_on : Boolean = false
    private lateinit var alertDialog : AlertDialog
    /* 웹뷰 관련 변수 */
    private val mHandler : Handler = Handler(Looper.myLooper()!!)

    private lateinit var webView: WebView
    private lateinit var seekBar: SeekBar
    private lateinit var start_button: Button
    private lateinit var cur_angle: TextView
    private lateinit var input_pos_x: EditText
    private lateinit var input_pos_y: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_activity)

        val HTML_FILE = "file:///android_asset/index.html"

        webView = findViewById(R.id.webView)
        seekBar = findViewById(R.id.seekBar)
        start_button = findViewById(R.id.start_button)
        cur_angle = findViewById(R.id.cur_angle)
        input_pos_x = findViewById(R.id.input_pos_x)
        input_pos_y = findViewById(R.id.input_pos_y)

        webViewSetting(HTML_FILE) // 웹뷰 첫 세팅 (줌, 스크롤, js 허용 등)

        seekBar.setMax(360);
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress % 90 == 0) {
                    cur_angle.text = "방향 : \n${progress}도"
                    webView.loadUrl("javascript:rotateArrow($progress)")
                } else {
                    seekBar!!.progress = (progress / 90) * 90
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        start_button.setOnClickListener {
            val intent = Intent(this, PDRActivity::class.java)
            intent.putExtra("input_pos_x", input_pos_x.text.toString())
            intent.putExtra("input_pos_y", input_pos_y.text.toString())
            intent.putExtra("cur_angle", cur_angle.text.filter(Char::isDigit))
            intent.putExtra("cur_floor", cur_floor)
            startActivity(intent)
        }

        val floorSpinner: Spinner = findViewById(R.id.floor_spinner)
        // 층 정보를 포함하는 배열
        val floors = arrayOf("1층", "2층", "3층", "4층", "5층")
        // ArrayAdapter를 사용하여 Spinner에 데이터 설정
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, floors)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        floorSpinner.adapter = adapter

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 여기서 setTestbed 함수 호출
                change_floor(cur_floor)
                mHandler.postDelayed(Runnable {
                    webView.loadUrl("javascript:rotateArrow(0)")
                }, 500)
            }
        }

        // 항목 선택 리스너 설정
        floorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // 선택된 층 정보 처리
                val selectedFloor = floors[position]
                // 예: 선택된 층 정보를 사용하여 지도 이미지 변경 등의 작업 수행

                when (selectedFloor) {
                    "B3층" -> change_floor("B3")
                    "B2층" -> change_floor("B2")
                    "B1층" -> change_floor("B1")
                    "0층" -> change_floor("0")
                    "1층" -> change_floor("1")
                    "2층" -> change_floor("2")
                    "3층" -> change_floor("3")
                    "4층" -> change_floor("4")
                    "5층" -> change_floor("5")
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // 아무 것도 선택되지 않았을 때의 처리
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if(event.getAction()== MotionEvent.ACTION_UP){
            webView.evaluateJavascript("getClickedPosition()") {
                    value ->
                val pos_arr = value.replace("\"", "").split("\\t")
                input_pos_x.setText((round(pos_arr[0].toFloat()*100) /100).toString())
                input_pos_y.setText((round(pos_arr[1].toFloat()*100) /100).toString())
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private fun webViewSetting(html_file_name: String) {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }
        }
        // 캐시와 히스토리를 클리어합니다.
        webView.clearCache(true)
        webView.clearHistory()
        webView.clearFormData()

        webView.loadUrl(html_file_name)
        webView.scrollTo(1690, 480)
        webView.isScrollbarFadingEnabled = true
        webView.setInitialScale(160)

        val webSettings = webView.settings
        webSettings.allowFileAccessFromFileURLs = true
        webSettings.allowUniversalAccessFromFileURLs = true
        webSettings.useWideViewPort = true
        webSettings.builtInZoomControls = true
        webSettings.javaScriptEnabled = true
        webSettings.javaScriptCanOpenWindowsAutomatically = false
        webSettings.setSupportMultipleWindows(false)
        webSettings.setSupportZoom(true)
        webSettings.domStorageEnabled = true
    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME)
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME)
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME)
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_GAME)
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_GAME)
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor != null) {
        }
    }

    fun change_floor(floor : String){
        cur_floor = floor
        mHandler.postDelayed(Runnable {
            webView.loadUrl("javascript:setTestbed('hansando', floor='${floor}', mode='setting')")
        }, 100)
    }
}