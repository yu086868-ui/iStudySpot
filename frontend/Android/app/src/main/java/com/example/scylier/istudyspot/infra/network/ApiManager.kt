package com.example.scylier.istudyspot.infra.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class ApiManager(private val token: String? = null, private val context: Context? = null) {
    private val apiService = ApiClient.createService(ApiService::class.java, token)
    private val useMockData = true // 控制是否使用Mock数据

    suspend fun <T> executeRequest(request: suspend () -> Response<BaseResponse<T>>): ApiResponse<T> {
        return withContext(Dispatchers.IO) {
            try {
                // 检查网络状态
                if (context != null && !isNetworkAvailable(context)) {
                    return@withContext ApiResponse.Error(408, "网络连接不可用，请检查网络设置")
                }

                val response = request()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        if (body.code in 200..299) {
                            ApiResponse.Success(body.code, body.message, body.data!!)
                        } else {
                            ApiResponse.Error(body.code, body.message)
                        }
                    } else {
                        ApiResponse.Error(500, "服务器响应为空")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    ApiResponse.Error(response.code(), errorBody ?: "网络请求失败")
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



    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && (
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        )
    }

    // 认证相关 API
    suspend fun login(username: String, password: String) = executeRequest {
        // 为login提供专门的Mock数据
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "登录成功",
                    data = com.example.scylier.istudyspot.models.auth.LoginResponse(
                        token = "mock_token",
                        user = com.example.scylier.istudyspot.models.auth.UserInfo(
                            id = "1",
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
        // 为register提供专门的Mock数据
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 201,
                    message = "注册成功",
                    data = com.example.scylier.istudyspot.models.auth.RegisterResponse(
                        token = "mock_token",
                        user = com.example.scylier.istudyspot.models.auth.UserInfo(
                            id = "1",
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

    suspend fun refreshToken() = executeRequest {
        // 为refreshToken提供专门的Mock数据
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "刷新成功",
                    data = com.example.scylier.istudyspot.models.auth.TokenResponse(
                        token = "mock_token"
                    )
                )
            )
        }
        apiService.refreshToken()
    }

    // 自习室相关 API
    suspend fun getStudyRooms(page: Int = 1, size: Int = 10) = executeRequest {
        // 为getStudyRooms提供专门的Mock数据
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = com.example.scylier.istudyspot.models.studyroom.StudyRoomListResponse(
                        total = 2,
                        list = listOf(
                            com.example.scylier.istudyspot.models.studyroom.StudyRoomItem(
                                id = "1",
                                name = "自习室1",
                                address = "图书馆三楼",
                                openingHours = "08:00-22:00",
                                occupancyRate = 0.8,
                                imageUrl = "https://example.com/room1.jpg"
                            ),
                            com.example.scylier.istudyspot.models.studyroom.StudyRoomItem(
                                id = "2",
                                name = "自习室2",
                                address = "图书馆四楼",
                                openingHours = "08:00-22:00",
                                occupancyRate = 0.6,
                                imageUrl = "https://example.com/room2.jpg"
                            )
                        )
                    )
                )
            )
        }
        apiService.getStudyRooms(page, size)
    }

    suspend fun getStudyRoomDetail(id: String) = executeRequest {
        // 为getStudyRoomDetail提供专门的Mock数据
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = com.example.scylier.istudyspot.models.studyroom.StudyRoomDetail(
                        id = id,
                        name = "自习室$id",
                        address = "图书馆${id}楼",
                        openingHours = "08:00-22:00",
                        description = "安静舒适的学习环境",
                        rules = "禁止大声喧哗，保持卫生",
                        imageUrl = "https://example.com/room$id.jpg"
                    )
                )
            )
        }
        apiService.getStudyRoomDetail(id)
    }

    // 座位相关 API
    suspend fun getStudyRoomSeats(id: String) = executeRequest {
        // 为getStudyRoomSeats提供专门的Mock数据
        if (useMockData) {
            val seats = mutableListOf<com.example.scylier.istudyspot.models.studyroom.SeatInfo>()
            for (row in 1..5) {
                for (col in 1..8) {
                    seats.add(
                        com.example.scylier.istudyspot.models.studyroom.SeatInfo(
                            id = "${id}_${row}_${col}",
                            row = row,
                            col = col,
                            status = if ((row + col) % 3 == 0) "booked" else if ((row + col) % 4 == 0) "occupied" else "available",
                            type = if (col == 1) "vip" else "normal",
                            pricePerHour = if (col == 1) 15.0 else 10.0
                        )
                    )
                }
            }
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = com.example.scylier.istudyspot.models.studyroom.SeatMapResponse(
                        studyRoomId = id,
                        rows = 5,
                        cols = 8,
                        seats = seats
                    )
                )
            )
        }
        apiService.getStudyRoomSeats(id)
    }

    suspend fun getSeatDetail(id: String) = executeRequest {
        // 为getSeatDetail提供专门的Mock数据
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = com.example.scylier.istudyspot.models.studyroom.SeatDetail(
                        id = id,
                        studyRoomId = "1",
                        row = 1,
                        col = 1,
                        status = "available",
                        type = "normal",
                        pricePerHour = 10.0,
                        description = "普通座位"
                    )
                )
            )
        }
        apiService.getSeatDetail(id)
    }

    // 订单相关 API
    suspend fun createOrder(seatId: String, startTime: String, endTime: String, bookingType: String) = executeRequest {
        // 为createOrder提供专门的Mock数据
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 201,
                    message = "订单创建成功",
                    data = com.example.scylier.istudyspot.models.order.OrderResponse(
                        id = "order_${System.currentTimeMillis()}",
                        seatId = seatId,
                        userId = "1",
                        startTime = startTime,
                        endTime = endTime,
                        totalPrice = 20.0,
                        status = "pending",
                        createdAt = "2026-10-01T09:00:00"
                    )
                )
            )
        }
        apiService.createOrder(com.example.scylier.istudyspot.models.order.CreateOrderRequest(seatId, startTime, endTime, bookingType))
    }

    suspend fun getUserOrders(status: String? = null, page: Int = 1, size: Int = 10) = executeRequest {
        // 为getUserOrders提供专门的Mock数据
        if (useMockData) {
            val orders = listOf(
                com.example.scylier.istudyspot.models.order.OrderItem(
                    id = "order1",
                    seatId = "seat1",
                    studyRoomName = "自习室1",
                    seatPosition = "1-1",
                    startTime = "2026-10-01T10:00:00",
                    endTime = "2026-10-01T12:00:00",
                    totalPrice = 20.0,
                    status = "pending",
                    createdAt = "2026-10-01T09:00:00"
                ),
                com.example.scylier.istudyspot.models.order.OrderItem(
                    id = "order2",
                    seatId = "seat2",
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
        apiService.getUserOrders(status, page, size)
    }

    suspend fun getOrderDetail(id: String) = executeRequest {
        // 为getOrderDetail提供专门的Mock数据
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = com.example.scylier.istudyspot.models.order.OrderDetail(
                        id = id,
                        seatId = "seat1",
                        userId = "1",
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

    suspend fun cancelOrder(id: String) = executeRequest {
        // 为cancelOrder提供专门的Mock数据
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

    // 签到/签退相关 API
    suspend fun checkin(id: String, checkinCode: String) = executeRequest {
        // 为checkin提供专门的Mock数据
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "签到成功",
                    data = com.example.scylier.istudyspot.models.order.CheckinResponse(
                        id = id,
                        checkinTime = "2026-10-01T10:00:00",
                        status = "in_use"
                    )
                )
            )
        }
        apiService.checkin(id, com.example.scylier.istudyspot.models.order.CheckinRequest(checkinCode))
    }

    suspend fun checkout(id: String) = executeRequest {
        // 为checkout提供专门的Mock数据
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "签退成功",
                    data = com.example.scylier.istudyspot.models.order.CheckoutResponse(
                        id = id,
                        checkoutTime = "2026-10-01T12:00:00",
                        actualDuration = 120,
                        actualPrice = 20.0,
                        status = "completed"
                    )
                )
            )
        }
        apiService.checkout(id)
    }

    // 用户相关 API
    suspend fun getUserInfo() = executeRequest {
        // 为getUserInfo提供专门的Mock数据
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = com.example.scylier.istudyspot.models.auth.UserInfo(
                        id = "1",
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
        // 为updateUserInfo提供专门的Mock数据
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "更新成功",
                    data = com.example.scylier.istudyspot.models.auth.UserInfo(
                        id = "1",
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
        // 为changePassword提供专门的Mock数据
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

    // 支付相关 API
    suspend fun createPayment(orderId: String, amount: Double, paymentMethod: String) = executeRequest {
        // 为createPayment提供专门的Mock数据
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 201,
                    message = "支付创建成功",
                    data = com.example.scylier.istudyspot.models.payment.PaymentResponse(
                        paymentId = "payment_${System.currentTimeMillis()}",
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

    suspend fun getPaymentStatus(id: String) = executeRequest {
        // 为getPaymentStatus提供专门的Mock数据
        if (useMockData) {
            return@executeRequest Response.success(
                BaseResponse(
                    code = 200,
                    message = "获取成功",
                    data = com.example.scylier.istudyspot.models.payment.PaymentStatusResponse(
                        id = id,
                        orderId = "order1",
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

    // 统计相关 API
    suspend fun getStudyRoomStatistics(id: String, startDate: String, endDate: String) = executeRequest {
        // 为getStudyRoomStatistics提供专门的Mock数据
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
}
