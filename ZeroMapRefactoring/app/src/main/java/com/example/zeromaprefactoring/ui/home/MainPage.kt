package com.example.zeromaprefactoring.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.zeromaprefactoring.R
import com.example.zeromaprefactoring.network.worker.WorkerInfoReporter
import com.example.zeromaprefactoring.ui.base.BaseActivity
import com.google.android.material.button.MaterialButton

class MainPage : BaseActivity() {

    private lateinit var usernameText: TextView
    private lateinit var workingBtn: MaterialButton
    private lateinit var outingBtn: MaterialButton
    private lateinit var offBtn: MaterialButton

    private var currentDutyState: DutyState = DutyState.OFF_DUTY

    private enum class DutyState {
        WORKING, OUTING, OFF_DUTY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainpage_v2)

        setupBottomNavigation(MainPage::class.java)

        initViews()
        loadUserInfo()
        loadDutyState()
        setupDutyButtons()
    }

    private fun initViews() {
        usernameText = findViewById(R.id.txtUserNameTop)
        workingBtn = findViewById(R.id.work_btn)
        outingBtn = findViewById(R.id.outing_btn)
        offBtn = findViewById(R.id.off_btn)
    }

    private fun loadUserInfo() {
        val pref = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)
        val userName = pref.getString("USER_NAME", "알 수 없음")
        usernameText.text = userName
        Log.d(TAG, "User name loaded: $userName")
    }

    private fun loadDutyState() {
        val pref = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)
        val savedState = pref.getString("DUTY_STATE", DutyState.OFF_DUTY.name)
        currentDutyState = try {
            DutyState.valueOf(savedState ?: DutyState.OFF_DUTY.name)
        } catch (e: Exception) {
            DutyState.OFF_DUTY
        }
    }

    private fun saveDutyState(state: DutyState) {
        val pref = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)
        pref.edit().putString("DUTY_STATE", state.name).apply()
        currentDutyState = state
    }

    private fun setupDutyButtons() {
        workingBtn.setOnClickListener { onDutyButtonClick(DutyState.WORKING) }
        outingBtn.setOnClickListener { onDutyButtonClick(DutyState.OUTING) }
        offBtn.setOnClickListener { onDutyButtonClick(DutyState.OFF_DUTY) }

        applyDutyStateUI(currentDutyState)
    }

    private fun onDutyButtonClick(newState: DutyState) {
        when (newState) {
            DutyState.WORKING -> {
                WorkerInfoReporter.sendEntryTime(this)
                Toast.makeText(this, "출근 처리되었습니다", Toast.LENGTH_SHORT).show()
            }
            DutyState.OUTING -> {
                WorkerInfoReporter.recordOutingStart(this)
                Toast.makeText(this, "외출 처리되었습니다", Toast.LENGTH_SHORT).show()
            }
            DutyState.OFF_DUTY -> {
                WorkerInfoReporter.sendExitTime(this)
                Toast.makeText(this, "퇴근 처리되었습니다", Toast.LENGTH_SHORT).show()
            }
        }

        saveDutyState(newState)
        applyDutyStateUI(newState)
    }

    private fun applyDutyStateUI(state: DutyState) {
        val activeButton = when (state) {
            DutyState.WORKING -> workingBtn
            DutyState.OUTING -> outingBtn
            DutyState.OFF_DUTY -> offBtn
        }
        setActiveDutyButton(activeButton)
    }

    private fun setActiveDutyButton(activeButton: MaterialButton) {
        val buttons = listOf(workingBtn, outingBtn, offBtn)
        buttons.forEach { button ->
            val isActive = button == activeButton
            button.isEnabled = isActive
            button.alpha = if (isActive) 1f else 0.35f
        }
    }

    companion object {
        private const val TAG = "MainPage"
    }
}
