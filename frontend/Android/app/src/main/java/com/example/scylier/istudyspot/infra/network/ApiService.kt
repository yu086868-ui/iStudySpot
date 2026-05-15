package com.example.scylier.istudyspot.infra.network

import com.example.scylier.istudyspot.models.BaseResponse
import com.example.scylier.istudyspot.models.ai.AiChatRequest
import com.example.scylier.istudyspot.models.ai.AiChatResponse
import com.example.scylier.istudyspot.models.auth.LoginRequest
import com.example.scylier.istudyspot.models.auth.LoginResponse
import com.example.scylier.istudyspot.models.auth.RegisterRequest
import com.example.scylier.istudyspot.models.auth.RegisterResponse
import com.example.scylier.istudyspot.models.auth.TokenResponse
import com.example.scylier.istudyspot.models.auth.UserInfo
import com.example.scylier.istudyspot.models.order.CancelOrderResponse
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
    suspend fun refreshToken(@Body body: Map<String, String>): Response<BaseResponse<TokenResponse>>

    @POST("/api/auth/logout")
    suspend fun logout(): Response<BaseResponse<Unit>>

    // 自习室相关 API
    @GET("/api/studyrooms")
    suspend fun getStudyRooms(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("status") status: String? = null,
        @Query("floor") floor: Int? = null,
        @Query("keyword") keyword: String? = null
    ): Response<BaseResponse<StudyRoomListResponse>>

    @GET("/api/studyrooms/{id}")
    suspend fun getStudyRoomDetail(@Path("id") id: String): Response<BaseResponse<StudyRoomDetail>>

    // 座位相关 API
    @GET("/api/studyrooms/{studyRoomId}/seats")
    suspend fun getStudyRoomSeats(
        @Path("studyRoomId") studyRoomId: String,
        @Query("status") status: String? = null,
        @Query("type") type: String? = null
    ): Response<BaseResponse<SeatMapResponse>>

    @GET("/api/seats/{id}")
    suspend fun getSeatDetail(@Path("id") id: String): Response<BaseResponse<SeatDetail>>

    // 预约/订单相关 API
    @POST("/api/reservations")
    suspend fun createOrder(@Body orderRequest: CreateOrderRequest): Response<BaseResponse<OrderResponse>>

    @GET("/api/reservations/my")
    suspend fun getUserOrders(
        @Query("status") status: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<BaseResponse<OrderListResponse>>

    @GET("/api/reservations/{id}")
    suspend fun getOrderDetail(@Path("id") id: String): Response<BaseResponse<OrderDetail>>

    @POST("/api/reservations/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: String): Response<BaseResponse<CancelOrderResponse>>

    @POST("/api/reservations/{id}/pay")
    suspend fun payOrder(@Path("id") id: String): Response<BaseResponse<Map<String, Any?>>>

    @GET("/api/reservations/rules")
    suspend fun getReservationRules(): Response<BaseResponse<Map<String, Any?>>>

    // 签到/签退相关 API
    @POST("/api/checkin")
    suspend fun checkin(@Body body: Map<String, String>): Response<BaseResponse<CheckinResponse>>

    @POST("/api/checkout")
    suspend fun checkout(@Body body: Map<String, String>): Response<BaseResponse<CheckoutResponse>>

    @GET("/api/checkin/records")
    suspend fun getCheckinRecords(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<BaseResponse<Map<String, Any?>>>

    @GET("/api/checkin/current")
    suspend fun getCurrentCheckin(): Response<BaseResponse<Map<String, Any?>>>

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

    // 公告相关 API
    @GET("/api/announcements")
    suspend fun getAnnouncements(
        @Query("type") type: String? = null,
        @Query("priority") priority: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<BaseResponse<Map<String, Any?>>>

    @GET("/api/announcements/{id}")
    suspend fun getAnnouncementDetail(@Path("id") id: String): Response<BaseResponse<Map<String, Any?>>>

    // 规则相关 API
    @GET("/api/rules")
    suspend fun getRules(
        @Query("studyRoomId") studyRoomId: String? = null,
        @Query("category") category: String? = null
    ): Response<BaseResponse<List<Map<String, Any?>>>>

    @GET("/api/rules/{id}")
    suspend fun getRuleDetail(@Path("id") id: String): Response<BaseResponse<Map<String, Any?>>>

    // AI聊天相关 API
    @GET("/api/characters")
    suspend fun getAiCharacters(): Response<BaseResponse<List<Map<String, Any?>>>>

    @POST("/api/chat")
    suspend fun sendAiMessage(@Body request: AiChatRequest): Response<BaseResponse<AiChatResponse>>

    // 智能客服相关 API
    @GET("/api/customer-service/welcome")
    suspend fun getCustomerServiceWelcome(): Response<BaseResponse<Map<String, Any?>>>

    @POST("/api/customer-service/chat")
    suspend fun customerServiceChat(
        @Query("sessionId") sessionId: String,
        @Query("message") message: String
    ): Response<BaseResponse<Map<String, Any?>>>

    @GET("/api/customer-service/history")
    suspend fun getCustomerServiceHistory(
        @Query("sessionId") sessionId: String
    ): Response<BaseResponse<Map<String, Any?>>>
}
