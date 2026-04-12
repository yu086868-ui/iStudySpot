package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.utils.ConfigManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrderFragment : Fragment() {
    private lateinit var tvOrderId: TextView
    private lateinit var tvStudyRoomName: TextView
    private lateinit var tvSeatPosition: TextView
    private lateinit var tvStartTime: TextView
    private lateinit var tvEndTime: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnCancel: Button
    private lateinit var btnCheckin: Button
    private lateinit var btnCheckout: Button
    private lateinit var repository: MainRepository
    private var orderId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvOrderId = view.findViewById(R.id.tv_order_id)
        tvStudyRoomName = view.findViewById(R.id.tv_study_room_name)
        tvSeatPosition = view.findViewById(R.id.tv_seat_position)
        tvStartTime = view.findViewById(R.id.tv_start_time)
        tvEndTime = view.findViewById(R.id.tv_end_time)
        tvTotalPrice = view.findViewById(R.id.tv_total_price)
        tvStatus = view.findViewById(R.id.tv_status)
        btnCancel = view.findViewById(R.id.btn_cancel)
        btnCheckin = view.findViewById(R.id.btn_checkin)
        btnCheckout = view.findViewById(R.id.btn_checkout)
        repository = MainRepository(requireContext())

        // 获取订单ID
        orderId = arguments?.getString("orderId")
        orderId?.let { loadOrderDetail(it) }

        // 设置按钮点击事件
        btnCancel.setOnClickListener {
            orderId?.let { cancelOrder(it) }
        }

        btnCheckin.setOnClickListener {
            orderId?.let { checkin(it) }
        }

        btnCheckout.setOnClickListener {
            orderId?.let { checkout(it) }
        }
    }

    private fun loadOrderDetail(orderId: String) {
        val token = ConfigManager.getInstance(requireContext()).getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val response = repository.getOrderDetail(orderId, token)
            when (response) {
                is ApiResponse.Success -> {
                    val order = response.data
                    tvOrderId.text = order.id
                    tvStudyRoomName.text = order.studyRoomName
                    tvSeatPosition.text = order.seatPosition
                    tvStartTime.text = order.startTime
                    tvEndTime.text = order.endTime
                    tvTotalPrice.text = order.totalPrice.toString()
                    tvStatus.text = order.status

                    // 根据订单状态显示不同的按钮
                    updateButtons(order.status)
                }
                is ApiResponse.Error -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateButtons(status: String) {
        when (status) {
            "pending" -> {
                btnCancel.visibility = View.VISIBLE
                btnCheckin.visibility = View.GONE
                btnCheckout.visibility = View.GONE
            }
            "paid" -> {
                btnCancel.visibility = View.VISIBLE
                btnCheckin.visibility = View.VISIBLE
                btnCheckout.visibility = View.GONE
            }
            "in_use" -> {
                btnCancel.visibility = View.GONE
                btnCheckin.visibility = View.GONE
                btnCheckout.visibility = View.VISIBLE
            }
            else -> {
                btnCancel.visibility = View.GONE
                btnCheckin.visibility = View.GONE
                btnCheckout.visibility = View.GONE
            }
        }
    }

    private fun cancelOrder(orderId: String) {
        val token = ConfigManager.getInstance(requireContext()).getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val response = repository.cancelOrder(orderId, token)
            when (response) {
                is ApiResponse.Success -> {
                    Toast.makeText(requireContext(), "订单取消成功", Toast.LENGTH_SHORT).show()
                    loadOrderDetail(orderId)
                }
                is ApiResponse.Error -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkin(orderId: String) {
        val token = ConfigManager.getInstance(requireContext()).getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
            return
        }

        // 这里应该是扫码获取签到码，暂时使用固定值
        val checkinCode = "123456"

        CoroutineScope(Dispatchers.Main).launch {
            val response = repository.checkin(orderId, checkinCode, token)
            when (response) {
                is ApiResponse.Success -> {
                    Toast.makeText(requireContext(), "签到成功", Toast.LENGTH_SHORT).show()
                    loadOrderDetail(orderId)
                }
                is ApiResponse.Error -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkout(orderId: String) {
        val token = ConfigManager.getInstance(requireContext()).getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val response = repository.checkout(orderId, token)
            when (response) {
                is ApiResponse.Success -> {
                    Toast.makeText(requireContext(), "签退成功", Toast.LENGTH_SHORT).show()
                    loadOrderDetail(orderId)
                }
                is ApiResponse.Error -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
