package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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
        viewModel = ViewModelProvider(this)[MoreViewModel::class.java]
        initMoreItems(view)
    }

    private fun initMoreItems(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.more_recycler)

        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        val adapter = MoreItemAdapter(viewModel.groupedItems) { item ->
            handleMoreItemClick(item)
        }
        recyclerView.adapter = adapter
    }

    private fun handleMoreItemClick(item: MoreItem) {
        when (item.title) {
            "预约记录" -> {
                findNavController().navigate(R.id.action_nav_more_to_orderListFragment)
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
                findNavController().navigate(R.id.action_nav_more_to_rulesFragment)
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
            "退出登录" -> {
                Toast.makeText(requireContext(), "退出登录功能开发中", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
