package com.example.testapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class CollectionModeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection_mode)

        val buttonPressure = findViewById<Button>(R.id.button_pressure)
        val buttonCompass = findViewById<Button>(R.id.button_compass)
        val buttonGyro = findViewById<Button>(R.id.button_gyro)
        val buttonPDR = findViewById<Button>(R.id.button_pdr)

        buttonPressure.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        buttonCompass.setOnClickListener {
            val intent = Intent(this, CompassActivity::class.java)
            startActivity(intent)
        }

        buttonGyro.setOnClickListener {
            val intent = Intent(this, GyroActivity::class.java)
            startActivity(intent)
        }

        buttonPDR.setOnClickListener {
            val intent = Intent(this, SettingActivity2::class.java)
            startActivity(intent)
        }
    }
}
