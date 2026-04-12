package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.scylier.istudyspot.BuildConfig
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.utils.ConfigManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookingFragment : Fragment() {
    private lateinit var tvStudyRoomName: TextView
    private lateinit var tvSeatPosition: TextView
    private lateinit var etStartTime: EditText
    private lateinit var etEndTime: EditText
    private lateinit var etBookingType: EditText
    private lateinit var btnSubmit: Button
    private lateinit var repository: MainRepository
    private var seatId: String? = null
    private var studyRoomId: String? = null
    private var studyRoomName: String? = null
    private var seatPosition: String? = null
    private var pricePerHour: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_booking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvStudyRoomName = view.findViewById(R.id.tv_study_room_name)
        tvSeatPosition = view.findViewById(R.id.tv_seat_position)
        etStartTime = view.findViewById(R.id.et_start_time)
        etEndTime = view.findViewById(R.id.et_end_time)
        etBookingType = view.findViewById(R.id.et_booking_type)
        btnSubmit = view.findViewById(R.id.btn_submit)
        repository = MainRepository(requireContext())

        // 获取传递的参数
        seatId = arguments?.getString("seatId")
        studyRoomId = arguments?.getString("studyRoomId")
        studyRoomName = arguments?.getString("studyRoomName")
        seatPosition = arguments?.getString("seatPosition")
        pricePerHour = arguments?.getDouble("pricePerHour", 0.0) ?: 0.0

        tvStudyRoomName.text = studyRoomName
        tvSeatPosition.text = seatPosition

        // 设置默认值
        etStartTime.setText("2023-10-01T10:00:00")
        etEndTime.setText("2023-10-01T12:00:00")
        etBookingType.setText("hour")

        // 提交预订
        btnSubmit.setOnClickListener {
            val startTime = etStartTime.text.toString()
            val endTime = etEndTime.text.toString()
            val bookingType = etBookingType.text.toString()

            if (startTime.isEmpty() || endTime.isEmpty() || bookingType.isEmpty()) {
                Toast.makeText(requireContext(), "请填写所有字段", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitBooking(startTime, endTime, bookingType)
        }
    }

    private fun submitBooking(startTime: String, endTime: String, bookingType: String) {
        val token = ConfigManager.getInstance(requireContext()).getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val response = repository.createOrder(
                seatId = seatId ?: "",
                startTime = startTime,
                endTime = endTime,
                bookingType = bookingType,
                token = token
            )

            when (response) {
                is ApiResponse.Success -> {
                    val order = response.data
                    
                    // 在debug模式下显示弹窗
                    if (BuildConfig.DEBUG) {
                        showResponseDialog("预订成功", "订单ID: ${order.id}\n总价: ${order.totalPrice}\n状态: ${order.status}")
                    } else {
                        Toast.makeText(requireContext(), "预订成功", Toast.LENGTH_SHORT).show()
                    }
                    
                    // 跳转到订单详情页面
                    val orderFragment = OrderFragment()
                    val bundle = Bundle()
                    bundle.putString("orderId", order.id)
                    orderFragment.arguments = bundle
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, orderFragment)
                        .addToBackStack(null)
                        .commit()
                }
                is ApiResponse.Error -> {
                    // 在debug模式下显示弹窗
                    if (BuildConfig.DEBUG) {
                        showResponseDialog("预订失败", "错误码: ${response.code}\n错误信息: ${response.message}")
                    } else {
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showResponseDialog(title: String, message: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("确定") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
