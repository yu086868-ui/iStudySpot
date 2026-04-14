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
import com.example.scylier.istudyspot.models.studyroom.SeatInfo
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.ui.screen.SeatMapScreen
import com.example.scylier.istudyspot.ui.theme.IStudySpotTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SeatFragment : Fragment() {
    private val repository by lazy { MainRepository(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val studyRoomId = arguments?.getString("studyRoomId") ?: ""
        val studyRoomName = arguments?.getString("studyRoomName") ?: "座位选择"

        return android.widget.FrameLayout(requireContext()).apply {
            addView(
                androidx.compose.ui.platform.ComposeView(requireContext()).apply {
                    setViewCompositionStrategy(
                        ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                    )
                    setContent {
                        IStudySpotTheme {
                            var seats by remember { mutableStateOf<List<SeatInfo>>(emptyList()) }
                            var isLoading by remember { mutableStateOf(true) }

                            SeatMapScreen(
                                studyRoomName = studyRoomName,
                                seats = seats,
                                isLoading = isLoading,
                                onSeatClick = { seat ->
                                    if (seat.status == "available") {
                                        val bundle = Bundle()
                                        bundle.putString("seatId", seat.id)
                                        bundle.putString("studyRoomId", studyRoomId)
                                        bundle.putString("studyRoomName", studyRoomName)
                                        bundle.putString("seatPosition", "${seat.row}-${seat.col}")
                                        bundle.putDouble("pricePerHour", seat.pricePerHour)
                                        findNavController().navigate(R.id.action_seatFragment_to_bookingFragment, bundle)
                                    } else {
                                        Toast.makeText(requireContext(), "该座位不可预订", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )

                            if (isLoading) {
                                loadSeats(studyRoomId) { list ->
                                    seats = list
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

    private fun loadSeats(studyRoomId: String, onResult: (List<SeatInfo>) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            val response = repository.getStudyRoomSeats(studyRoomId)
            when (response) {
                is ApiResponse.Success -> onResult(response.data.seats)
                is ApiResponse.Error -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                    onResult(emptyList())
                }
            }
        }
    }
}
