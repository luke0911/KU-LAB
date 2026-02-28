package com.example.zeromaprefactoring.ui.incident

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ScrollView
import android.widget.TextView
import com.example.zeromaprefactoring.R
import com.example.zeromaprefactoring.ui.base.BaseActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class IncidentActivity : BaseActivity() {

    // 상단 버튼들
    private lateinit var alertButtons: List<MaterialButton>
    private lateinit var menuDateFilterLayout: TextInputLayout
    private lateinit var scrollViewAlertList: ScrollView

    // 중간 액션 버튼
    private lateinit var btnSubmitReport: MaterialButton
    private lateinit var btnReportIncident: MaterialButton

    // --- (수정) ScrollView 참조 추가 ---
    private lateinit var formScrollView: ScrollView // '신고하기' 폼

    // ▼▼▼ [신규] '보고하기' 버튼 토글 상태 추적 ▼▼▼
    private var isReporting: Boolean = false
    // ▲▲▲ [신규] 여기까지 ▲▲▲

    // ▼▼▼ [수정] '보고하기' 폼 관련 뷰 ▼▼▼
    private lateinit var reportFormScrollView: ScrollView // '보고하기' 폼
    private lateinit var textToggleReportFormType: TextView
    private lateinit var textSelectedReportFormType: TextView
    private lateinit var layoutReportFormSubMenus: View
    private lateinit var btnReportTypeDanger: MaterialButton
    private lateinit var btnReportTypeDefect: MaterialButton
    private lateinit var layoutReportFormDetails: View
    private var selectedReportType: MaterialButton? = null
    // (보고하기 폼 내부의 '추가내용' 등은 필요시 추가로 선언)
    // private lateinit var inputAdditionalInfo_Report: TextInputEditText
    // ▲▲▲ [수정] 여기까지 ▲▲▲


    // ▼▼▼ [신규 추가] 구조 요청 관련 뷰 ▼▼▼
    private lateinit var scrollViewRescueRequest: ScrollView
    private lateinit var btnSubmitRescue: MaterialButton // XML에 원래 있던 버튼
    private lateinit var btnCompleteRescue: MaterialButton // XML에 새로 추가한 버튼
    private var rescueTimer: CountDownTimer? = null
    // ▲▲▲ [신규 추가] 여기까지 ▲▲▲

    // '신고하기' 폼 관련 뷰
    private lateinit var layoutReportIncident: View
    private lateinit var textToggleReportType: TextView
    private lateinit var textSelectedReportType: TextView
    private lateinit var layoutReportSubMenus: View

    private lateinit var layoutEventType: View
    private lateinit var dropdownEventType: AutoCompleteTextView
    private lateinit var inputLayoutOtherEventType: TextInputLayout
    private lateinit var inputOtherEventType: TextInputEditText

    private lateinit var layoutAccidentType: View
    private lateinit var dropdownAccidentType: AutoCompleteTextView
    private lateinit var inputLayoutOtherAccidentType: TextInputLayout
    private lateinit var inputOtherAccidentType: TextInputEditText

    // '신고하기' 추가 내용 및 사진 첨부 UI
    private lateinit var layoutReportDetails: View

    private val strokeWidthPx by lazy { (1 * resources.displayMetrics.density).toInt() }
    private val activeBlueColor = Color.parseColor("#0000FF")
    private val inactiveStrokeColor by lazy { ColorStateList.valueOf(Color.parseColor("#DDDDDD")) }

    private lateinit var username: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incident)
        setupBottomNavigation(IncidentActivity::class.java)

        // --- 뷰 찾기 ---
        val btnAlertEvent = findViewById<MaterialButton>(R.id.btnAlertEvent)
        val btnAlertAccident = findViewById<MaterialButton>(R.id.btnAlertAccident)
        val btnAlertRescue = findViewById<MaterialButton>(R.id.btnAlertRescue)
        val btnAlertReport = findViewById<MaterialButton>(R.id.btnAlertReport)
        val btnDateFilter = findViewById<AutoCompleteTextView>(R.id.btnDateFilter)

        //$$$$$$$$$$$$$$$$$$$$$$$$
        username = findViewById(R.id.txtUserNameTop)
        val pref = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE) // SharedPreferences 객체 가져오기
        username.text = pref.getString("USER_NAME", "알수없음")
        //$$$$$$$$$$$$$$$$$$$$$$$$

        menuDateFilterLayout = findViewById(R.id.menuDateFilterLayout)
        scrollViewAlertList = findViewById(R.id.scrollViewAlertList)

        btnSubmitReport = findViewById(R.id.btnSubmitReport)
        btnReportIncident = findViewById(R.id.btnReportIncident)

        // '신고하기' 폼
        formScrollView = findViewById(R.id.formScrollView)

        // ▼▼▼ [수정] '보고하기' 폼 뷰 찾기 ▼▼▼
        reportFormScrollView = findViewById(R.id.reportFormScrollView)
        textToggleReportFormType = findViewById(R.id.textToggleReportFormType)
        textSelectedReportFormType = findViewById(R.id.textSelectedReportFormType)
        layoutReportFormSubMenus = findViewById(R.id.layoutReportFormSubMenus)
        btnReportTypeDanger = findViewById(R.id.btnReportTypeDanger)
        btnReportTypeDefect = findViewById(R.id.btnReportTypeDefect)
        layoutReportFormDetails = findViewById(R.id.layoutReportFormDetails)
        // ▲▲▲ [수정] 여기까지 ▲▲▲

        // ▼▼▼ [신규 추가] 구조 요청 뷰 찾기 ▼▼▼
        scrollViewRescueRequest = findViewById(R.id.scrollViewRescueRequest)
        btnSubmitRescue = findViewById(R.id.btnSubmitRescue) // 상단 액션 버튼
        btnCompleteRescue = findViewById(R.id.btnCompleteRescue) // 스크롤뷰 내부 완료 버튼
        // ▲▲▲ [신규 추가] 여기까지 ▲▲▲

        // '신고하기' 폼 ScrollView 내부 뷰
        layoutReportIncident = findViewById(R.id.layoutReportIncident)
        textToggleReportType = findViewById(R.id.textToggleReportType)
        textSelectedReportType = findViewById(R.id.textSelectedReportType)
        layoutReportSubMenus = findViewById(R.id.layoutReportSubMenus)

        layoutEventType = findViewById(R.id.layoutEventType)
        dropdownEventType = findViewById(R.id.dropdownEventType)
        inputLayoutOtherEventType = findViewById(R.id.inputLayoutOtherEventType)
        inputOtherEventType = findViewById(R.id.inputOtherEventType)

        layoutAccidentType = findViewById(R.id.layoutAccidentType)
        dropdownAccidentType = findViewById(R.id.dropdownAccidentType)
        inputLayoutOtherAccidentType = findViewById(R.id.inputLayoutOtherAccidentType)
        inputOtherAccidentType = findViewById(R.id.inputOtherAccidentType)

        layoutReportDetails = findViewById(R.id.layoutReportDetails)

        alertButtons = listOf(btnAlertEvent, btnAlertAccident, btnAlertRescue, btnAlertReport)

        // --- 드롭다운 세팅 ---
        setupDropdownMenu(btnDateFilter, listOf("1일 기준", "1주 기준", "1달 기준"))
        // '신고하기' 폼 드롭다운
        setupDropdownMenu(dropdownEventType, listOf("선택", "화재", "폭발", "감전", "가스 유출", "기계오작동", "기타..."))
        setupDropdownMenu(dropdownAccidentType, listOf("선택", "낙상", "끼임", "충돌", "기타..."))


        // --- '신고하기' 폼 : 신고 종류 토글 ---
        textToggleReportType.setOnClickListener {
            if (layoutReportSubMenus.visibility == View.VISIBLE) {
                layoutReportSubMenus.visibility = View.GONE
                textToggleReportType.text = "▼ 신고 종류"
            } else {
                layoutReportSubMenus.visibility = View.VISIBLE
                textToggleReportType.text = "▲ 신고 종류"
            }
        }

        // ▼▼▼ [신규] '보고하기' 폼 : 보고 종류 토글 ▼▼▼
        textToggleReportFormType.setOnClickListener {
            if (layoutReportFormSubMenus.visibility == View.VISIBLE) {
                layoutReportFormSubMenus.visibility = View.GONE
                textToggleReportFormType.text = "▼ 보고 종류"
            } else {
                layoutReportFormSubMenus.visibility = View.VISIBLE
                textToggleReportFormType.text = "▲ 보고 종류"
            }
        }
        // ▲▲▲ [신규] 여기까지 ▲▲▲

        // --- '신고하기' 폼 : 드롭다운 및 기타 리스너 ---
        addReportTypeListeners()

        // --- 상단 버튼 ---
        alertButtons.forEach { it.setOnClickListener { btn -> onAlertCategoryClick(btn as MaterialButton) } }

        // --- 액션 버튼 ---
        btnSubmitReport.setOnClickListener { onActionClick(it as MaterialButton) }
        btnReportIncident.setOnClickListener { onActionClick(it as MaterialButton) }


        // ▼▼▼ [신규] '보고하기' 폼 : 하위 메뉴 버튼 리스너 ▼▼▼
        btnReportTypeDanger.setOnClickListener { onReportTypeClick(it as MaterialButton) }
        btnReportTypeDefect.setOnClickListener { onReportTypeClick(it as MaterialButton) }
        // ▲▲▲ [신규] 여기까지 ▲▲▲

        // ▼▼▼ [신규 추가] 구조 요청 버튼 리스너 ▼▼▼
        btnSubmitRescue.setOnClickListener { onRescueClick() }

        // (구조 요청 완료 버튼 리스너 - 필요시 추가)
        btnCompleteRescue.setOnClickListener {
            rescueTimer?.cancel()
            btnCompleteRescue.text = "구조 요청 완료됨"
        }
        // ▲▲▲ [신규 추가] 여기까지 ▲▲▲

        // 초기 상태 설정
        onAlertCategoryClick(btnAlertEvent)
    }

    private fun setupDropdownMenu(view: AutoCompleteTextView, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        view.setAdapter(adapter)
    }

    // '신고하기' 폼 리스너
    private fun addReportTypeListeners() {
        // 사건 종류 선택 시
        dropdownEventType.setOnItemClickListener { _, _, position, _ ->
            val selected = dropdownEventType.adapter.getItem(position).toString()
            handleSelection(selected, isEvent = true)
        }

        // 사고 종류 선택 시
        dropdownAccidentType.setOnItemClickListener { _, _, position, _ ->
            val selected = dropdownAccidentType.adapter.getItem(position).toString()
            handleSelection(selected, isEvent = false)
        }

        // 기타 입력 실시간 표시
        inputOtherEventType.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateSelectedReportText(s.toString()) // '신고하기' 폼의 선택된 텍스트 업데이트
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        inputOtherAccidentType.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateSelectedReportText(s.toString()) // '신고하기' 폼의 선택된 텍스트 업데이트
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // “완료(Enter)” 누르면 드롭다운 자동 닫힘 추가
        inputOtherEventType.setOnEditorActionListener { v, actionId, _ ->
            val text = v.text.toString().trim()
            if (text.isNotEmpty()) {
                closeDropdownAndShowDetails(text) // '신고하기' 폼의 드롭다운 닫기
                true
            } else {
                false
            }
        }

        inputOtherAccidentType.setOnEditorActionListener { v, actionId, _ ->
            val text = v.text.toString().trim()
            if (text.isNotEmpty()) {
                closeDropdownAndShowDetails(text) // '신고하기' 폼의 드롭다운 닫기
                true
            } else {
                false
            }
        }
    }

    // '신고하기' 폼 핸들러
    private fun handleSelection(selected: String, isEvent: Boolean) {
        if (isEvent) {
            // 사건 선택
            dropdownAccidentType.setText("선택", false)
            inputLayoutOtherAccidentType.visibility = View.GONE
            inputOtherAccidentType.text = null

            if (selected == "기타...") {
                inputLayoutOtherEventType.visibility = View.VISIBLE
            } else {
                inputLayoutOtherEventType.visibility = View.GONE
                inputOtherEventType.text = null
                if (selected != "선택") closeDropdownAndShowDetails(selected)
            }
        } else {
            // 사고 선택
            dropdownEventType.setText("선택", false)
            inputLayoutOtherEventType.visibility = View.GONE
            inputOtherEventType.text = null

            if (selected == "기타...") {
                inputLayoutOtherAccidentType.visibility = View.VISIBLE
            } else {
                inputLayoutOtherAccidentType.visibility = View.GONE
                inputOtherAccidentType.text = null
                if (selected != "선택") closeDropdownAndShowDetails(selected)
            }
        }
    }

    // '신고하기' 폼 핸들러
    private fun closeDropdownAndShowDetails(selected: String) {
        layoutReportSubMenus.visibility = View.GONE
        textToggleReportType.text = "▼ 신고 종류"
        updateSelectedReportText(selected)
    }

    // '신고하기' 폼 핸들러
    private fun updateSelectedReportText(text: String?) {
        if (text.isNullOrEmpty() || text == "선택" || text == "기타...") {
            textSelectedReportType.visibility = View.GONE
            layoutReportDetails.visibility = View.GONE
        } else {
            textSelectedReportType.text = text
            textSelectedReportType.visibility = View.VISIBLE
            layoutReportDetails.visibility = View.VISIBLE
        }
    }

    // 버튼 클릭 처리들 ----------------------------------------------------

    // ▼▼▼ [신규] '보고하기' 폼 : 하위 메뉴 버튼 클릭 핸들러 ▼▼▼
    private fun onReportTypeClick(clicked: MaterialButton) {
        // 1. 이전에 선택된 버튼이 있으면 리셋
        selectedReportType?.let {
            it.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            it.setTextColor(Color.BLACK)
            it.strokeColor = inactiveStrokeColor
            it.strokeWidth = strokeWidthPx
        }

        // 2. 새로 선택
        if (selectedReportType == clicked) {
            // 이미 선택된 버튼을 다시 클릭 -> 선택 해제
            selectedReportType = null
            textSelectedReportFormType.visibility = View.GONE
            layoutReportFormDetails.visibility = View.GONE
        } else {
            // 새 버튼 클릭
            clicked.backgroundTintList = ColorStateList.valueOf(activeBlueColor)
            clicked.setTextColor(Color.WHITE)
            clicked.strokeWidth = 0
            selectedReportType = clicked

            // 선택된 텍스트 업데이트 및 세부 폼 보이기
            textSelectedReportFormType.text = clicked.text
            textSelectedReportFormType.visibility = View.VISIBLE
            layoutReportFormDetails.visibility = View.VISIBLE

            // 하위 메뉴 숨기기
            layoutReportFormSubMenus.visibility = View.GONE
            textToggleReportFormType.text = "▼ 보고 종류"
        }
    }
    // ▲▲▲ [신규] 여기까지 ▲▲▲

    // ▼▼▼ [신규 추가] 구조 요청 버튼 클릭 핸들러 ▼▼▼
    private fun onRescueClick() {
        // 1. 다른 뷰들 숨기기
        menuDateFilterLayout.visibility = View.GONE
        scrollViewAlertList.visibility = View.GONE
        formScrollView.visibility = View.GONE
        reportFormScrollView.visibility = View.GONE // [추가] '보고하기' 폼 숨기기

        // 2. 다른 버튼들 초기화
        resetAlertCategoryButtons()
        resetActionButtons() // "보고하기", "신고하기" 버튼을 흰색으로

        // 3. 구조 요청 뷰 보이기
        scrollViewRescueRequest.visibility = View.VISIBLE

        // 4. 타이머 시작 (기존 타이머가 있다면 취소 후 새로 시작)
        rescueTimer?.cancel()
        rescueTimer = object : CountDownTimer(30000, 1000) { // 30초
            override fun onTick(millisUntilFinished: Long) {
                btnCompleteRescue.text = "구조 요청 완료하기 (${millisUntilFinished / 1000})"
            }

            override fun onFinish() {
                btnCompleteRescue.text = "구조 요청 완료됨"
            }
        }.start()
    }
    // ▲▲▲ [신규 추가] 여기까지 ▲▲▲


    // --- (수정) '보고하기' 버튼 토글 기능 및 폼 연동 ---
    private fun onActionClick(clicked: MaterialButton) {
        // 1. 공통 뷰 숨기기
        menuDateFilterLayout.visibility = View.GONE
        scrollViewAlertList.visibility = View.GONE
        scrollViewRescueRequest.visibility = View.GONE
        rescueTimer?.cancel()

        // 2. 상단 알림 버튼(사건, 사고...)들 초기화
        resetAlertCategoryButtons()

        // 3. '보고하기' 버튼 (btnSubmitReport)을 클릭한 경우
        if (clicked.id == R.id.btnSubmitReport) {

            val currentlyReporting = isReporting // 현재 상태 저장

            // 4. 모든 액션 버튼(보고, 신고)을 기본(흰색) 상태로 리셋
            resetActionButtons()

            if (!currentlyReporting) {
                // "보고하기" -> "보고취소" (활성 상태)
                isReporting = true // 상태를 다시 true로
                clicked.text = "보고취소"
                clicked.backgroundTintList = ColorStateList.valueOf(activeBlueColor)
                clicked.setTextColor(Color.WHITE)
                clicked.strokeWidth = 0

                // ▼▼▼ [수정] '보고하기' 폼 보이기 ▼▼▼
                reportFormScrollView.visibility = View.VISIBLE
                formScrollView.visibility = View.GONE // '신고하기' 폼 숨기기
            } else {
                // "보고취소" -> "보고하기" (비활성 상태)
                // (resetActionButtons()가 이미 모든 작업을 완료함)
                // '보고하기' 폼은 resetActionButtons()에서 이미 숨겨짐
            }

        } else if (clicked.id == R.id.btnReportIncident) {
            // 5. '신고하기' 버튼 (btnReportIncident)을 클릭한 경우

            // 5-1. 모든 액션 버튼(보고, 신고) 리셋 (이때 '보고하기'도 리셋됨)
            resetActionButtons()

            // 5-2. '신고하기' 버튼 활성화 (파란색)
            clicked.backgroundTintList = ColorStateList.valueOf(activeBlueColor)
            clicked.setTextColor(Color.WHITE)
            clicked.strokeWidth = 0

            // 5-3. 폼 보이기
            formScrollView.visibility = View.VISIBLE
            reportFormScrollView.visibility = View.GONE // '보고하기' 폼 숨기기
            resetReportDropdowns() // '신고하기' 폼 리셋
        }
    }

    // --- (수정) layoutReportIncident 대신 formScrollView 제어 ---
    private fun onAlertCategoryClick(clicked: MaterialButton) {
        formScrollView.visibility = View.GONE // '신고하기' 폼 숨기기
        reportFormScrollView.visibility = View.GONE // [추가] '보고하기' 폼 숨기기
        scrollViewRescueRequest.visibility = View.GONE // [수정] 구조 요청 뷰 숨기기
        rescueTimer?.cancel() // [수정] 타이머 취소

        // '신고하기' 폼 내부 상태 초기화
        layoutReportSubMenus.visibility = View.GONE
        inputLayoutOtherEventType.visibility = View.GONE
        inputLayoutOtherAccidentType.visibility = View.GONE
        layoutReportDetails.visibility = View.GONE
        textToggleReportType.text = "▼ 신고 종류"

        // '보고하기' 폼 내부 상태 초기화 (resetActionButtons에서 호출되지만 여기서도 안전하게 호출)
        resetReportForm()

        resetActionButtons()
        resetAlertCategoryButtons()

        clicked.backgroundTintList = ColorStateList.valueOf(activeBlueColor)
        clicked.setTextColor(Color.WHITE)
        clicked.strokeWidth = 0

        menuDateFilterLayout.visibility = View.VISIBLE
        scrollViewAlertList.visibility =
            if (clicked.id == R.id.btnAlertEvent) View.VISIBLE else View.GONE

        if (clicked.id != R.id.btnAlertEvent) {
            scrollViewAlertList.visibility = View.GONE
        }
    }

    // '신고하기' 폼 리셋
    private fun resetReportDropdowns() {
        textToggleReportType.text = "▼ 신고 종류"
        layoutReportSubMenus.visibility = View.GONE
        updateSelectedReportText(null) // '신고하기' 폼 업데이트
        inputLayoutOtherEventType.visibility = View.GONE
        inputOtherEventType.text = null
        inputLayoutOtherAccidentType.visibility = View.GONE
        inputOtherAccidentType.text = null
        dropdownEventType.setText("선택", false)
        dropdownAccidentType.setText("선택", false)
    }

    // ▼▼▼ [신규] '보고하기' 폼 리셋 함수 ▼▼▼
    private fun resetReportForm() {
        textToggleReportFormType.text = "▼ 보고 종류"
        layoutReportFormSubMenus.visibility = View.GONE
        layoutReportFormDetails.visibility = View.GONE
        textSelectedReportFormType.visibility = View.GONE
        textSelectedReportFormType.text = ""

        // 선택된 버튼 리셋
        selectedReportType?.let {
            it.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            it.setTextColor(Color.BLACK)
            it.strokeColor = inactiveStrokeColor
            it.strokeWidth = strokeWidthPx
        }
        selectedReportType = null

        // (필요시 '추가내용' 등 텍스트 필드 클리어)
        // findViewById<TextInputEditText>(R.id.inputAdditionalInfo_Report).text = null
    }
    // ▲▲▲ [신규] 여기까지 ▲▲▲


    private fun resetAlertCategoryButtons() {
        alertButtons.forEach {
            it.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            it.setTextColor(Color.BLACK)
            it.strokeColor = inactiveStrokeColor
            it.strokeWidth = strokeWidthPx
        }
    }

    // ▼▼▼ [수정] '보고하기' 폼 숨기기 및 리셋 로직 추가 ▼▼▼
    private fun resetActionButtons() {
        // "구조요청" 버튼은 이 리셋 로직에서 제외
        listOf(btnSubmitReport, btnReportIncident).forEach {
            it.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            it.setTextColor(Color.BLACK)
            it.strokeColor = inactiveStrokeColor
            it.strokeWidth = strokeWidthPx
        }

        // '보고하기' 버튼 상태 및 텍스트 초기화
        if (isReporting) {
            isReporting = false
            btnSubmitReport.text = "보고하기"
        }

        // ▼▼▼ [수정] '보고하기' 폼 숨기기 및 리셋 ▼▼▼
        reportFormScrollView.visibility = View.GONE
        resetReportForm()
        // ▲▲▲ [수정] 여기까지 ▲▲▲

        // 구조요청 버튼은 항상 빨간색 기본 상태 유지
        btnSubmitRescue.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF4D6F"))
        btnSubmitRescue.setTextColor(Color.WHITE)
        btnSubmitRescue.strokeWidth = 0 // 구조요청 버튼은 테두리 없음
    }
    // ▲▲▲ [수정] 여기까지 ▲▲▲

    // ▼▼▼ [추가] 액티비티 종료 시 타이머 취소 ▼▼▼
    override fun onDestroy() {
        super.onDestroy()
        rescueTimer?.cancel() // 메모리 누수 방지
    }
    // ▲▲▲ [추가] 여기까지 ▲▲▲
}