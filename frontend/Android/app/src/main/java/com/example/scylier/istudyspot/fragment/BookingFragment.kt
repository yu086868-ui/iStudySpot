package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.ui.screen.BookingScreen
import com.example.scylier.istudyspot.ui.theme.IStudySpotTheme
import com.example.scylier.istudyspot.utils.ConfigManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookingFragment : Fragment() {
    private val repository by lazy { MainRepository(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val seatId = arguments?.getString("seatId") ?: ""
        val studyRoomId = arguments?.getString("studyRoomId") ?: ""
        val studyRoomName = arguments?.getString("studyRoomName") ?: ""
        val seatPosition = arguments?.getString("seatPosition") ?: ""
        val pricePerHour = arguments?.getDouble("pricePerHour") ?: 0.0

        return android.widget.FrameLayout(requireContext()).apply {
            addView(
                androidx.compose.ui.platform.ComposeView(requireContext()).apply {
                    setViewCompositionStrategy(
                        ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                    )
                    setContent {
                        IStudySpotTheme {
                            BookingScreen(
                                studyRoomName = studyRoomName,
                                seatPosition = seatPosition,
                                pricePerHour = pricePerHour,
                                onBook = { startTime, endTime, bookingType ->
                                    createOrder(seatId, startTime, endTime, bookingType)
                                }
                            )
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

    private fun createOrder(seatId: String, startTime: String, endTime: String, bookingType: String) {
        val token = ConfigManager.getInstance(requireContext()).getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val response = repository.createOrder(seatId, startTime, endTime, bookingType, token)
            when (response) {
                is ApiResponse.Success -> {
                    Toast.makeText(requireContext(), "预约成功", Toast.LENGTH_SHORT).show()
                    val bundle = Bundle()
                    bundle.putString("orderId", response.data.id)
                    findNavController().navigate(R.id.action_bookingFragment_to_orderFragment, bundle)
                }
                is ApiResponse.Error -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
