package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.adapters.FunctionItemAdapter
import com.example.scylier.istudyspot.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        // 初始化功能项组件
        initFunctionItems(view)
    }

    private fun initFunctionItems(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.features_recycler)
        
        // 设置布局管理器，4列网格
        val layoutManager = GridLayoutManager(requireContext(), 4)
        recyclerView.layoutManager = layoutManager
        
        // 创建并设置适配器
        val adapter = FunctionItemAdapter(viewModel.mainFeatures)
        recyclerView.adapter = adapter
    }
}
