package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.card.CardItem
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CardViewModel(private val repository: MainRepository = MainRepository()) : ViewModel() {
    private val _cards = MutableStateFlow<List<CardItem>>(emptyList())
    val cards: StateFlow<List<CardItem>> = _cards

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadCards(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            when (val response = repository.getCardList(userId)) {
                is ApiResponse.Success -> {
                    _cards.value = response.data ?: emptyList()
                }
                is ApiResponse.Error -> {}
            }
            _isLoading.value = false
        }
    }
}
