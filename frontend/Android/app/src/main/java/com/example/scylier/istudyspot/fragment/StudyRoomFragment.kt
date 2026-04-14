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
import com.example.scylier.istudyspot.models.studyroom.StudyRoomItem
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.ui.screen.StudyRoomScreen
import com.example.scylier.istudyspot.ui.theme.IStudySpotTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StudyRoomFragment : Fragment() {
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
                            var studyRooms by remember { mutableStateOf<List<StudyRoomItem>>(emptyList()) }
                            var isLoading by remember { mutableStateOf(true) }

                            StudyRoomScreen(
                                studyRooms = studyRooms,
                                isLoading = isLoading,
                                onStudyRoomClick = { room ->
                                    val bundle = Bundle()
                                    bundle.putString("studyRoomId", room.id)
                                    bundle.putString("studyRoomName", room.name)
                                    findNavController().navigate(R.id.action_studyRoomFragment_to_seatFragment, bundle)
                                }
                            )

                            if (isLoading) {
                                loadStudyRooms { rooms ->
                                    studyRooms = rooms
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

    private fun loadStudyRooms(onResult: (List<StudyRoomItem>) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            val response = repository.getStudyRooms()
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
