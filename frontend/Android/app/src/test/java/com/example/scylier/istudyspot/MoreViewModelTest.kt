package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.viewmodel.MoreViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MoreViewModelTest {

    private lateinit var viewModel: MoreViewModel

    @Before
    fun setup() {
        viewModel = MoreViewModel()
    }

    @Test
    fun testViewModel_canBeInstantiated() {
        assertNotNull(viewModel)
    }

    @Test
    fun testViewModel_isViewModel() {
        assertTrue(viewModel is androidx.lifecycle.ViewModel)
    }
}
