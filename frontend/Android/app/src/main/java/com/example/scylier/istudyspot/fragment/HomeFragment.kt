package com.example.scylier.istudyspot.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.adapters.FunctionItemAdapter
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.FunctionItem
import com.example.scylier.istudyspot.infra.network.ApiManager
import com.example.scylier.istudyspot.infra.network.ErrorHandler
import com.example.scylier.istudyspot.viewmodel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var token: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        token = arguments?.getString("token", "") ?: ""
        initFunctionItems(view)
    }

    private fun initFunctionItems(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.features_recycler)

        val layoutManager = GridLayoutManager(requireContext(), 4)
        recyclerView.layoutManager = layoutManager

        val adapter = FunctionItemAdapter(viewModel.mainFeatures) { item ->
            handleFunctionItemClick(item)
        }
        recyclerView.adapter = adapter
    }

    private fun handleFunctionItemClick(item: FunctionItem) {
        when (item.id) {
            "booking" -> {
                findNavController().navigate(R.id.action_nav_home_to_studyRoomFragment)
            }
            "checkin" -> {
                findNavController().navigate(R.id.action_nav_home_to_orderListFragment)
                Toast.makeText(requireContext(), "请在订单详情中签到", Toast.LENGTH_SHORT).show()
            }
            "guide" -> {
                findNavController().navigate(R.id.action_nav_home_to_guideFragment)
            }
            "my_booking" -> {
                findNavController().navigate(R.id.action_nav_home_to_orderListFragment)
            }
            "study_record" -> {
                findNavController().navigate(R.id.action_nav_home_to_studyRecordFragment)
            }
            "team_booking" -> {
                Toast.makeText(requireContext(), "团队预约功能开发中", Toast.LENGTH_SHORT).show()
            }
            "notification" -> {
                findNavController().navigate(R.id.action_nav_home_to_notificationFragment)
            }
            "settings" -> {
                findNavController().navigate(R.id.action_nav_home_to_moreFragment)
            }
        }
    }

    private fun getStudyRooms() {
        CoroutineScope(Dispatchers.Main).launch {
            val apiManager = ApiManager(token = token, context = requireContext())
            val response = apiManager.getStudyRooms()

            when (response) {
                is ApiResponse.Success -> {
                    val studyRooms = response.data.list
                    Toast.makeText(requireContext(), "获取到${studyRooms.size}个自习室", Toast.LENGTH_SHORT).show()
                }
                is ApiResponse.Error -> {
                    val errorMessage = ErrorHandler.getErrorMessage(response)
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
