package com.example.zeromaprefactoring.ui.main

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import com.example.zeromaprefactoring.R
import com.example.zeromaprefactoring.ui.base.BaseActivity
import com.google.android.material.button.MaterialButton

class MainPage : BaseActivity() {

    private lateinit var usernameText: TextView
    private lateinit var workingBtn: MaterialButton
    private lateinit var outingBtn: MaterialButton
    private lateinit var offBtn: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainpage_v2)

        setupBottomNavigation(MainPage::class.java)

        initViews()
        loadUserInfo()
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
    }

    private fun setupDutyButtons() {
        // TODO: 근무 상태 관리 로직 추가
        // 지금은 기본 상태만 설정
        setActiveDutyButton(workingBtn)
    }

    private fun setActiveDutyButton(activeButton: MaterialButton) {
        val buttons = listOf(workingBtn, outingBtn, offBtn)
        buttons.forEach { button ->
            val isActive = button == activeButton
            button.isEnabled = isActive
            button.alpha = if (isActive) 1f else 0.35f
        }
    }
}
