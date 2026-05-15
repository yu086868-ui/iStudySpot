package com.example.scylier.istudyspot.repository

import android.content.Context
import com.example.scylier.istudyspot.infra.network.ApiManager

class MainRepository(private val context: Context) {
    private val apiManager = ApiManager(context = context)

    // 认证相关
    suspend fun login(username: String, password: String) = apiManager.login(username, password)
    suspend fun register(username: String, password: String, nickname: String, phone: String? = null, studentId: String? = null) =
        apiManager.register(username, password, nickname)
    suspend fun refreshToken(refreshToken: String) = apiManager.refreshToken(refreshToken)
    suspend fun logout(token: String) = ApiManager(token = token, context = context).logout()

    // 自习室相关
    suspend fun getStudyRooms(page: Int = 1, pageSize: Int = 20, status: String? = null, keyword: String? = null) =
        apiManager.getStudyRooms(page, pageSize, status, keyword)
    suspend fun getStudyRoomDetail(id: String) = apiManager.getStudyRoomDetail(id)

    // 座位相关
    suspend fun getStudyRoomSeats(id: String, status: String? = null, type: String? = null) =
        apiManager.getStudyRoomSeats(id, status, type)
    suspend fun getSeatDetail(id: String) = apiManager.getSeatDetail(id)

    // 预约/订单相关
    suspend fun createOrder(studyRoomId: String, seatId: String, startTime: String, endTime: String, bookingType: String, token: String) =
        ApiManager(token = token, context = context).createOrder(studyRoomId, seatId, startTime, endTime, bookingType)

    suspend fun getUserOrders(status: String? = null, startDate: String? = null, endDate: String? = null, page: Int = 1, pageSize: Int = 20, token: String) =
        ApiManager(token = token, context = context).getUserOrders(status, startDate, endDate, page, pageSize)

    suspend fun getOrderDetail(id: String, token: String) =
        ApiManager(token = token, context = context).getOrderDetail(id)

    suspend fun cancelOrder(id: String, token: String) =
        ApiManager(token = token, context = context).cancelOrder(id)

    suspend fun payOrder(id: String, token: String) =
        ApiManager(token = token, context = context).payOrder(id)

    suspend fun getReservationRules() = apiManager.getReservationRules()

    // 签到/签退相关
    suspend fun checkin(reservationId: String, seatId: String, token: String) =
        ApiManager(token = token, context = context).checkin(reservationId, seatId)

    suspend fun checkout(checkInRecordId: String, token: String) =
        ApiManager(token = token, context = context).checkout(checkInRecordId)

    suspend fun getCheckinRecords(startDate: String? = null, endDate: String? = null, page: Int = 1, pageSize: Int = 20, token: String) =
        ApiManager(token = token, context = context).getCheckinRecords(startDate, endDate, page, pageSize)

    suspend fun getCurrentCheckin(token: String) =
        ApiManager(token = token, context = context).getCurrentCheckin()

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

    // 公告相关
    suspend fun getAnnouncements(type: String? = null, priority: String? = null, page: Int = 1, pageSize: Int = 20) =
        apiManager.getAnnouncements(type, priority, page, pageSize)

    suspend fun getAnnouncementDetail(id: String) = apiManager.getAnnouncementDetail(id)

    // 规则相关
    suspend fun getRules(studyRoomId: String? = null, category: String? = null) =
        apiManager.getRules(studyRoomId, category)

    suspend fun getRuleDetail(id: String) = apiManager.getRuleDetail(id)

    // AI聊天相关
    suspend fun getAiCharacters() = apiManager.getAiCharacters()
    suspend fun sendAiMessage(message: String, sessionId: String? = null, characterId: String? = null) =
        apiManager.sendAiMessage(message, sessionId, characterId)

    // 智能客服相关
    suspend fun getCustomerServiceWelcome() = apiManager.getCustomerServiceWelcome()
    suspend fun customerServiceChat(sessionId: String, message: String) =
        apiManager.customerServiceChat(sessionId, message)
    suspend fun getCustomerServiceHistory(sessionId: String) =
        apiManager.getCustomerServiceHistory(sessionId)
}
