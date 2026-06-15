package com.example.scylier.istudyspot.repository

import com.example.scylier.istudyspot.infra.network.ApiManager
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.agent.AgentChatResponse
import com.example.scylier.istudyspot.models.agent.AgentToolExecutionResult

class MainRepository {
    private val apiManager = ApiManager()

    suspend fun login(username: String, password: String) = apiManager.login(username, password)
    suspend fun register(username: String, password: String, nickname: String, phone: String? = null, studentId: String? = null) =
        apiManager.register(username, password, nickname, phone, studentId)
    suspend fun refreshToken(refreshToken: String) = apiManager.refreshToken(refreshToken)
    suspend fun logout() = apiManager.logout()

    suspend fun getStudyRooms(page: Int = 1, pageSize: Int = 20, status: String? = null, keyword: String? = null) =
        apiManager.getStudyRooms(page, pageSize, status, keyword)
    suspend fun getStudyRoomDetail(id: Long) = apiManager.getStudyRoomDetail(id)

    suspend fun getStudyRoomSeats(id: Long, status: String? = null, type: String? = null) =
        apiManager.getStudyRoomSeats(id, status, type)
    suspend fun getStudyRoomSeatLayout(id: Long) = apiManager.getStudyRoomSeatLayout(id)
    suspend fun getSeatDetail(id: Long) = apiManager.getSeatDetail(id)

    suspend fun createOrder(studyRoomId: Long, seatId: Long, startTime: String, endTime: String, bookingType: String) =
        apiManager.createOrder(studyRoomId, seatId, startTime, endTime, bookingType)

    suspend fun getUserOrders(status: String? = null, startDate: String? = null, endDate: String? = null, page: Int = 1, pageSize: Int = 20) =
        apiManager.getUserOrders(status, startDate, endDate, page, pageSize)

    suspend fun getOrderDetail(id: Long) = apiManager.getOrderDetail(id)
    suspend fun cancelOrder(id: Long) = apiManager.cancelOrder(id)
    suspend fun payOrder(id: Long) = apiManager.payOrder(id)
    suspend fun renewOrder(orderId: Long, newEndTime: String): ApiResponse<Map<String, Any?>> {
        return try {
            apiManager.renewOrder(orderId, newEndTime)
        } catch (e: Exception) {
            ApiResponse.Error(500, "续时失败: ${e.message}")
        }
    }
    suspend fun getReservationRules() = apiManager.getReservationRules()

    suspend fun checkin(reservationId: Long, seatId: Long) = apiManager.checkin(reservationId, seatId)
    suspend fun checkout(checkInRecordId: Long) = apiManager.checkout(checkInRecordId)
    suspend fun getCheckinRecords(startDate: String? = null, endDate: String? = null, page: Int = 1, pageSize: Int = 20) =
        apiManager.getCheckinRecords(startDate, endDate, page, pageSize)
    suspend fun getCurrentCheckin() = apiManager.getCurrentCheckin()

    suspend fun getUserInfo() = apiManager.getUserInfo()
    suspend fun updateUserInfo(nickname: String? = null, avatar: String? = null, phone: String? = null, email: String? = null) =
        apiManager.updateUserInfo(nickname, avatar, phone, email)
    suspend fun changePassword(oldPassword: String, newPassword: String) =
        apiManager.changePassword(oldPassword, newPassword)

    suspend fun createPayment(orderId: Long, amount: Double, paymentMethod: String) =
        apiManager.createPayment(orderId, amount, paymentMethod)
    suspend fun getPaymentStatus(id: Long) = apiManager.getPaymentStatus(id)

    suspend fun getStudyRoomStatistics(id: Long, startDate: String, endDate: String) =
        apiManager.getStudyRoomStatistics(id, startDate, endDate)

    suspend fun getAnnouncements(type: String? = null, priority: String? = null, page: Int = 1, pageSize: Int = 20) =
        apiManager.getAnnouncements(type, priority, page, pageSize)
    suspend fun getAnnouncementDetail(id: Long) = apiManager.getAnnouncementDetail(id)

    suspend fun getRules(studyRoomId: String? = null, category: String? = null) =
        apiManager.getRules(studyRoomId, category)
    suspend fun getRuleDetail(id: Long) = apiManager.getRuleDetail(id)

    suspend fun getAiCharacters() = apiManager.getAiCharacters()
    suspend fun sendAiMessage(message: String, sessionId: String? = null, characterId: String? = null) =
        apiManager.sendAiMessage(message, sessionId, characterId)

    suspend fun getAgentToolCatalog() = apiManager.getAgentToolCatalog()

    suspend fun sendAgentMessage(
        message: String,
        sessionId: String? = null
    ): ApiResponse<AgentChatResponse> = apiManager.sendAgentMessage(message, sessionId)

    suspend fun executeAgentTool(
        tool: String,
        arguments: Map<String, Any?> = emptyMap()
    ): ApiResponse<AgentToolExecutionResult> = apiManager.executeAgentTool(tool, arguments)

    suspend fun getCustomerServiceWelcome() = apiManager.getCustomerServiceWelcome()
    suspend fun customerServiceChat(sessionId: String, message: String) =
        apiManager.customerServiceChat(sessionId, message)
    suspend fun getCustomerServiceHistory(sessionId: String) =
        apiManager.getCustomerServiceHistory(sessionId)

    suspend fun getAchievements() = apiManager.getAchievements()

    suspend fun getViolations() = apiManager.getViolations()
    suspend fun appealViolation(id: Long, reason: String) = apiManager.appealViolation(id, reason)

    suspend fun getCardList(userId: String): ApiResponse<List<com.example.scylier.istudyspot.models.card.CardItem>> {
        return try {
            when (val response = apiManager.getCardList(userId)) {
                is ApiResponse.Success -> {
                    val rawList = response.data ?: emptyList()
                    val cards = rawList.map { map ->
                        com.example.scylier.istudyspot.models.card.CardItem(
                            uuid = map["uuid"] as? String ?: "",
                            rarity = map["rarity"] as? String ?: "N",
                            borderTheme = map["borderTheme"] as? String ?: "",
                            cardTheme = map["cardTheme"] as? String ?: "",
                            themeCategory = map["themeCategory"] as? String ?: "",
                            markdown = map["markdown"] as? String ?: "",
                            studyDuration = (map["studyDuration"] as? Number)?.toInt() ?: 0,
                            createTime = map["createTime"] as? String,
                            imageURL = map["imageURL"] as? String
                        )
                    }
                    ApiResponse.Success(response.code, response.message, cards)
                }
                is ApiResponse.Error -> response
            }
        } catch (e: Exception) {
            ApiResponse.Error(500, "获取卡片列表失败: ${e.message}")
        }
    }
}
