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
import com.example.scylier.istudyspot.infra.network.ApiManager
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.ui.screen.LoginScreen
import com.example.scylier.istudyspot.ui.theme.IStudySpotTheme
import com.example.scylier.istudyspot.utils.ConfigManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

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
                            LoginScreen(
                                onLogin = { username, password ->
                                    login(username, password)
                                },
                                onRegisterClick = {
                                    findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
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

    private fun login(username: String, password: String) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "用户名和密码不能为空", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val apiManager = ApiManager(context = requireContext())
            val response = apiManager.login(username, password)

            when (response) {
                is ApiResponse.Success -> {
                    // 保存 token 和用户信息
                    val configManager = ConfigManager.getInstance(requireContext())
                    configManager.saveToken(response.data.token)
                    configManager.saveUserId(response.data.user.id)
                    configManager.saveUsername(response.data.user.username)
                    configManager.saveNickname(response.data.user.nickname)

                    Toast.makeText(requireContext(), "登录成功", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is ApiResponse.Error -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
