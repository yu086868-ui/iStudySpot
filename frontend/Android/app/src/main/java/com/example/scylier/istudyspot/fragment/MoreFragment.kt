package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.adapters.MoreItemAdapter
import com.example.scylier.istudyspot.models.MoreItem
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
        val adapter = MoreItemAdapter(viewModel.groupedItems) { item ->
            handleMoreItemClick(item)
        }
        recyclerView.adapter = adapter
    }

    private fun handleMoreItemClick(item: MoreItem) {
        when (item.title) {
            "预约记录" -> {
                // 跳转到预约记录页面（即订单列表页面）
                val orderListFragment = OrderListFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, orderListFragment)
                    .addToBackStack(null)
                    .commit()
            }
            "违规记录" -> {
                Toast.makeText(requireContext(), "违规记录功能开发中", Toast.LENGTH_SHORT).show()
            }
            "学习统计" -> {
                Toast.makeText(requireContext(), "学习统计功能开发中", Toast.LENGTH_SHORT).show()
            }
            "成就徽章" -> {
                Toast.makeText(requireContext(), "成就徽章功能开发中", Toast.LENGTH_SHORT).show()
            }
            "积分兑换" -> {
                Toast.makeText(requireContext(), "积分兑换功能开发中", Toast.LENGTH_SHORT).show()
            }
            "积分明细" -> {
                Toast.makeText(requireContext(), "积分明细功能开发中", Toast.LENGTH_SHORT).show()
            }
            "帮助中心" -> {
                // 跳转到规则页面（帮助中心）
                val rulesFragment = RulesFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, rulesFragment)
                    .addToBackStack(null)
                    .commit()
            }
            "意见反馈" -> {
                Toast.makeText(requireContext(), "意见反馈功能开发中", Toast.LENGTH_SHORT).show()
            }
            "关于我们" -> {
                Toast.makeText(requireContext(), "关于我们功能开发中", Toast.LENGTH_SHORT).show()
            }
            "推荐好友" -> {
                Toast.makeText(requireContext(), "推荐好友功能开发中", Toast.LENGTH_SHORT).show()
            }
            "数据导出" -> {
                Toast.makeText(requireContext(), "数据导出功能开发中", Toast.LENGTH_SHORT).show()
            }
            "隐私政策" -> {
                Toast.makeText(requireContext(), "隐私政策功能开发中", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
