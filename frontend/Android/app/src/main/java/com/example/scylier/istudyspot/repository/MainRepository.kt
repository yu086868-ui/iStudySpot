package com.example.scylier.istudyspot.repository

import android.content.Context
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.auth.LoginRequest
import com.example.scylier.istudyspot.models.auth.RegisterRequest
import com.example.scylier.istudyspot.models.auth.UserInfo
import com.example.scylier.istudyspot.models.order.CreateOrderRequest
import com.example.scylier.istudyspot.models.order.OrderListResponse
import com.example.scylier.istudyspot.models.studyroom.StudyRoomListResponse
import com.example.scylier.istudyspot.models.studyroom.SeatMapResponse
import com.example.scylier.istudyspot.network.ApiManager

class MainRepository(private val context: Context) {
    private val apiManager = ApiManager(context = context)

    // 认证相关
    suspend fun login(username: String, password: String) = apiManager.login(username, password)
    suspend fun register(username: String, password: String, nickname: String) = apiManager.register(username, password, nickname)
    suspend fun refreshToken(token: String) = apiManager.refreshToken()

    // 自习室相关
    suspend fun getStudyRooms(page: Int = 1, size: Int = 10) = apiManager.getStudyRooms(page, size)
    suspend fun getStudyRoomDetail(id: String) = apiManager.getStudyRoomDetail(id)

    // 座位相关
    suspend fun getStudyRoomSeats(id: String) = apiManager.getStudyRoomSeats(id)
    suspend fun getSeatDetail(id: String) = apiManager.getSeatDetail(id)

    // 订单相关
    suspend fun createOrder(seatId: String, startTime: String, endTime: String, bookingType: String, token: String) = 
        ApiManager(token = token, context = context).createOrder(seatId, startTime, endTime, bookingType)
    
    suspend fun getUserOrders(status: String? = null, page: Int = 1, size: Int = 10, token: String) = 
        ApiManager(token = token, context = context).getUserOrders(status, page, size)
    
    suspend fun getOrderDetail(id: String, token: String) = 
        ApiManager(token = token, context = context).getOrderDetail(id)
    
    suspend fun cancelOrder(id: String, token: String) = 
        ApiManager(token = token, context = context).cancelOrder(id)

    // 签到/签退相关
    suspend fun checkin(id: String, checkinCode: String, token: String) = 
        ApiManager(token = token, context = context).checkin(id, checkinCode)
    
    suspend fun checkout(id: String, token: String) = 
        ApiManager(token = token, context = context).checkout(id)

    // 用户相关
    suspend fun getUserInfo(token: String) = 
        ApiManager(token = token, context = context).getUserInfo()
    
    suspend fun updateUserInfo(nickname: String? = null, avatar: String? = null, phone: String? = null, email: String? = null, token: String) = 
        ApiManager(token = token, context = context).updateUserInfo(nickname, avatar, phone, email)
    
    suspend fun changePassword(oldPassword: String, newPassword: String, token: String) = 
        ApiManager(token = token, context = context).changePassword(oldPassword, newPassword)

    // 支付相关
    suspend fun createPayment(orderId: String, amount: Double, paymentMethod: String, token: String) = 
        ApiManager(token = token, context = context).createPayment(orderId, amount, paymentMethod)
    
    suspend fun getPaymentStatus(id: String, token: String) = 
        ApiManager(token = token, context = context).getPaymentStatus(id)

    // 统计相关
    suspend fun getStudyRoomStatistics(id: String, startDate: String, endDate: String) = 
        apiManager.getStudyRoomStatistics(id, startDate, endDate)
}
