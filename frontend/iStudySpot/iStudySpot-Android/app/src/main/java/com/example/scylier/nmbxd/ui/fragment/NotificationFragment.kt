package com.example.scylier.nmbxd.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.scylier.nmbxd.R
import com.example.scylier.nmbxd.ui.viewmodel.NotificationViewModel

class NotificationFragment: Fragment(R.layout.fragment_notification) {
    private val viewModel: NotificationViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}