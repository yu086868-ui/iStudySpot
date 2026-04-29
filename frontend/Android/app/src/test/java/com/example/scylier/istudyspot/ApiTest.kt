package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.infra.network.ApiManager
import com.example.scylier.istudyspot.models.ApiResponse
import kotlinx.coroutines.runBlocking
import org.junit.Test


class ApiTest {
    private val apiManager = ApiManager()

    @Test
    fun testLogin() = runBlocking {
        val response = apiManager.login("test", "password")
        when (response) {
            is ApiResponse.Success -> println("Login success: ${response.data.token}")
            is ApiResponse.Error -> println("Login error: ${response.message}")
        }
    }

    @Test
    fun testGetStudyRooms() = runBlocking {
        val response = apiManager.getStudyRooms()
        when (response) {
            is ApiResponse.Success -> println("Get study rooms success: ${response.data.total} rooms")
            is ApiResponse.Error -> println("Get study rooms error: ${response.message}")
        }
    }

    @Test
    fun testGetStudyRoomDetail() = runBlocking {
        val response = apiManager.getStudyRoomDetail("1")
        when (response) {
            is ApiResponse.Success -> println("Get study room detail success: ${response.data.name}")
            is ApiResponse.Error -> println("Get study room detail error: ${response.message}")
        }
    }
}
