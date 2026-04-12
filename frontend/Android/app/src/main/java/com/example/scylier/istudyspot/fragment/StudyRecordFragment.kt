package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.viewmodel.StudyRecordViewModel

/**
 * 学习记录Fragment
 * 展示用户的学习统计数据，包括学习时长、预约次数、连续学习天数等
 */
class StudyRecordFragment : Fragment() {

    private lateinit var viewModel: StudyRecordViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_study_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[StudyRecordViewModel::class.java]
        
        initStatistics(view)
    }

    private fun initStatistics(view: View) {
        // 本周学习时长
        view.findViewById<TextView>(R.id.tv_week_hours).text = 
            "${viewModel.weekStudyHours}小时"
        
        // 本月学习时长
        view.findViewById<TextView>(R.id.tv_month_hours).text = 
            "${viewModel.monthStudyHours}小时"
        
        // 累计学习时长
        view.findViewById<TextView>(R.id.tv_total_hours).text = 
            "${viewModel.totalStudyHours}小时"
        
        // 累计预约次数
        view.findViewById<TextView>(R.id.tv_total_bookings).text = 
            "${viewModel.totalBookings}次"
        
        // 连续学习天数
        view.findViewById<TextView>(R.id.tv_streak_days).text = 
            "${viewModel.streakDays}天"
        
        // 平均每次学习时长
        view.findViewById<TextView>(R.id.tv_avg_duration).text = 
            "${viewModel.avgStudyDuration}小时"
        
        // 最喜欢的座位
        view.findViewById<TextView>(R.id.tv_favorite_seat).text = 
            viewModel.favoriteSeat
        
        // 最常去的时间段
        view.findViewById<TextView>(R.id.tv_peak_time).text = 
            viewModel.peakTime
    }
}
