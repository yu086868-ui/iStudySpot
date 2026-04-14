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
import com.example.scylier.istudyspot.ui.screen.MoreScreen
import com.example.scylier.istudyspot.ui.theme.IStudySpotTheme

class MoreFragment : Fragment() {

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
                            MoreScreen(
                                onAction = { title ->
                                    handleAction(title)
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

    private fun handleAction(title: String) {
        when (title) {
            "预约记录" -> findNavController().navigate(R.id.action_nav_more_to_orderListFragment)
            "帮助中心" -> findNavController().navigate(R.id.action_nav_more_to_rulesFragment)
            else -> Toast.makeText(requireContext(), "${title}功能开发中", Toast.LENGTH_SHORT).show()
        }
    }
}
