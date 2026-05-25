package com.example.scylier.istudyspot.infra.network

import com.example.scylier.istudyspot.BuildConfig
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class ApiManager {
    private val apiService = ApiClient.apiService
    private val useMockData = BuildConfig.USE_MOCK

    suspend fun <T> executeRequest(request: suspend () -> Response<BaseResponse<T>>): ApiResponse<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = request()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        if (body.code in 200..299) {
                            @Suppress("UNCHECKED_CAST")
                            val safeData = body.data as? T
                            if (safeData != null) {
                                ApiResponse.Success(body.code, body.message, safeData)
                            } else {
                                @Suppress("UNCHECKED_CAST")
                                ApiResponse.Success(body.code, body.message, Unit as T)
                            }
                        } else {
                            ApiResponse.Error(body.code, body.message)
                        }
                    } else {
                        ApiResponse.Error(500, "服务器响应为空")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    ApiResponse.Error(response.code(), parseErrorMessage(errorBody))
                }
            } catch (e: SocketTimeoutException) {
                ApiResponse.Error(408, "网络请求超时，请检查网络连接")
            } catch (e: IOException) {
                ApiResponse.Error(408, "网络连接异常: ${e.message}")
            } catch (e: Exception) {
                ApiResponse.Error(500, "网络请求异常: ${e.message}")
            }
        }
    }

    suspend fun <T> executeRawRequest(request: suspend () -> Response<T>): ApiResponse<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = request()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        ApiResponse.Success(200, "success", body)
                    } else {
                        ApiResponse.Error(500, "服务器响应为空")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    ApiResponse.Error(response.code(), parseErrorMessage(errorBody))
                }
            } catch (e: SocketTimeoutException) {
                ApiResponse.Error(408, "网络请求超时，请检查网络连接")
            } catch (e: IOException) {
                ApiResponse.Error(408, "网络连接异常: ${e.message}")
            } catch (e: Exception) {
                ApiResponse.Error(500, "网络请求异常: ${e.message}")
            }
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "网络请求失败"
        if (errorBody.trim().startsWith("<") || errorBody.contains("<!DOCTYPE") || errorBody.contains("<html")) {
            return "服务器处理异常"
        }
        return try {
            val json = org.json.JSONObject(errorBody)
            json.optString("message", json.optString("error", "网络请求失败"))
        } catch (e: Exception) {
            if (errorBody.length > 100) errorBody.substring(0, 100) + "..." else errorBody
        }
    }

    suspend fun login(username: String, password: String) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "登录成功",
                    data = com.example.scylier.istudyspot.models.auth.LoginResponse(
                        token = "mock_token",
                        user = com.example.scylier.istudyspot.models.auth.UserInfo(
                            id = 1L,
                            username = username,
                            nickname = "测试用户",
                            avatar = "https://example.com/avatar.jpg"
                        )
                    )
                )
            )
        }
        apiService.login(com.example.scylier.istudyspot.models.auth.LoginRequest(username, password))
    }

    suspend fun register(username: String, password: String, nickname: String) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 201,
                    message = "注册成功",
                    data = com.example.scylier.istudyspot.models.auth.RegisterResponse(
                        token = "mock_token",
                        user = com.example.scylier.istudyspot.models.auth.UserInfo(
                            id = 1L,
                            username = username,
                            nickname = nickname,
                            avatar = "https://example.com/avatar.jpg"
                        )
                    )
                )
            )
        }
        apiService.register(com.example.scylier.istudyspot.models.auth.RegisterRequest(username, password, nickname))
    }

    suspend fun refreshToken(refreshToken: String) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "刷新成功",
                    data = com.example.scylier.istudyspot.models.auth.TokenResponse(
                        token = "mock_new_token"
                    )
                )
            )
        }
        apiService.refreshToken(mapOf("refreshToken" to refreshToken))
    }

    suspend fun logout() = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse<Unit>(
                    code = 200,
                    message = "登出成功",
                    data = Unit
                )
            )
        }
        apiService.logout()
    }

    suspend fun getStudyRooms(page: Int = 1, pageSize: Int = 20, status: String? = null, keyword: String? = null) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = com.example.scylier.istudyspot.models.studyroom.StudyRoomListResponse(
                        total = 2,
                        list = listOf(
                            com.example.scylier.istudyspot.models.studyroom.StudyRoomItem(
                                id = 1L,
                                name = "自习室1",
                                address = "图书馆三楼",
                                openTime = "08:00",
                                closeTime = "22:00",
                                imageUrl = "https://example.com/room1.jpg"
                            ),
                            com.example.scylier.istudyspot.models.studyroom.StudyRoomItem(
                                id = 2L,
                                name = "自习室2",
                                address = "图书馆四楼",
                                openTime = "08:00",
                                closeTime = "22:00",
                                imageUrl = "https://example.com/room2.jpg"
                            )
                        )
                    )
                )
            )
        }
        apiService.getStudyRooms(page, pageSize, status, null, keyword)
    }

    suspend fun getStudyRoomDetail(id: Long) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = com.example.scylier.istudyspot.models.studyroom.StudyRoomDetail(
                        id = id,
                        name = "自习室$id",
                        address = "图书馆${id}楼",
                        openTime = "08:00",
                        closeTime = "22:00",
                        description = "安静舒适的学习环境",
                        rules = "禁止大声喧哗，保持卫生",
                        imageUrl = "https://example.com/room$id.jpg"
                    )
                )
            )
        }
        apiService.getStudyRoomDetail(id)
    }

    suspend fun getStudyRoomSeats(id: Long, status: String? = null, type: String? = null) = executeRequest {
        if (useMockData) {
            val seats = mutableListOf<com.example.scylier.istudyspot.models.studyroom.SeatInfo>()
            for (row in 1..5) {
                for (col in 1..8) {
                    seats.add(
                        com.example.scylier.istudyspot.models.studyroom.SeatInfo(
                            id = (row * 10 + col).toLong(),
                            rowNum = row,
                            colNum = col,
                            status = if ((row + col) % 3 == 0) "booked" else if ((row + col) % 4 == 0) "in_use" else "available",
                            seatType = if (col == 1) 2 else 1,
                            pricePerHour = if (col == 1) 15.0 else 10.0
                        )
                    )
                }
            }
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = seats
                )
            )
        }
        apiService.getStudyRoomSeats(id, status, type)
    }

    suspend fun getSeatDetail(id: Long) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = com.example.scylier.istudyspot.models.studyroom.SeatDetail(
                        id = id,
                        roomId = 1L,
                        rowNum = 1,
                        colNum = 1,
                        status = "available",
                        seatType = 1,
                        pricePerHour = 10.0,
                        description = "普通座位"
                    )
                )
            )
        }
        apiService.getSeatDetail(id)
    }

    suspend fun createOrder(studyRoomId: Long, seatId: Long, startTime: String, endTime: String, bookingType: String) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 201,
                    message = "订单创建成功",
                    data = com.example.scylier.istudyspot.models.order.OrderResponse(
                        id = System.currentTimeMillis(),
                        seatId = seatId,
                        userId = 1L,
                        startTime = startTime,
                        endTime = endTime,
                        totalPrice = 20.0,
                        status = "pending",
                        createdAt = "2026-10-01T09:00:00"
                    )
                )
            )
        }
        apiService.createOrder(com.example.scylier.istudyspot.models.order.CreateOrderRequest(studyRoomId, seatId, startTime, endTime, bookingType))
    }

    suspend fun getUserOrders(status: String? = null, startDate: String? = null, endDate: String? = null, page: Int = 1, pageSize: Int = 20) = executeRequest {
        if (useMockData) {
            val orders = listOf(
                com.example.scylier.istudyspot.models.order.OrderItem(
                    id = 1L,
                    seatId = 1L,
                    studyRoomName = "自习室1",
                    seatPosition = "1-1",
                    startTime = "2026-10-01T10:00:00",
                    endTime = "2026-10-01T12:00:00",
                    totalPrice = 20.0,
                    status = "pending",
                    createdAt = "2026-10-01T09:00:00"
                ),
                com.example.scylier.istudyspot.models.order.OrderItem(
                    id = 2L,
                    seatId = 2L,
                    studyRoomName = "自习室2",
                    seatPosition = "2-3",
                    startTime = "2026-10-02T14:00:00",
                    endTime = "2026-10-02T16:00:00",
                    totalPrice = 20.0,
                    status = "paid",
                    createdAt = "2026-10-02T13:00:00"
                )
            )
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = com.example.scylier.istudyspot.models.order.OrderListResponse(
                        total = orders.size,
                        list = orders
                    )
                )
            )
        }
        apiService.getUserOrders(status, startDate, endDate, page, pageSize)
    }

    suspend fun getOrderDetail(id: Long) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = com.example.scylier.istudyspot.models.order.OrderDetail(
                        id = id,
                        seatId = 1L,
                        userId = 1L,
                        studyRoomName = "自习室1",
                        seatPosition = "1-1",
                        startTime = "2026-10-01T10:00:00",
                        endTime = "2026-10-01T12:00:00",
                        totalPrice = 20.0,
                        status = "paid",
                        createdAt = "2026-10-01T09:00:00",
                        updatedAt = "2026-10-01T09:00:00"
                    )
                )
            )
        }
        apiService.getOrderDetail(id)
    }

    suspend fun cancelOrder(id: Long) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "订单取消成功",
                    data = com.example.scylier.istudyspot.models.order.CancelOrderResponse(
                        id = id,
                        status = "cancelled"
                    )
                )
            )
        }
        apiService.cancelOrder(id)
    }

    suspend fun payOrder(id: Long) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "支付成功",
                    data = mapOf("orderId" to id, "status" to "paid")
                )
            )
        }
        apiService.payOrder(id)
    }

    suspend fun getReservationRules() = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = mapOf(
                        "maxAdvanceDays" to 7,
                        "maxDailyReservations" to 2,
                        "maxDurationHours" to 4,
                        "minDurationMinutes" to 30,
                        "cancellationDeadlineMinutes" to 15,
                        "noShowPenalty" to 5
                    )
                )
            )
        }
        apiService.getReservationRules()
    }

    suspend fun checkin(reservationId: Long, seatId: Long) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "签到成功",
                    data = com.example.scylier.istudyspot.models.order.CheckinResponse(
                        id = reservationId,
                        checkinTime = "2026-10-01T10:00:00",
                        status = "in_use"
                    )
                )
            )
        }
        apiService.checkin(mapOf("reservationId" to reservationId.toString(), "seatId" to seatId.toString()))
    }

    suspend fun checkout(checkInRecordId: Long) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "签退成功",
                    data = com.example.scylier.istudyspot.models.order.CheckoutResponse(
                        id = checkInRecordId,
                        checkoutTime = "2026-10-01T12:00:00",
                        actualDuration = 120,
                        actualPrice = 20.0,
                        status = "completed"
                    )
                )
            )
        }
        apiService.checkout(mapOf("checkInRecordId" to checkInRecordId.toString()))
    }

    suspend fun getCheckinRecords(startDate: String? = null, endDate: String? = null, page: Int = 1, pageSize: Int = 20) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = mapOf("list" to emptyList<Any>(), "total" to 0, "page" to page, "pageSize" to pageSize)
                )
            )
        }
        apiService.getCheckinRecords(startDate, endDate, page, pageSize)
    }

    suspend fun getCurrentCheckin() = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = mapOf("isCheckedIn" to false, "checkInRecord" to null)
                )
            )
        }
        apiService.getCurrentCheckin()
    }

    suspend fun getUserInfo() = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = com.example.scylier.istudyspot.models.auth.UserInfo(
                        id = 1L,
                        username = "test",
                        nickname = "测试用户",
                        avatar = "https://example.com/avatar.jpg",
                        phone = "13800138000",
                        email = "test@example.com"
                    )
                )
            )
        }
        apiService.getUserInfo()
    }

    suspend fun updateUserInfo(nickname: String? = null, avatar: String? = null, phone: String? = null, email: String? = null) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "更新成功",
                    data = com.example.scylier.istudyspot.models.auth.UserInfo(
                        id = 1L,
                        username = "test",
                        nickname = nickname ?: "测试用户",
                        avatar = avatar ?: "https://example.com/avatar.jpg",
                        phone = phone ?: "13800138000",
                        email = email ?: "test@example.com"
                    )
                )
            )
        }
        apiService.updateUserInfo(com.example.scylier.istudyspot.models.user.UpdateUserRequest(nickname, avatar, phone, email))
    }

    suspend fun changePassword(oldPassword: String, newPassword: String) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "密码修改成功",
                    data = Unit
                )
            )
        }
        apiService.changePassword(com.example.scylier.istudyspot.models.user.ChangePasswordRequest(oldPassword, newPassword))
    }

    suspend fun createPayment(orderId: Long, amount: Double, paymentMethod: String) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 201,
                    message = "支付创建成功",
                    data = com.example.scylier.istudyspot.models.payment.PaymentResponse(
                        id = System.currentTimeMillis(),
                        orderId = orderId,
                        amount = amount,
                        paymentMethod = paymentMethod,
                        paymentUrl = "https://example.com/pay",
                        createdAt = "2026-10-01T09:00:00"
                    )
                )
            )
        }
        apiService.createPayment(com.example.scylier.istudyspot.models.payment.CreatePaymentRequest(orderId, amount, paymentMethod))
    }

    suspend fun getPaymentStatus(id: Long) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = com.example.scylier.istudyspot.models.payment.PaymentStatusResponse(
                        id = id,
                        orderId = 1L,
                        amount = 20.0,
                        paymentMethod = "wechat",
                        status = "success",
                        createdAt = "2026-10-01T09:00:00",
                        updatedAt = "2026-10-01T09:01:00"
                    )
                )
            )
        }
        apiService.getPaymentStatus(id)
    }

    suspend fun getStudyRoomStatistics(id: Long, startDate: String, endDate: String) = executeRequest {
        if (useMockData) {
            val dailyData = listOf(
                com.example.scylier.istudyspot.models.statistics.DailyData(
                    date = "2026-10-01",
                    occupancyRate = 0.8,
                    bookings = 20,
                    revenue = 400.0
                ),
                com.example.scylier.istudyspot.models.statistics.DailyData(
                    date = "2026-10-02",
                    occupancyRate = 0.7,
                    bookings = 15,
                    revenue = 300.0
                )
            )
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = com.example.scylier.istudyspot.models.statistics.StudyRoomStatisticsResponse(
                        studyRoomId = id,
                        studyRoomName = "自习室$id",
                        totalSeats = 40,
                        avgOccupancyRate = 0.75,
                        totalBookings = 100,
                        totalRevenue = 2000.0,
                        dailyData = dailyData
                    )
                )
            )
        }
        apiService.getStudyRoomStatistics(id, startDate, endDate)
    }

    suspend fun getAnnouncements(type: String? = null, priority: String? = null, page: Int = 1, pageSize: Int = 20) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = mapOf("list" to emptyList<Any>(), "total" to 0, "page" to page, "pageSize" to pageSize)
                )
            )
        }
        apiService.getAnnouncements(type, priority, page, pageSize)
    }

    suspend fun getAnnouncementDetail(id: Long) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = mapOf("id" to id, "title" to "系统公告", "content" to "欢迎使用iStudySpot", "type" to "system", "priority" to "high")
                )
            )
        }
        apiService.getAnnouncementDetail(id)
    }

    suspend fun getRules(studyRoomId: String? = null, category: String? = null) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = emptyList<Map<String, Any?>>()
                )
            )
        }
        apiService.getRules(studyRoomId, category)
    }

    suspend fun getRuleDetail(id: Long) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = mapOf("id" to id, "title" to "预约规则", "content" to "请遵守预约规则", "category" to "booking")
                )
            )
        }
        apiService.getRuleDetail(id)
    }

    suspend fun getAiCharacters() = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = listOf(
                        mapOf("id" to 1L, "name" to "学习助手", "persona" to "friendly", "speaking_style" to "casual")
                    )
                )
            )
        }
        apiService.getAiCharacters()
    }

    suspend fun sendAiMessage(message: String, sessionId: String? = null, characterId: String? = null): ApiResponse<com.example.scylier.istudyspot.models.ai.AiChatResponse> {
        val result = executeRequest {
            if (useMockData) {
                return@executeRequest Response.success(
                    BaseResponse(
                        code = 200,
                        message = "success",
                        data = mapOf(
                            "reply" to "这是AI助手的回复",
                            "session_id" to (sessionId ?: "session_${System.currentTimeMillis()}")
                        )
                    )
                )
            }
            apiService.sendAiMessage(com.example.scylier.istudyspot.models.ai.AiChatRequest(message, sessionId, characterId))
        }
        return when (result) {
            is ApiResponse.Success -> {
                val map = result.data
                ApiResponse.Success(result.code, result.message, com.example.scylier.istudyspot.models.ai.AiChatResponse(
                    reply = map["reply"] as? String ?: "",
                    session_id = map["session_id"] as? String
                ))
            }
            is ApiResponse.Error -> result
        }
    }

    suspend fun getCustomerServiceWelcome() = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = mapOf("welcomeMessage" to "您好！我是iStudySpot智能客服，有什么可以帮您？", "recommendedQuestions" to listOf("如何预约座位？", "自习室开放时间？"))
                )
            )
        }
        apiService.getCustomerServiceWelcome()
    }

    suspend fun customerServiceChat(sessionId: String, message: String) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "success",
                    data = mapOf("response" to "这是智能客服的回复")
                )
            )
        }
        apiService.customerServiceChat(mapOf("sessionId" to sessionId, "message" to message))
    }

    suspend fun getCustomerServiceHistory(sessionId: String) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = mapOf("messages" to emptyList<Any>())
                )
            )
        }
        apiService.getCustomerServiceHistory(sessionId)
    }
}
