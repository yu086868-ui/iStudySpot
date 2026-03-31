package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.adapters.MoreItemAdapter
import com.example.scylier.istudyspot.viewmodel.MoreViewModel

class MoreFragment : Fragment() {

    private lateinit var viewModel: MoreViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[MoreViewModel::class.java]
        // 初始化功能项组件
        initMoreItems(view)
    }

    private fun initMoreItems(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.more_recycler)
        
        // 设置布局管理器
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        
        // 创建并设置适配器
        val adapter = MoreItemAdapter(viewModel.groupedItems)
        recyclerView.adapter = adapter
    }
}
