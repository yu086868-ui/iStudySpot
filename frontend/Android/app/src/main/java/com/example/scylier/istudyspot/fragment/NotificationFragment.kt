package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.viewmodel.NotificationViewModel

/**
 * 通知提醒Fragment
 * 展示系统通知、预约提醒、活动消息等
 */
class NotificationFragment : Fragment() {

    private lateinit var viewModel: NotificationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[NotificationViewModel::class.java]
        
        initNotificationList(view)
    }

    private fun initNotificationList(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.notification_recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        val adapter = NotificationAdapter(viewModel.notifications)
        recyclerView.adapter = adapter
    }

    inner class NotificationAdapter(
        private val notifications: List<NotificationViewModel.Notification>
    ) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notification, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(notifications[position])
        }

        override fun getItemCount(): Int = notifications.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvTitle = itemView.findViewById<TextView>(R.id.tv_notification_title)
            private val tvContent = itemView.findViewById<TextView>(R.id.tv_notification_content)
            private val tvTime = itemView.findViewById<TextView>(R.id.tv_notification_time)
            private val unreadIndicator = itemView.findViewById<View>(R.id.unread_indicator)

            fun bind(notification: NotificationViewModel.Notification) {
                tvTitle.text = notification.title
                tvContent.text = notification.content
                tvTime.text = notification.time
                unreadIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE
            }
        }
    }
}
