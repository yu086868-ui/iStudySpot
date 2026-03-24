package com.example.scylier.istudyspot.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.scylier.istudyspot.viewmodel.LoginViewModel
import kotlin.getValue

class LoginFragment: Fragment() {

    private val viewModel: LoginViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}