package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.order.OrderDetail
import com.example.scylier.istudyspot.models.order.OrderItem
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class OrderListUiState(
    val orders: List<OrderItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class OrderDetailUiState(
    val order: OrderDetail? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val actionSuccess: String? = null
)

class OrderViewModel : ViewModel() {
    private val repository = MainRepository()

    private val _orderListState = MutableStateFlow(OrderListUiState())
    val orderListState: StateFlow<OrderListUiState> = _orderListState

    private val _orderDetailState = MutableStateFlow(OrderDetailUiState())
    val orderDetailState: StateFlow<OrderDetailUiState> = _orderDetailState

    fun loadOrders() {
        _orderListState.value = _orderListState.value.copy(isLoading = true)
        viewModelScope.launch {
            when (val response = repository.getUserOrders()) {
                is ApiResponse.Success -> {
                    _orderListState.value = OrderListUiState(
                        orders = response.data.list,
                        isLoading = false
                    )
                }
                is ApiResponse.Error -> {
                    _orderListState.value = OrderListUiState(
                        isLoading = false,
                        error = response.message
                    )
                }
            }
        }
    }

    fun loadOrderDetail(orderId: String) {
        _orderDetailState.value = _orderDetailState.value.copy(isLoading = true, actionSuccess = null)
        viewModelScope.launch {
            when (val response = repository.getOrderDetail(orderId)) {
                is ApiResponse.Success -> {
                    _orderDetailState.value = OrderDetailUiState(
                        order = response.data,
                        isLoading = false
                    )
                }
                is ApiResponse.Error -> {
                    _orderDetailState.value = OrderDetailUiState(
                        isLoading = false,
                        error = response.message
                    )
                }
            }
        }
    }

    fun checkin(orderId: String) {
        viewModelScope.launch {
            when (val response = repository.checkin(orderId, orderId)) {
                is ApiResponse.Success -> {
                    _orderDetailState.value = _orderDetailState.value.copy(
                        actionSuccess = "签到成功"
                    )
                    loadOrderDetail(orderId)
                }
                is ApiResponse.Error -> {
                    _orderDetailState.value = _orderDetailState.value.copy(
                        error = response.message
                    )
                }
            }
        }
    }

    fun checkout(orderId: String) {
        viewModelScope.launch {
            when (val response = repository.checkout(orderId)) {
                is ApiResponse.Success -> {
                    _orderDetailState.value = _orderDetailState.value.copy(
                        actionSuccess = "签退成功"
                    )
                    loadOrderDetail(orderId)
                }
                is ApiResponse.Error -> {
                    _orderDetailState.value = _orderDetailState.value.copy(
                        error = response.message
                    )
                }
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            when (val response = repository.cancelOrder(orderId)) {
                is ApiResponse.Success -> {
                    _orderDetailState.value = _orderDetailState.value.copy(
                        actionSuccess = "取消成功"
                    )
                    loadOrderDetail(orderId)
                }
                is ApiResponse.Error -> {
                    _orderDetailState.value = _orderDetailState.value.copy(
                        error = response.message
                    )
                }
            }
        }
    }

    fun clearActionSuccess() {
        _orderDetailState.value = _orderDetailState.value.copy(actionSuccess = null, error = null)
    }
}
