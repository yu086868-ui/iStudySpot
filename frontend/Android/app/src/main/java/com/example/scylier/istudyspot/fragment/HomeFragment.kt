package com.example.scylier.istudyspot.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.adapters.FunctionItemAdapter
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.FunctionItem
import com.example.scylier.istudyspot.network.ApiManager
import com.example.scylier.istudyspot.network.ErrorHandler
import com.example.scylier.istudyspot.viewmodel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var token: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        // 获取token
        token = arguments?.getString("token", "") ?: ""
        // 初始化功能项组件
        initFunctionItems(view)
    }

    private fun initFunctionItems(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.features_recycler)
        
        // 设置布局管理器，4列网格
        val layoutManager = GridLayoutManager(requireContext(), 4)
        recyclerView.layoutManager = layoutManager
        
        // 创建并设置适配器
        val adapter = FunctionItemAdapter(viewModel.mainFeatures) { item ->
            handleFunctionItemClick(item)
        }
        recyclerView.adapter = adapter
    }

    private fun handleFunctionItemClick(item: FunctionItem) {
        when (item.id) {
            "booking" -> {
                // 预约座位 - 跳转到自习室列表页面
                val studyRoomFragment = StudyRoomFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, studyRoomFragment)
                    .addToBackStack(null)
                    .commit()
            }
            "checkin" -> {
                // 签到 - 跳转到订单列表页面（从订单中签到）
                val orderListFragment = OrderListFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, orderListFragment)
                    .addToBackStack(null)
                    .commit()
                Toast.makeText(requireContext(), "请在订单详情中签到", Toast.LENGTH_SHORT).show()
            }
            "guide" -> {
                // 场馆导览 - 跳转到场馆导览页面
                val guideFragment = GuideFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, guideFragment)
                    .addToBackStack(null)
                    .commit()
            }
            "my_booking" -> {
                // 我的预约 - 跳转到订单列表页面
                val orderListFragment = OrderListFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, orderListFragment)
                    .addToBackStack(null)
                    .commit()
            }
            "study_record" -> {
                // 学习记录 - 跳转到学习记录页面
                val studyRecordFragment = StudyRecordFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, studyRecordFragment)
                    .addToBackStack(null)
                    .commit()
            }
            "team_booking" -> {
                // 团队预约 - 团队预约功能
                Toast.makeText(requireContext(), "团队预约功能开发中", Toast.LENGTH_SHORT).show()
            }
            "notification" -> {
                // 通知提醒 - 跳转到通知提醒页面
                val notificationFragment = NotificationFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, notificationFragment)
                    .addToBackStack(null)
                    .commit()
            }
            "settings" -> {
                // 偏好设置 - 跳转到更多页面
                val moreFragment = MoreFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, moreFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun getStudyRooms() {
        CoroutineScope(Dispatchers.Main).launch {
            val apiManager = ApiManager(token = token, context = requireContext())
            val response = apiManager.getStudyRooms()

            when (response) {
                is ApiResponse.Success -> {
                    val studyRooms = response.data.list
                    Toast.makeText(requireContext(), "获取到${studyRooms.size}个自习室", Toast.LENGTH_SHORT).show()
                    // 这里可以跳转到自习室列表页面
                }
                is ApiResponse.Error -> {
                    val errorMessage = ErrorHandler.getErrorMessage(response)
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getUserOrders() {
        CoroutineScope(Dispatchers.Main).launch {
            val apiManager = ApiManager(token = token, context = requireContext())
            val response = apiManager.getUserOrders()

            when (response) {
                is ApiResponse.Success -> {
                    val orders = response.data.list
                    Toast.makeText(requireContext(), "获取到${orders.size}个订单", Toast.LENGTH_SHORT).show()
                    // 这里可以跳转到订单列表页面
                }
                is ApiResponse.Error -> {
                    val errorMessage = ErrorHandler.getErrorMessage(response)
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
