package com.example.scylier.istudyspot.infra.network

import com.example.scylier.istudyspot.models.BaseResponse
import com.example.scylier.istudyspot.models.auth.LoginRequest
import com.example.scylier.istudyspot.models.auth.LoginResponse
import com.example.scylier.istudyspot.models.auth.RegisterRequest
import com.example.scylier.istudyspot.models.auth.RegisterResponse
import com.example.scylier.istudyspot.models.auth.TokenResponse
import com.example.scylier.istudyspot.models.auth.UserInfo
import com.example.scylier.istudyspot.models.order.CancelOrderResponse
import com.example.scylier.istudyspot.models.order.CheckinRequest
import com.example.scylier.istudyspot.models.order.CheckinResponse
import com.example.scylier.istudyspot.models.order.CheckoutResponse
import com.example.scylier.istudyspot.models.order.CreateOrderRequest
import com.example.scylier.istudyspot.models.order.OrderDetail
import com.example.scylier.istudyspot.models.order.OrderListResponse
import com.example.scylier.istudyspot.models.order.OrderResponse
import com.example.scylier.istudyspot.models.payment.CreatePaymentRequest
import com.example.scylier.istudyspot.models.payment.PaymentResponse
import com.example.scylier.istudyspot.models.payment.PaymentStatusResponse
import com.example.scylier.istudyspot.models.statistics.StudyRoomStatisticsResponse
import com.example.scylier.istudyspot.models.studyroom.SeatDetail
import com.example.scylier.istudyspot.models.studyroom.SeatMapResponse
import com.example.scylier.istudyspot.models.studyroom.StudyRoomDetail
import com.example.scylier.istudyspot.models.studyroom.StudyRoomListResponse
import com.example.scylier.istudyspot.models.ai.AiChatRequest
import com.example.scylier.istudyspot.models.ai.AiChatResponse
import com.example.scylier.istudyspot.models.user.ChangePasswordRequest
import com.example.scylier.istudyspot.models.user.UpdateUserRequest
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // 认证相关 API
    @POST("/api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<BaseResponse<LoginResponse>>

    @POST("/api/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<BaseResponse<RegisterResponse>>

    @POST("/api/auth/refresh")
    suspend fun refreshToken(): Response<BaseResponse<TokenResponse>>

    // 自习室相关 API
    @GET("/api/studyrooms")
    suspend fun getStudyRooms(@Query("page") page: Int = 1, @Query("size") size: Int = 10): Response<BaseResponse<StudyRoomListResponse>>

    @GET("/api/studyrooms/{id}")
    suspend fun getStudyRoomDetail(@Path("id") id: String): Response<BaseResponse<StudyRoomDetail>>

    // 座位相关 API
    @GET("/api/studyrooms/{id}/seats")
    suspend fun getStudyRoomSeats(@Path("id") id: String): Response<BaseResponse<SeatMapResponse>>

    @GET("/api/seats/{id}")
    suspend fun getSeatDetail(@Path("id") id: String): Response<BaseResponse<SeatDetail>>

    // 订单相关 API
    @POST("/api/orders")
    suspend fun createOrder(@Body orderRequest: CreateOrderRequest): Response<BaseResponse<OrderResponse>>

    @GET("/api/users/me/orders")
    suspend fun getUserOrders(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Response<BaseResponse<OrderListResponse>>

    @GET("/api/orders/{id}")
    suspend fun getOrderDetail(@Path("id") id: String): Response<BaseResponse<OrderDetail>>

    @PUT("/api/orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: String): Response<BaseResponse<CancelOrderResponse>>

    // 签到/签退相关 API
    @POST("/api/orders/{id}/checkin")
    suspend fun checkin(@Path("id") id: String, @Body checkinRequest: CheckinRequest): Response<BaseResponse<CheckinResponse>>

    @POST("/api/orders/{id}/checkout")
    suspend fun checkout(@Path("id") id: String): Response<BaseResponse<CheckoutResponse>>

    // 用户相关 API
    @GET("/api/users/me")
    suspend fun getUserInfo(): Response<BaseResponse<UserInfo>>

    @PUT("/api/users/me")
    suspend fun updateUserInfo(@Body updateRequest: UpdateUserRequest): Response<BaseResponse<UserInfo>>

    @PUT("/api/users/me/password")
    suspend fun changePassword(@Body passwordRequest: ChangePasswordRequest): Response<BaseResponse<Unit>>

    // 支付相关 API
    @POST("/api/payments")
    suspend fun createPayment(@Body paymentRequest: CreatePaymentRequest): Response<BaseResponse<PaymentResponse>>

    @GET("/api/payments/{id}")
    suspend fun getPaymentStatus(@Path("id") id: String): Response<BaseResponse<PaymentStatusResponse>>

    // 统计相关 API
    @GET("/api/studyrooms/{id}/statistics")
    suspend fun getStudyRoomStatistics(
        @Path("id") id: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<BaseResponse<StudyRoomStatisticsResponse>>

    // AI咨询相关 API
    @POST("/api/ai/chat")
    suspend fun sendAiMessage(@Body request: AiChatRequest): Response<BaseResponse<AiChatResponse>>
}
