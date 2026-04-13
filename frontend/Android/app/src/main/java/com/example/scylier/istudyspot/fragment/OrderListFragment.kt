package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.order.OrderItem
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.utils.ConfigManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrderListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderAdapter
    private lateinit var repository: MainRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        repository = MainRepository(requireContext())

        loadOrders()
    }

    private fun loadOrders() {
        val token = ConfigManager.getInstance(requireContext()).getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val response = repository.getUserOrders(token = token)
            when (response) {
                is ApiResponse.Success -> {
                    val orders = response.data.list
                    adapter = OrderAdapter(orders) {
                        val bundle = Bundle()
                        bundle.putString("orderId", it.id)
                        findNavController().navigate(R.id.action_orderListFragment_to_orderFragment, bundle)
                    }
                    recyclerView.adapter = adapter
                }
                is ApiResponse.Error -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    inner class OrderAdapter(
        private val orders: List<OrderItem>,
        private val onItemClickListener: (OrderItem) -> Unit
    ) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_order, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val order = orders[position]
            holder.bind(order)
        }

        override fun getItemCount(): Int = orders.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvStudyRoomName: TextView = itemView.findViewById(R.id.tv_study_room_name)
            private val tvSeatPosition: TextView = itemView.findViewById(R.id.tv_seat_position)
            private val tvStartTime: TextView = itemView.findViewById(R.id.tv_start_time)
            private val tvEndTime: TextView = itemView.findViewById(R.id.tv_end_time)
            private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)

            fun bind(order: OrderItem) {
                tvStudyRoomName.text = order.studyRoomName
                tvSeatPosition.text = order.seatPosition
                tvStartTime.text = order.startTime
                tvEndTime.text = order.endTime
                tvStatus.text = order.status

                itemView.setOnClickListener {
                    onItemClickListener(order)
                }
            }
        }
    }
}
