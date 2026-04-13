package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.infra.network.ApiManager
import com.example.scylier.istudyspot.infra.network.ErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUsername = view.findViewById(R.id.et_username)
        etPassword = view.findViewById(R.id.et_password)
        btnLogin = view.findViewById(R.id.btn_login)
        btnRegister = view.findViewById(R.id.btn_register)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "用户名和密码不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            login(username, password)
        }

        btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun login(username: String, password: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val apiManager = ApiManager(context = requireContext())
            val response = apiManager.login(username, password)

            when (response) {
                is ApiResponse.Success -> {
                    // 登录成功，保存token
                    val token = response.data.token
                    // 这里可以保存token到SharedPreferences
                    Toast.makeText(requireContext(), "登录成功", Toast.LENGTH_SHORT).show()
                    // 关闭登录fragment
                    parentFragmentManager.popBackStack()
                    // 通知ProfileFragment更新用户信息
                    (parentFragmentManager.findFragmentByTag("ProfileFragment") as? ProfileFragment)?.getUserInfo()
                }
                is ApiResponse.Error -> {
                    val errorMessage = ErrorHandler.getErrorMessage(response)
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
