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
import androidx.navigation.fragment.findNavController
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.order.OrderItem
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.ui.screen.OrderListScreen
import com.example.scylier.istudyspot.ui.theme.IStudySpotTheme
import com.example.scylier.istudyspot.utils.ConfigManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrderListFragment : Fragment() {
    private val repository by lazy { MainRepository(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return android.widget.FrameLayout(requireContext()).apply {
            addView(
                androidx.compose.ui.platform.ComposeView(requireContext()).apply {
                    setViewCompositionStrategy(
                        ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                    )
                    setContent {
                        IStudySpotTheme {
                            var orders by remember { mutableStateOf<List<OrderItem>>(emptyList()) }
                            var isLoading by remember { mutableStateOf(true) }

                            OrderListScreen(
                                orders = orders,
                                isLoading = isLoading,
                                onOrderClick = { order ->
                                    val bundle = Bundle()
                                    bundle.putString("orderId", order.id)
                                    findNavController().navigate(R.id.action_orderListFragment_to_orderFragment, bundle)
                                }
                            )

                            if (isLoading) {
                                loadOrders { list ->
                                    orders = list
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

    private fun loadOrders(onResult: (List<OrderItem>) -> Unit) {
        val token = ConfigManager.getInstance(requireContext()).getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
            onResult(emptyList())
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val response = repository.getUserOrders(token = token)
            when (response) {
                is ApiResponse.Success -> onResult(response.data.list)
                is ApiResponse.Error -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                    onResult(emptyList())
                }
            }
        }
    }
}
