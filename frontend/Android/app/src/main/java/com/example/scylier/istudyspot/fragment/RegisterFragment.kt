package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.example.scylier.istudyspot.BuildConfig
import com.example.scylier.istudyspot.infra.network.ApiManager
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.ui.screen.RegisterScreen
import com.example.scylier.istudyspot.ui.theme.IStudySpotTheme
import com.example.scylier.istudyspot.utils.ConfigManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

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
                            RegisterScreen(
                                onRegister = { username, password, confirmPassword, nickname ->
                                    register(username, password, confirmPassword, nickname)
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

    private fun register(username: String, password: String, confirmPassword: String, nickname: String) {
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "请填写所有必填字段", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "两次密码输入不一致", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val apiManager = ApiManager(context = requireContext())
            val response = apiManager.register(username, password, nickname)

            when (response) {
                is ApiResponse.Success -> {
                    // 保存 token 和用户信息
                    val configManager = ConfigManager.getInstance(requireContext())
                    configManager.saveToken(response.data.token)
                    configManager.saveUserId(response.data.user.id)
                    configManager.saveUsername(response.data.user.username)
                    configManager.saveNickname(response.data.user.nickname)

                    if (BuildConfig.DEBUG) {
                        androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("注册成功(Debug)")
                            .setMessage("用户ID: ${response.data.user.id}\n用户名: ${response.data.user.username}")
                            .setPositiveButton("确定", null)
                            .show()
                    } else {
                        Toast.makeText(requireContext(), "注册成功", Toast.LENGTH_SHORT).show()
                    }
                    parentFragmentManager.popBackStack()
                }
                is ApiResponse.Error -> {
                    if (BuildConfig.DEBUG) {
                        androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("注册失败(Debug)")
                            .setMessage("错误码: ${response.code}\n错误信息: ${response.message}")
                            .setPositiveButton("确定", null)
                            .show()
                    } else {
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
