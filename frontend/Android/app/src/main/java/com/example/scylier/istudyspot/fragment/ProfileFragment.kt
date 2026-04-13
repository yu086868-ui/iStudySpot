package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
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
        initUI(view)
        token = arguments?.getString("token", "") ?: ""
        getUserInfo()
    }

    private fun initUI(view: View) {
        ivAvatar = view.findViewById(R.id.iv_avatar)
        tvUsername = view.findViewById(R.id.tv_username)
        tvNickname = view.findViewById(R.id.tv_nickname)
        tvPhone = view.findViewById(R.id.tv_phone)
        tvEmail = view.findViewById(R.id.tv_email)

        ivAvatar.setOnClickListener {
            showLoginFragment()
        }
    }

    private fun showLoginFragment() {
        findNavController().navigate(R.id.action_nav_profile_to_loginFragment)
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
                }
                is ApiResponse.Error -> {
                    val errorMessage = ErrorHandler.getErrorMessage(response)
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
