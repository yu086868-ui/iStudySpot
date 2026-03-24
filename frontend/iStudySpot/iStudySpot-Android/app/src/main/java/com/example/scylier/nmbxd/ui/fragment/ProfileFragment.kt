package com.example.scylier.nmbxd.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.scylier.nmbxd.R
import com.example.scylier.nmbxd.ui.viewmodel.ProfileViewModel

class ProfileFragment: Fragment(R.layout.fragment_profile) {
    private val viewModel: ProfileViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }



}