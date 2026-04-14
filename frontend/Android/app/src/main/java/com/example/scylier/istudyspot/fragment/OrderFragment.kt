package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.order.OrderDetail
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.ui.screen.OrderDetailScreen
import com.example.scylier.istudyspot.ui.theme.IStudySpotTheme
import com.example.scylier.istudyspot.utils.ConfigManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrderFragment : Fragment() {
    private val repository by lazy { MainRepository(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val orderId = arguments?.getString("orderId") ?: ""

        return android.widget.FrameLayout(requireContext()).apply {
            addView(
                androidx.compose.ui.platform.ComposeView(requireContext()).apply {
                    setViewCompositionStrategy(
                        ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                    )
                    setContent {
                        IStudySpotTheme {
                            var order by remember { mutableStateOf<OrderDetail?>(null) }
                            var isLoading by remember { mutableStateOf(true) }

                            OrderDetailScreen(
                                order = order,
                                isLoading = isLoading,
                                onCheckin = { checkin(orderId) },
                                onCheckout = { checkout(orderId) },
                                onCancel = { cancelOrder(orderId) }
                            )

                            if (isLoading) {
                                loadOrder(orderId) { detail ->
                                    order = detail
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    private fun loadOrder(orderId: String, onResult: (OrderDetail?) -> Unit) {
        val token = ConfigManager.getInstance(requireContext()).getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
            onResult(null)
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val response = repository.getOrderDetail(orderId, token)
            when (response) {
                is ApiResponse.Success -> onResult(response.data)
                is ApiResponse.Error -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                    onResult(null)
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

        CoroutineScope(Dispatchers.Main).launch {
            val response = repository.checkin(orderId, "123456", token)
            when (response) {
                is ApiResponse.Success -> {
                    Toast.makeText(requireContext(), "签到成功", Toast.LENGTH_SHORT).show()
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
                }
                is ApiResponse.Error -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
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
                    Toast.makeText(requireContext(), "取消成功", Toast.LENGTH_SHORT).show()
                }
                is ApiResponse.Error -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
