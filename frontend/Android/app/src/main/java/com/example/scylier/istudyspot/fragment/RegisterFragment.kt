package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.scylier.istudyspot.BuildConfig
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.network.ApiManager
import com.example.scylier.istudyspot.network.ErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etNickname: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnBack: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUsername = view.findViewById(R.id.et_username)
        etPassword = view.findViewById(R.id.et_password)
        etNickname = view.findViewById(R.id.et_nickname)
        btnRegister = view.findViewById(R.id.btn_register)
        btnBack = view.findViewById(R.id.btn_back)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val nickname = etNickname.text.toString().trim()

            if (username.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
                Toast.makeText(requireContext(), "所有字段不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            register(username, password, nickname)
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun register(username: String, password: String, nickname: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val apiManager = ApiManager(context = requireContext())
            val response = apiManager.register(username, password, nickname)

            when (response) {
                is ApiResponse.Success -> {
                    // 注册成功，保存token
                    val token = response.data.token
                    // 这里可以保存token到SharedPreferences
                    
                    // 在debug模式下显示弹窗
                    if (BuildConfig.DEBUG) {
                        showResponseDialog("注册成功", "Token: $token\n用户名: ${response.data.user.username}\n昵称: ${response.data.user.nickname}")
                    } else {
                        Toast.makeText(requireContext(), "注册成功", Toast.LENGTH_SHORT).show()
                    }
                    
                    // 关闭注册fragment
                    parentFragmentManager.popBackStack()
                    // 通知ProfileFragment更新用户信息
                    (parentFragmentManager.findFragmentByTag("ProfileFragment") as? ProfileFragment)?.getUserInfo()
                }
                is ApiResponse.Error -> {
                    val errorMessage = ErrorHandler.getErrorMessage(response)
                    
                    // 在debug模式下显示弹窗
                    if (BuildConfig.DEBUG) {
                        showResponseDialog("注册失败", "错误码: ${response.code}\n错误信息: $errorMessage")
                    } else {
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showResponseDialog(title: String, message: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("确定") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
