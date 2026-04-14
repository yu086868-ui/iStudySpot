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
import com.example.scylier.istudyspot.ui.screen.HomeScreen
import com.example.scylier.istudyspot.ui.theme.IStudySpotTheme

class HomeFragment : Fragment() {

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
                            HomeScreen(
                                onAction = { actionId ->
                                    handleAction(actionId)
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

    private fun handleAction(actionId: String) {
        when (actionId) {
            "booking" -> findNavController().navigate(R.id.action_nav_home_to_studyRoomFragment)
            "checkin" -> {
                findNavController().navigate(R.id.action_nav_home_to_orderListFragment)
                Toast.makeText(requireContext(), "请在订单详情中签到", Toast.LENGTH_SHORT).show()
            }
            "guide" -> findNavController().navigate(R.id.action_nav_home_to_guideFragment)
            "my_booking" -> findNavController().navigate(R.id.action_nav_home_to_orderListFragment)
            "study_record" -> findNavController().navigate(R.id.action_nav_home_to_studyRecordFragment)
            "team_booking" -> Toast.makeText(requireContext(), "团队预约功能开发中", Toast.LENGTH_SHORT).show()
            "notification" -> findNavController().navigate(R.id.action_nav_home_to_notificationFragment)
            "settings" -> findNavController().navigate(R.id.action_nav_home_to_moreFragment)
        }
    }
}
