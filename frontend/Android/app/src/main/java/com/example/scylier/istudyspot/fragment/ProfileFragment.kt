package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.network.ApiManager
import com.example.scylier.istudyspot.network.ErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private lateinit var token: String
    private lateinit var ivAvatar: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvNickname: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvEmail: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化UI组件
        initUI(view)
        // 获取token
        token = arguments?.getString("token", "") ?: ""
        // 获取用户信息
        getUserInfo()
    }

    private fun initUI(view: View) {
        ivAvatar = view.findViewById(R.id.iv_avatar)
        tvUsername = view.findViewById(R.id.tv_username)
        tvNickname = view.findViewById(R.id.tv_nickname)
        tvPhone = view.findViewById(R.id.tv_phone)
        tvEmail = view.findViewById(R.id.tv_email)

        // 添加头像点击事件
        ivAvatar.setOnClickListener {
            showLoginFragment()
        }
    }

    private fun showLoginFragment() {
        val loginFragment = LoginFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, loginFragment)
            .addToBackStack(null)
            .commit()
    }

    fun getUserInfo() {
        CoroutineScope(Dispatchers.Main).launch {
            val apiManager = ApiManager(token = token, context = requireContext())
            val response = apiManager.getUserInfo()

            when (response) {
                is ApiResponse.Success -> {
                    val userInfo = response.data
                    tvUsername.text = userInfo.username
                    tvNickname.text = userInfo.nickname
                    tvPhone.text = userInfo.phone ?: "未设置"
                    tvEmail.text = userInfo.email ?: "未设置"
                    // 这里可以添加头像加载逻辑
                }
                is ApiResponse.Error -> {
                    val errorMessage = ErrorHandler.getErrorMessage(response)
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
