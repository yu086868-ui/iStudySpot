package com.example.scylier.istudyspot.network

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
        apiService.getStudyRoomDetail(id)
    }

    // 座位相关 API
    suspend fun getStudyRoomSeats(id: String) = executeRequest {
        apiService.getStudyRoomSeats(id)
    }

    suspend fun getSeatDetail(id: String) = executeRequest {
        apiService.getSeatDetail(id)
    }

    // 订单相关 API
    suspend fun createOrder(seatId: String, startTime: String, endTime: String, bookingType: String) = executeRequest {
        apiService.createOrder(com.example.scylier.istudyspot.models.order.CreateOrderRequest(seatId, startTime, endTime, bookingType))
    }

    suspend fun getUserOrders(status: String? = null, page: Int = 1, size: Int = 10) = executeRequest {
        apiService.getUserOrders(status, page, size)
    }

    suspend fun getOrderDetail(id: String) = executeRequest {
        apiService.getOrderDetail(id)
    }

    suspend fun cancelOrder(id: String) = executeRequest {
        apiService.cancelOrder(id)
    }

    // 签到/签退相关 API
    suspend fun checkin(id: String, checkinCode: String) = executeRequest {
        apiService.checkin(id, com.example.scylier.istudyspot.models.order.CheckinRequest(checkinCode))
    }

    suspend fun checkout(id: String) = executeRequest {
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
        apiService.updateUserInfo(com.example.scylier.istudyspot.models.user.UpdateUserRequest(nickname, avatar, phone, email))
    }

    suspend fun changePassword(oldPassword: String, newPassword: String) = executeRequest {
        apiService.changePassword(com.example.scylier.istudyspot.models.user.ChangePasswordRequest(oldPassword, newPassword))
    }

    // 支付相关 API
    suspend fun createPayment(orderId: String, amount: Double, paymentMethod: String) = executeRequest {
        apiService.createPayment(com.example.scylier.istudyspot.models.payment.CreatePaymentRequest(orderId, amount, paymentMethod))
    }

    suspend fun getPaymentStatus(id: String) = executeRequest {
        apiService.getPaymentStatus(id)
    }

    // 统计相关 API
    suspend fun getStudyRoomStatistics(id: String, startDate: String, endDate: String) = executeRequest {
        apiService.getStudyRoomStatistics(id, startDate, endDate)
    }
}
