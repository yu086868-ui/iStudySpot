package com.example.scylier.istudyspot.navigation

import kotlinx.serialization.Serializable

sealed class NavRoutes {
    @Serializable
    data object Home : NavRoutes()

    @Serializable
    data object StudyRoom : NavRoutes()

    @Serializable
    data class Seat(
        val studyRoomId: String,
        val studyRoomName: String
    ) : NavRoutes()

    @Serializable
    data class Booking(
        val seatId: String,
        val studyRoomId: String,
        val studyRoomName: String,
        val seatPosition: String,
        val pricePerHour: Double
    ) : NavRoutes()

    @Serializable
    data class Order(
        val orderId: String
    ) : NavRoutes()

    @Serializable
    data object OrderList : NavRoutes()

    @Serializable
    data object Profile : NavRoutes()

    @Serializable
    data object Login : NavRoutes()

    @Serializable
    data object Register : NavRoutes()

    @Serializable
    data object Rules : NavRoutes()

    @Serializable
    data object More : NavRoutes()

    @Serializable
    data object Guide : NavRoutes()

    @Serializable
    data object StudyRecord : NavRoutes()

    @Serializable
    data object Notification : NavRoutes()

    @Serializable
    data object AiChat : NavRoutes()
}
