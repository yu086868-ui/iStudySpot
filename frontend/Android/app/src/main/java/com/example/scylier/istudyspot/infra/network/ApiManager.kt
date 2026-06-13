package com.example.scylier.istudyspot.infra.network

import com.example.scylier.istudyspot.BuildConfig
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.BaseResponse
import com.example.scylier.istudyspot.models.agent.AgentChatRequest
import com.example.scylier.istudyspot.models.agent.AgentChatResponse
import com.example.scylier.istudyspot.models.agent.AgentToolDefinition
import com.example.scylier.istudyspot.models.agent.AgentToolExecuteRequest
import com.example.scylier.istudyspot.models.agent.AgentToolExecutionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class ApiManager(
    private val apiService: ApiService = ApiClient.apiService,
    private val useMockData: Boolean = BuildConfig.USE_MOCK
) {

    suspend fun <T> executeRequest(request: suspend () -> Response<BaseResponse<T>>): ApiResponse<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = request()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        if (body.code in 200..299) {
                            ApiResponse.Success(body.code, body.message, body.data)
                        } else {
                            ApiResponse.Error(body.code, body.message)
                        }
                    } else {
                        ApiResponse.Error(500, "Server response is empty")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    ApiResponse.Error(response.code(), parseErrorMessage(errorBody))
                }
            } catch (e: SocketTimeoutException) {
                ApiResponse.Error(408, "Network request timed out")
            } catch (e: IOException) {
                ApiResponse.Error(408, "缃戠粶杩炴帴寮傚父: ${e.message}")
            } catch (e: Exception) {
                ApiResponse.Error(500, "缃戠粶璇锋眰寮傚父: ${e.message}")
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
                        ApiResponse.Error(500, "Server response is empty")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    ApiResponse.Error(response.code(), parseErrorMessage(errorBody))
                }
            } catch (e: SocketTimeoutException) {
                ApiResponse.Error(408, "Network request timed out")
            } catch (e: IOException) {
                ApiResponse.Error(408, "缃戠粶杩炴帴寮傚父: ${e.message}")
            } catch (e: Exception) {
                ApiResponse.Error(500, "缃戠粶璇锋眰寮傚父: ${e.message}")
            }
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "缃戠粶璇锋眰澶辫触"
        if (errorBody.trim().startsWith("<") || errorBody.contains("<!DOCTYPE") || errorBody.contains("<html")) {
            return "Server processing error"
        }
        return try {
            val json = org.json.JSONObject(errorBody)
            json.optString("message", json.optString("error", "缃戠粶璇锋眰澶辫触"))
        } catch (e: Exception) {
            if (errorBody.length > 100) errorBody.substring(0, 100) + "..." else errorBody
        }
    }

    suspend fun login(username: String, password: String) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "鐧诲綍鎴愬姛",
                    data = com.example.scylier.istudyspot.models.auth.LoginResponse(
                        token = "mock_token",
                        user = com.example.scylier.istudyspot.models.auth.UserInfo(
                            id = 1L,
                            username = username,
                            nickname = "娴嬭瘯鐢ㄦ埛",
                            avatar = "https://example.com/avatar.jpg"
                        )
                    )
                )
            )
        }
        apiService.login(com.example.scylier.istudyspot.models.auth.LoginRequest(username, password))
    }

    suspend fun register(username: String, password: String, nickname: String, phone: String? = null, studentId: String? = null) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 201,
                    message = "娉ㄥ唽鎴愬姛",
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
        apiService.register(com.example.scylier.istudyspot.models.auth.RegisterRequest(username, password, nickname, phone, studentId))
    }

    suspend fun refreshToken(refreshToken: String) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "鍒锋柊鎴愬姛",
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
                    message = "鐧诲嚭鎴愬姛",
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
                    message = "鑾峰彇鎴愬姛",
                    data = com.example.scylier.istudyspot.models.studyroom.StudyRoomListResponse(
                        total = 2,
                        list = listOf(
                            com.example.scylier.istudyspot.models.studyroom.StudyRoomItem(
                                id = 1L,
                                name = "Study Room A",
                                address = "Library 3F",
                                openTime = "08:00",
                                closeTime = "22:00",
                                imageUrl = "https://example.com/room1.jpg"
                            ),
                            com.example.scylier.istudyspot.models.studyroom.StudyRoomItem(
                                id = 2L,
                                name = "Study Room B",
                                address = "Library 4F",
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
                    message = "鑾峰彇鎴愬姛",
                    data = com.example.scylier.istudyspot.models.studyroom.StudyRoomDetail(
                        id = id,
                        name = "Study Room $id",
                        address = "Library ${id}F",
                        openTime = "08:00",
                        closeTime = "22:00",
                        description = "瀹夐潤鑸掗€傜殑瀛︿範鐜",
                        rules = "Keep quiet and keep the room clean.",
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
                            pricePerHour = if (col == 1) 15.0 else 10.0,
                            seatNumber = "${('A'.code + row - 1).toChar()}${col.toString().padStart(2, '0')}"
                        )
                    )
                }
            }
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "鑾峰彇鎴愬姛",
                    data = seats
                )
            )
        }
        apiService.getStudyRoomSeats(id, status, type)
    }

    suspend fun getStudyRoomSeatLayout(id: Long) = executeRequest {
        if (useMockData) {
            val seats = listOf(
                com.example.scylier.istudyspot.models.studyroom.SeatInfo(
                    id = 101L, rowNum = 3, colNum = 1, status = "available", seatType = 1, pricePerHour = 15.0,
                    seatNumber = "A01", hasPower = 1, hasLamp = 1, isWindow = 1
                ),
                com.example.scylier.istudyspot.models.studyroom.SeatInfo(
                    id = 102L, rowNum = 3, colNum = 2, status = "booked", seatType = 1, pricePerHour = 15.0,
                    seatNumber = "A02", hasPower = 1, hasLamp = 1, isWindow = 1
                ),
                com.example.scylier.istudyspot.models.studyroom.SeatInfo(
                    id = 103L, rowNum = 6, colNum = 6, status = "available", seatType = 2, pricePerHour = 20.0,
                    seatNumber = "B01", hasPower = 1, hasLamp = 0, isWindow = 0
                )
            )
            val items = listOf(
                com.example.scylier.istudyspot.models.studyroom.SeatLayoutItemInfo(
                    id = 1L, roomId = id, itemType = "front_desk", itemKey = "front-desk", label = "前台",
                    rowNum = 1, colNum = 1, widthUnits = 2, heightUnits = 1, zIndex = 10
                ),
                com.example.scylier.istudyspot.models.studyroom.SeatLayoutItemInfo(
                    id = 2L, roomId = id, itemType = "aisle", itemKey = "main-aisle", label = "走道",
                    rowNum = 2, colNum = 4, widthUnits = 2, heightUnits = 6, zIndex = 1
                ),
                com.example.scylier.istudyspot.models.studyroom.SeatLayoutItemInfo(
                    id = 3L, roomId = id, itemType = "table", itemKey = "table-a", label = "共享长桌A",
                    rowNum = 3, colNum = 1, widthUnits = 3, heightUnits = 2, zIndex = 4
                ),
                com.example.scylier.istudyspot.models.studyroom.SeatLayoutItemInfo(
                    id = 4L, roomId = id, itemType = "window", itemKey = "window", label = "落地窗",
                    rowNum = 2, colNum = 1, widthUnits = 3, heightUnits = 1, zIndex = 5
                ),
                com.example.scylier.istudyspot.models.studyroom.SeatLayoutItemInfo(
                    id = 5L, roomId = id, seatId = 101L, itemType = "seat", itemKey = "seat-101", label = "A01",
                    rowNum = 3, colNum = 1, widthUnits = 1, heightUnits = 1, zIndex = 20
                ),
                com.example.scylier.istudyspot.models.studyroom.SeatLayoutItemInfo(
                    id = 6L, roomId = id, seatId = 102L, itemType = "seat", itemKey = "seat-102", label = "A02",
                    rowNum = 3, colNum = 2, widthUnits = 1, heightUnits = 1, zIndex = 20
                ),
                com.example.scylier.istudyspot.models.studyroom.SeatLayoutItemInfo(
                    id = 7L, roomId = id, seatId = 103L, itemType = "seat", itemKey = "seat-103", label = "B01",
                    rowNum = 6, colNum = 6, widthUnits = 1, heightUnits = 1, zIndex = 20
                )
            )
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = com.example.scylier.istudyspot.models.studyroom.SeatLayoutData(
                        studyRoomId = id,
                        studyRoomName = "复杂布局自习室",
                        rows = 8,
                        cols = 9,
                        cellSize = 40,
                        layoutMode = "hybrid",
                        seats = seats,
                        items = items
                    )
                )
            )
        }
        apiService.getStudyRoomSeatLayout(id)
    }

    suspend fun getSeatDetail(id: Long) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "鑾峰彇鎴愬姛",
                    data = com.example.scylier.istudyspot.models.studyroom.SeatDetail(
                        id = id,
                        roomId = 1L,
                        rowNum = 1,
                        colNum = 1,
                        status = "available",
                        seatType = 1,
                        pricePerHour = 10.0,
                        seatNumber = "A01",
                        description = "Standard seat"
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
                    message = "璁㈠崟鍒涘缓鎴愬姛",
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
                    studyRoomName = "鑷範瀹?",
                    seatPosition = "A01",
                    startTime = "2026-10-01T10:00:00",
                    endTime = "2026-10-01T12:00:00",
                    totalPrice = 20.0,
                    status = "pending",
                    createdAt = "2026-10-01T09:00:00",
                    seatNumber = "A01"
                ),
                com.example.scylier.istudyspot.models.order.OrderItem(
                    id = 2L,
                    seatId = 2L,
                    studyRoomName = "鑷範瀹?",
                    seatPosition = "B03",
                    startTime = "2026-10-02T14:00:00",
                    endTime = "2026-10-02T16:00:00",
                    totalPrice = 20.0,
                    status = "paid",
                    createdAt = "2026-10-02T13:00:00",
                    seatNumber = "B03"
                )
            )
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "鑾峰彇鎴愬姛",
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
                    message = "鑾峰彇鎴愬姛",
                    data = com.example.scylier.istudyspot.models.order.OrderDetail(
                        id = id,
                        seatId = 1L,
                        userId = 1L,
                        studyRoomName = "鑷範瀹?",
                        seatPosition = "A01",
                        startTime = "2026-10-01T10:00:00",
                        endTime = "2026-10-01T12:00:00",
                        totalPrice = 20.0,
                        status = "paid",
                        createdAt = "2026-10-01T09:00:00",
                        updatedAt = "2026-10-01T09:00:00",
                        seatNumber = "A01"
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
                    message = "璁㈠崟鍙栨秷鎴愬姛",
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
                    message = "鏀粯鎴愬姛",
                    data = mapOf("orderId" to id, "status" to "paid", "paymentStatus" to "success")
                )
            )
        }
        apiService.payOrder(id)
    }

    suspend fun renewOrder(orderId: Long, newEndTime: String) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "缁椂鎴愬姛",
                    data = mapOf("orderId" to orderId, "newEndTime" to newEndTime, "additionalAmount" to 10.0)
                )
            )
        }
        apiService.renewOrder(orderId, mapOf("newEndTime" to newEndTime))
    }

    suspend fun getReservationRules() = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "鑾峰彇鎴愬姛",
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
                    message = "绛惧埌鎴愬姛",
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
                    message = "绛鹃€€鎴愬姛",
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
                    message = "鑾峰彇鎴愬姛",
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
                    message = "鑾峰彇鎴愬姛",
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
                    message = "鑾峰彇鎴愬姛",
                    data = com.example.scylier.istudyspot.models.auth.UserInfo(
                        id = 1L,
                        username = "test",
                        nickname = "娴嬭瘯鐢ㄦ埛",
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
                    message = "鏇存柊鎴愬姛",
                    data = com.example.scylier.istudyspot.models.auth.UserInfo(
                        id = 1L,
                        username = "test",
                        nickname = nickname ?: "娴嬭瘯鐢ㄦ埛",
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
                    message = "瀵嗙爜淇敼鎴愬姛",
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
                    message = "鏀粯鎴愬姛",
                    data = com.example.scylier.istudyspot.models.payment.PaymentResponse(
                        paymentId = System.currentTimeMillis().toString(),
                        orderId = orderId,
                        amount = amount,
                        paymentMethod = paymentMethod,
                        status = "success",
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
                    message = "鑾峰彇鎴愬姛",
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
                    message = "鑾峰彇鎴愬姛",
                    data = com.example.scylier.istudyspot.models.statistics.StudyRoomStatisticsResponse(
                        studyRoomId = id,
                        studyRoomName = "鑷範瀹?id",
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
                    message = "鑾峰彇鎴愬姛",
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
                    message = "鑾峰彇鎴愬姛",
                    data = mapOf("id" to id, "title" to "绯荤粺鍏憡", "content" to "娆㈣繋浣跨敤iStudySpot", "type" to "system", "priority" to "high")
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
                    message = "鑾峰彇鎴愬姛",
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
                    message = "鑾峰彇鎴愬姛",
                    data = mapOf("id" to id, "title" to "预约规则", "content" to "请遵守预约规则。", "category" to "booking")
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
                    message = "鑾峰彇鎴愬姛",
                    data = listOf(
                        mapOf("id" to 1L, "name" to "学习助手", "persona" to "友好、谨慎", "speaking_style" to "简洁实用")
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
                            "reply" to "这是智能助手的模拟回复。",
                            "session_id" to (sessionId ?: "session_${System.currentTimeMillis()}")
                        )
                    )
                )
            }
            apiService.sendAiMessage(com.example.scylier.istudyspot.models.ai.AiChatRequest(message, sessionId, characterId))
        }
        return when (result) {
            is ApiResponse.Success -> {
                        val map = result.data ?: emptyMap()
                        ApiResponse.Success(result.code, result.message, com.example.scylier.istudyspot.models.ai.AiChatResponse(
                    reply = map["reply"] as? String ?: "",
                    session_id = map["session_id"] as? String
                ))
            }
            is ApiResponse.Error -> result
        }
    }

    suspend fun getAgentToolCatalog(): ApiResponse<List<AgentToolDefinition>> = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "success",
                    data = listOf(
                        AgentToolDefinition(
                            name = "list_study_rooms",
                            title = "自习室列表",
                            description = "查看可用自习室",
                            requiresAuth = true,
                            tags = listOf("studyroom", "read"),
                            inputSchema = mapOf("keyword" to "string?")
                        ),
                        AgentToolDefinition(
                            name = "list_room_seats",
                            title = "座位列表",
                            description = "查看指定自习室的座位",
                            requiresAuth = true,
                            tags = listOf("seat", "read"),
                            inputSchema = mapOf("studyRoomId" to "number")
                        ),
                        AgentToolDefinition(
                            name = "get_my_reservations",
                            title = "我的预约",
                            description = "查看当前登录用户的预约",
                            requiresAuth = true,
                            tags = listOf("reservation", "read"),
                            inputSchema = emptyMap()
                        ),
                        AgentToolDefinition(
                            name = "get_reservation_rules",
                            title = "预约规则",
                            description = "查看预约与取消规则",
                            requiresAuth = true,
                            tags = listOf("rules", "read"),
                            inputSchema = emptyMap()
                        )
                    )
                )
            )
        }
        apiService.getAgentToolCatalog()
    }

    suspend fun sendAgentMessage(
        message: String,
        sessionId: String? = null
    ): ApiResponse<AgentChatResponse> = executeRequest {
        if (useMockData) {
            val toolResult = AgentToolExecutionResult(
                tool = "get_reservation_rules",
                summary = "已加载预约规则",
                referenceScope = "response",
                data = mapOf(
                    "maxAdvanceDays" to 7,
                    "maxDailyReservations" to 2,
                    "maxDurationHours" to 4,
                    "minDurationMinutes" to 30,
                    "cancellationDeadlineMinutes" to 15,
                    "noShowPenalty" to 5
                ),
                uiAction = com.example.scylier.istudyspot.models.agent.AgentUiAction(
                    type = "navigate",
                    route = "reservation_rules",
                    params = emptyMap()
                )
            )
            val isRuleQuery = message.contains("规则") || message.contains("rule", ignoreCase = true)
            val reply = if (isRuleQuery) {
                "已找到预约规则。你最多可以提前 7 天预约。"
            } else {
                "我可以帮你查询自习室、座位、预约记录和预约规则。"
            }
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "success",
                    data = AgentChatResponse(
                        sessionId = sessionId ?: "agent_session_${System.currentTimeMillis()}",
                        reply = reply,
                        toolResult = if (isRuleQuery) toolResult else null,
                        toolResults = if (isRuleQuery) listOf(toolResult) else emptyList(),
                        suggestedPrompts = listOf("查看我的预约", "查看可用自习室", "查看 1 号自习室的座位")
                    )
                )
            )
        }
        apiService.agentChat(AgentChatRequest(message = message, sessionId = sessionId))
    }

    suspend fun executeAgentTool(
        tool: String,
        arguments: Map<String, Any?> = emptyMap()
    ): ApiResponse<AgentToolExecutionResult> = executeRequest {
        if (useMockData) {
            val result = when (tool) {
                "list_study_rooms" -> AgentToolExecutionResult(
                    tool = tool,
                    summary = "找到 2 间自习室",
                    referenceScope = "response",
                    data = mapOf(
                        "items" to listOf(
                            mapOf(
                                "id" to 1L,
                                "name" to "安静自习室 A",
                                "address" to "图书馆 3 层",
                                "openTime" to "08:00",
                                "closeTime" to "22:00",
                                "status" to 1
                            ),
                            mapOf(
                                "id" to 2L,
                                "name" to "临窗自习室 B",
                                "address" to "图书馆 4 层",
                                "openTime" to "09:00",
                                "closeTime" to "23:00",
                                "status" to 1
                            )
                        ),
                        "page" to 1,
                        "pageSize" to 20,
                        "total" to 2
                    ),
                    uiAction = com.example.scylier.istudyspot.models.agent.AgentUiAction(
                        type = "navigate",
                        route = "studyroom_list",
                        params = emptyMap()
                    )
                )
                "list_room_seats" -> AgentToolExecutionResult(
                    tool = tool,
                    summary = "已加载座位",
                    referenceScope = "response",
                    data = mapOf(
                        "studyRoomId" to (arguments["studyRoomId"] ?: 1L),
                        "items" to listOf(
                            mapOf("id" to 11L, "seatNumber" to "A01", "rowNum" to 1, "colNum" to 1, "status" to "available"),
                            mapOf("id" to 12L, "seatNumber" to "A02", "rowNum" to 1, "colNum" to 2, "status" to "booked")
                        )
                    ),
                    uiAction = com.example.scylier.istudyspot.models.agent.AgentUiAction(
                        type = "navigate",
                        route = "seat_list",
                        params = mapOf("studyRoomId" to (arguments["studyRoomId"] ?: 1L))
                    )
                )
                "get_my_reservations" -> AgentToolExecutionResult(
                    tool = tool,
                    summary = "已加载预约记录",
                    referenceScope = "response",
                    data = mapOf(
                        "items" to listOf(
                            mapOf(
                                "reference" to "ORDER_REF_1",
                                "status" to "paid",
                                "roomName" to "安静自习室 A",
                                "seatPosition" to "A03",
                                "seatNumber" to "A03",
                                "timeRange" to "2026-06-09 19:00:00 - 2026-06-09 21:00:00",
                                "canCancel" to true,
                                "canRenew" to false
                            )
                        ),
                        "page" to 1,
                        "pageSize" to 20,
                        "total" to 1
                    ),
                    uiAction = com.example.scylier.istudyspot.models.agent.AgentUiAction(
                        type = "navigate",
                        route = "reservation_list",
                        params = emptyMap()
                    ),
                    references = mapOf(
                        "ORDER_REF_1" to mapOf(
                            "type" to "reservation",
                        "display" to mapOf(
                            "status" to "paid",
                            "roomName" to "安静自习室 A",
                            "seatPosition" to "A03",
                            "seatNumber" to "A03"
                        )
                    )
                )
                )
                else -> AgentToolExecutionResult(
                    tool = "get_reservation_rules",
                    summary = "已加载预约规则",
                    referenceScope = "response",
                    data = mapOf(
                        "maxAdvanceDays" to 7,
                        "maxDailyReservations" to 2,
                        "maxDurationHours" to 4,
                        "minDurationMinutes" to 30,
                        "cancellationDeadlineMinutes" to 15,
                        "noShowPenalty" to 5
                    ),
                    uiAction = com.example.scylier.istudyspot.models.agent.AgentUiAction(
                        type = "navigate",
                        route = "reservation_rules",
                        params = emptyMap()
                    )
                )
            }
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "success",
                    data = result
                )
            )
        }
        apiService.executeAgentTool(AgentToolExecuteRequest(tool, arguments))
    }

    suspend fun getCardList(userId: String) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "success",
                    data = listOf(
                        mapOf("uuid" to "card1", "rarity" to "SR", "borderTheme" to "Star", "cardTheme" to "Galaxy", "themeCategory" to "Space", "markdown" to "Study under the stars", "studyDuration" to 120, "createTime" to "2026-05-01 10:00:00"),
                        mapOf("uuid" to "card2", "rarity" to "N", "borderTheme" to "Simple", "cardTheme" to "Morning", "themeCategory" to "Nature", "markdown" to "A calm morning study card", "studyDuration" to 30, "createTime" to "2026-05-02 08:00:00"),
                        mapOf("uuid" to "card3", "rarity" to "SSR", "borderTheme" to "Gold", "cardTheme" to "Legend", "themeCategory" to "Legend", "markdown" to "Proof of a legendary study session", "studyDuration" to 600, "createTime" to "2026-05-03 14:00:00")
                    )
                )
            )
        }
        apiService.getCardList(userId)
    }

    suspend fun getCustomerServiceWelcome() = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "鑾峰彇鎴愬姛",
                    data = mapOf("welcomeMessage" to "Hello, I am the iStudySpot support assistant.", "recommendedQuestions" to listOf("How do I reserve a seat?", "When are study rooms open?"))
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
                    data = mapOf("response" to "This is a mock customer service reply.")
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
                    message = "鑾峰彇鎴愬姛",
                    data = mapOf("messages" to emptyList<Any>())
                )
            )
        }
        apiService.getCustomerServiceHistory(sessionId)
    }

    suspend fun getAchievements() = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "鑾峰彇鎴愬姛",
                    data = listOf(
                        mapOf("code" to "early_bird", "name" to "Early bird", "description" to "Check in between 7:00 and 8:00 for 3 days", "icon" to "wb_sunny", "category" to "study", "isUnlocked" to true),
                        mapOf("code" to "night_owl", "name" to "Night owl", "description" to "Study after 21:00 for 3 days", "icon" to "nights_stay", "category" to "study", "isUnlocked" to false),
                        mapOf("code" to "study_master", "name" to "瀛﹂湼", "description" to "绱瀛︿範100灏忔椂", "icon" to "school", "category" to "study", "isUnlocked" to true),
                        mapOf("code" to "streak_king", "name" to "Streak king", "description" to "Check in for 7 consecutive days", "icon" to "local_fire_department", "category" to "study", "isUnlocked" to true),
                        mapOf("code" to "punctual", "name" to "Punctual", "description" to "Check in on time for 30 days", "icon" to "schedule", "category" to "study", "isUnlocked" to false),
                        mapOf("code" to "regular", "name" to "Regular", "description" to "Use the same seat 10 times", "icon" to "event_seat", "category" to "study", "isUnlocked" to false),
                        mapOf("code" to "social", "name" to "Social", "description" to "Invite 3 friends", "icon" to "people", "category" to "social", "isUnlocked" to false),
                        mapOf("code" to "marathon", "name" to "Marathon", "description" to "Study more than 6 hours once", "icon" to "directions_run", "category" to "study", "isUnlocked" to false)
                    )
                )
            )
        }
        apiService.getAchievements()
    }

    suspend fun getViolations() = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "鑾峰彇鎴愬姛",
                    data = mapOf("list" to emptyList<Any>(), "total" to 0)
                )
            )
        }
        apiService.getViolations()
    }

    suspend fun appealViolation(id: Long, reason: String) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse<Unit>(code = 200, message = "Appeal submitted", data = Unit)
            )
        }
        apiService.appealViolation(id, mapOf("reason" to reason))
    }

    // 寰呭姙鐩稿叧
    suspend fun getTodos(status: String? = null) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "鑾峰彇鎴愬姛",
                    data = listOf(
                        com.example.scylier.istudyspot.models.todo.Todo(
                            id = 1L, userId = 1L, title = "Review calculus chapter 3", priority = 1, status = "pending",
                            dueTime = "2026-06-05 18:00:00", createdAt = "2026-06-02 09:00:00"
                        ),
                        com.example.scylier.istudyspot.models.todo.Todo(
                            id = 2L, userId = 1L, title = "瀹屾垚鑻辫闃呰浣滀笟", priority = 2, status = "pending",
                            createdAt = "2026-06-02 09:30:00"
                        ),
                        com.example.scylier.istudyspot.models.todo.Todo(
                            id = 3L, userId = 1L, title = "鏁寸悊鐗╃悊绗旇", priority = 3, status = "completed",
                            completedAt = "2026-06-01 20:00:00", createdAt = "2026-06-01 10:00:00"
                        )
                    )
                )
            )
        }
        apiService.getTodos(status)
    }

    suspend fun createTodo(title: String, priority: Int = 2, dueTime: String? = null, orderId: Long? = null) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "鍒涘缓鎴愬姛",
                    data = com.example.scylier.istudyspot.models.todo.Todo(
                        id = System.currentTimeMillis(), userId = 1L, title = title, priority = priority,
                        status = "pending", dueTime = dueTime, orderId = orderId
                    )
                )
            )
        }
        apiService.createTodo(com.example.scylier.istudyspot.models.todo.CreateTodoRequest(title, priority, dueTime, orderId))
    }

    suspend fun updateTodo(todoId: Long, title: String, priority: Int, dueTime: String? = null, orderId: Long? = null) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "鏇存柊鎴愬姛",
                    data = com.example.scylier.istudyspot.models.todo.Todo(
                        id = todoId, userId = 1L, title = title, priority = priority,
                        status = "pending", dueTime = dueTime, orderId = orderId
                    )
                )
            )
        }
        apiService.updateTodo(todoId, com.example.scylier.istudyspot.models.todo.UpdateTodoRequest(title, priority, dueTime, orderId))
    }

    suspend fun toggleTodo(todoId: Long) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "鎿嶄綔鎴愬姛",
                    data = com.example.scylier.istudyspot.models.todo.Todo(
                        id = todoId, userId = 1L, title = "娴嬭瘯寰呭姙", priority = 2,
                        status = "completed", completedAt = "2026-06-02 10:00:00"
                    )
                )
            )
        }
        apiService.toggleTodo(todoId)
    }

    suspend fun deleteTodo(todoId: Long) = executeRequest {
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse<Unit>(code = 200, message = "鍒犻櫎鎴愬姛", data = Unit)
            )
        }
        apiService.deleteTodo(todoId)
    }
}
