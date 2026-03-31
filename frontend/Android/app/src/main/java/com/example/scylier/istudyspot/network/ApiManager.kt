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
        apiService.login(com.example.scylier.istudyspot.models.auth.LoginRequest(username, password))
    }

    suspend fun register(username: String, password: String, nickname: String) = executeRequest {
        apiService.register(com.example.scylier.istudyspot.models.auth.RegisterRequest(username, password, nickname))
    }

    suspend fun refreshToken() = executeRequest {
        apiService.refreshToken()
    }

    // 自习室相关 API
    suspend fun getStudyRooms(page: Int = 1, size: Int = 10) = executeRequest {
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
