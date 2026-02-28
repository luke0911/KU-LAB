package com.example.zeromaprefactoring.ui.chart

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.example.zeromaprefactoring.R
import com.example.zeromaprefactoring.ui.base.BaseActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class ChartActivity : BaseActivity() {

    // 탭 UI
    private lateinit var tabOverall: LinearLayout
    private lateinit var tvTabOverall: TextView
    private lateinit var viewTabOverallLine: View
    private lateinit var tabMyStats: LinearLayout
    private lateinit var tvTabMyStats: TextView
    private lateinit var viewTabMyStatsLine: View

    // 스크롤 뷰 (컨텐츠 영역)
    private lateinit var scrollViewOverallStats: ScrollView
    private lateinit var scrollViewMyStats: ScrollView

    // "전체 통계" 차트
    private lateinit var pieChartWorkStatus: PieChart
    private lateinit var pieChartSafetyMeeting: PieChart
    private lateinit var pieChartWorkerAction: PieChart

    // "내 통계" 차트
    private lateinit var pieChartMyMoveStatus: PieChart
    private lateinit var pieChartMyWorkTime: PieChart
    private lateinit var pieChartMyWorkStatus: PieChart
    private lateinit var pieChartMySafetyMeeting: PieChart

    //유저이름
    private lateinit var username: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        // BaseActivity의 공통 네비게이션 설정 함수 호출
        setupBottomNavigation(ChartActivity::class.java)

        // 뷰 초기화
        findViews()

        // 탭 클릭 리스너 설정
        setupTabClickListeners()

        // "전체 통계" 탭을 기본으로 선택
        selectTab(isOverallSelected = true)

        // 모든 차트 설정
        setupAllCharts()
    }

    /**
     * XML 레이아웃의 뷰들을 ID로 찾아 변수에 할당합니다.
     */
    private fun findViews() {
        // 탭
        tabOverall = findViewById(R.id.tabOverall)
        tvTabOverall = findViewById(R.id.tvTabOverall)
        viewTabOverallLine = findViewById(R.id.viewTabOverallLine)
        tabMyStats = findViewById(R.id.tabMyStats)
        tvTabMyStats = findViewById(R.id.tvTabMyStats)
        viewTabMyStatsLine = findViewById(R.id.viewTabMyStatsLine)

        // 컨텐츠
        scrollViewOverallStats = findViewById(R.id.scrollViewOverallStats)
        scrollViewMyStats = findViewById(R.id.scrollViewMyStats)

        // 전체 통계 차트
        pieChartWorkStatus = findViewById(R.id.pieChart_work_status)
        pieChartSafetyMeeting = findViewById(R.id.pieChart_safety_meeting)
        pieChartWorkerAction = findViewById(R.id.pieChart_worker_action)

        // 내 통계 차트
        pieChartMyMoveStatus = findViewById(R.id.pieChart_my_move_status)
        pieChartMyWorkTime = findViewById(R.id.pieChart_my_work_time)
        pieChartMyWorkStatus = findViewById(R.id.pieChart_my_work_status)
        pieChartMySafetyMeeting = findViewById(R.id.pieChart_my_safety_meeting)


        //$$$$$$$$$$$$$$$$$$$$$$$$
        username = findViewById(R.id.txtUserNameTop)
        val pref = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE) // SharedPreferences 객체 가져오기
        username.text = pref.getString("USER_NAME", "알수없음")
        //$$$$$$$$$$$$$$$$$$$$$$$$
    }

    /**
     * "전체 통계" / "내 통계" 탭에 클릭 리스너를 설정합니다.
     */
    private fun setupTabClickListeners() {
        tabOverall.setOnClickListener {
            selectTab(isOverallSelected = true)
        }
        tabMyStats.setOnClickListener {
            selectTab(isOverallSelected = false)
        }
    }

    /**
     * 탭 선택 상태에 따라 UI(글꼴, 밑줄, 컨텐츠)를 변경합니다.
     */
    private fun selectTab(isOverallSelected: Boolean) {
        if (isOverallSelected) {
            // "전체 통계" 탭 활성화
            tvTabOverall.setTextColor(Color.BLACK)
            tvTabOverall.setTypeface(null, Typeface.BOLD)
            viewTabOverallLine.visibility = View.VISIBLE

            // "내 통계" 탭 비활성화
            tvTabMyStats.setTextColor(Color.GRAY)
            tvTabMyStats.setTypeface(null, Typeface.NORMAL)
            viewTabMyStatsLine.visibility = View.INVISIBLE

            // 컨텐츠 표시
            scrollViewOverallStats.visibility = View.VISIBLE
            scrollViewMyStats.visibility = View.GONE
        } else {
            // "전체 통계" 탭 비활성화
            tvTabOverall.setTextColor(Color.GRAY)
            tvTabOverall.setTypeface(null, Typeface.NORMAL)
            viewTabOverallLine.visibility = View.INVISIBLE

            // "내 통계" 탭 활성화
            tvTabMyStats.setTextColor(Color.BLACK)
            tvTabMyStats.setTypeface(null, Typeface.BOLD)
            viewTabMyStatsLine.visibility = View.VISIBLE

            // 컨텐츠 표시
            scrollViewOverallStats.visibility = View.GONE
            scrollViewMyStats.visibility = View.VISIBLE
        }
    }

    /**
     * 모든 차트를 초기화하고 데이터를 설정합니다.
     */
    private fun setupAllCharts() {
        // --- "전체 통계" 차트 설정 ---
        // (색상: Green, Black)
        setupPieChart(pieChartWorkStatus, listOf(80f, 20f), listOf("#2ECC71", "#2C3E50"))
        // (색상: Blue, Red)
        setupPieChart(pieChartSafetyMeeting, listOf(70f, 30f), listOf("#3498DB", "#E74C3C"))
        // (색상: Green, Red, Yellow)
        setupPieChart(pieChartWorkerAction, listOf(60f, 10f, 30f), listOf("#2ECC71", "#E74C3C", "#F39C12"))

        // --- "내 통계" 차트 설정 ---
        // (색상: Green, Red, Yellow)
        setupPieChart(pieChartMyMoveStatus, listOf(50f, 20f, 30f), listOf("#2ECC71", "#E74C3C", "#F39C12"))
        // (색상: Green, Purple, Black)
        setupPieChart(pieChartMyWorkTime, listOf(70f, 10f, 20f), listOf("#2ECC71", "#9B59B6", "#2C3E50"))
        // (색상: Green, Black)
        setupPieChart(pieChartMyWorkStatus, listOf(90f, 10f), listOf("#2ECC71", "#2C3E50"))
        // (색상: Blue, Red)
        setupPieChart(pieChartMySafetyMeeting, listOf(60f, 40f), listOf("#3498DB", "#E74C3C"))
    }

    /**
     * 파이 차트의 공통 설정 및 데이터 적용을 처리하는 헬퍼 함수
     * @param chart 설정할 PieChart 뷰
     * @param dataValues 차트에 표시할 데이터 값 리스트 (예: 60f, 40f)
     * @param colorHexStrings 차트 조각에 적용할 16진수 색상 **문자열** 리스트
     */
    private fun setupPieChart(chart: PieChart, dataValues: List<Float>, colorHexStrings: List<String>) {
        // 1. 데이터 항목 생성 (PieEntry)
        val entries = ArrayList<PieEntry>()
        for (value in dataValues) {
            entries.add(PieEntry(value))
        }

        // 2. 데이터 세트 생성 (PieDataSet)
        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawValues(false) // 차트 위에 값(숫자) 표시 안 함

        // 3. 색상 설정
        val colors = ArrayList<Int>()
        for (colorString in colorHexStrings) {
            colors.add(Color.parseColor(colorString))
        }
        dataSet.colors = colors

        // 4. 데이터 객체 생성 (PieData)
        val data = PieData(dataSet)

        // 5. 차트 속성 설정
        chart.data = data
        chart.description.isEnabled = false // 차트 설명 텍스트 (우측 하단)
        chart.legend.isEnabled = false      // 범례 (색상/이름 표시)
        chart.isRotationEnabled = false     // 차트 회전 비활성화
        chart.isDrawHoleEnabled = true      // 가운데 구멍(도넛 모양) 활성화
        chart.holeRadius = 65f              // 구멍 반지름 (기본 50f)
        chart.transparentCircleRadius = 0f  // 구멍 주변 반투명 원 제거
        chart.setTouchEnabled(false)        // 차트 터치 비활성화

        // 6. 차트 갱신
        chart.invalidate()
    }
}