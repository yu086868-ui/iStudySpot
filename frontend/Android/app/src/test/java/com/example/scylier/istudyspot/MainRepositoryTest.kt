package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.infra.network.ApiManager
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.agent.AgentChatResponse
import com.example.scylier.istudyspot.models.agent.AgentToolDefinition
import com.example.scylier.istudyspot.models.agent.AgentToolExecutionResult
import com.example.scylier.istudyspot.models.agent.AgentUiAction
import com.example.scylier.istudyspot.repository.MainRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MainRepositoryTest {

    private val apiManager = mockk<ApiManager>()
    private val repository = MainRepository(apiManager)

    @Test
    fun authAndStudyRoomApisShouldDelegateToManager() = runTest {
        val loginResponse = ApiResponse.Success(200, "ok", null)
        val roomsResponse = ApiResponse.Success(200, "ok", null)
        val detailResponse = ApiResponse.Success(200, "ok", null)
        val guidesResponse = ApiResponse.Success(200, "ok", null)
        val guideDetailResponse = ApiResponse.Success(200, "ok", null)
        val seatsResponse = ApiResponse.Success(200, "ok", null)
        val layoutResponse = ApiResponse.Success(200, "ok", null)
        val seatDetailResponse = ApiResponse.Success(200, "ok", null)
        coEvery { apiManager.login("u", "p") } returns loginResponse
        coEvery { apiManager.refreshToken("r") } returns ApiResponse.Success(200, "ok", null)
        coEvery { apiManager.logout() } returns ApiResponse.Success(200, "ok", Unit)
        coEvery { apiManager.getStudyRooms(2, 10, "open", "k") } returns roomsResponse
        coEvery { apiManager.getStudyRoomDetail(1L) } returns detailResponse
        coEvery { apiManager.getStudyRoomGuides() } returns guidesResponse
        coEvery { apiManager.getStudyRoomGuideDetail(1L) } returns guideDetailResponse
        coEvery { apiManager.getStudyRoomSeats(1L, "available", "normal") } returns seatsResponse
        coEvery { apiManager.getStudyRoomSeatLayout(1L) } returns layoutResponse
        coEvery { apiManager.getSeatDetail(11L) } returns seatDetailResponse

        assertEquals(loginResponse, repository.login("u", "p"))
        assertEquals(roomsResponse, repository.getStudyRooms(2, 10, "open", "k"))
        assertEquals(detailResponse, repository.getStudyRoomDetail(1L))
        assertEquals(guidesResponse, repository.getStudyRoomGuides())
        assertEquals(guideDetailResponse, repository.getStudyRoomGuideDetail(1L))
        assertEquals(seatsResponse, repository.getStudyRoomSeats(1L, "available", "normal"))
        assertEquals(layoutResponse, repository.getStudyRoomSeatLayout(1L))
        assertEquals(seatDetailResponse, repository.getSeatDetail(11L))
        assertEquals("ok", (repository.refreshToken("r") as ApiResponse.Success).message)
        assertTrue(repository.logout() is ApiResponse.Success)
    }

    @Test
    fun reservationUserAndSupportApisShouldDelegateToManager() = runTest {
        val orderResponse = ApiResponse.Success(201, "ok", null)
        val listResponse = ApiResponse.Success(200, "ok", null)
        val detailResponse = ApiResponse.Success(200, "ok", null)
        val paymentResponse = ApiResponse.Success(201, "ok", null)
        val agentCatalog = ApiResponse.Success(200, "ok", listOf(AgentToolDefinition("tool", "title", "desc")))
        val agentChat = ApiResponse.Success(200, "ok", AgentChatResponse(sessionId = "sid", reply = "reply"))
        val agentExec = ApiResponse.Success(
            200,
            "ok",
            AgentToolExecutionResult(
                tool = "tool",
                summary = "summary",
                referenceScope = "response",
                data = emptyMap(),
                uiAction = AgentUiAction("navigate", "route")
            )
        )
        coEvery { apiManager.createOrder(any(), any(), any(), any(), any()) } returns orderResponse
        coEvery { apiManager.getUserOrders("paid", "2026-06-01", "2026-06-30", 3, 5) } returns listResponse
        coEvery { apiManager.getOrderDetail(1L) } returns detailResponse
        coEvery { apiManager.cancelOrder(1L) } returns ApiResponse.Success(200, "ok", null)
        coEvery { apiManager.payOrder(1L) } returns ApiResponse.Success(200, "ok", mapOf("status" to "paid"))
        coEvery { apiManager.renewOrder(1L, "end") } returns ApiResponse.Success(200, "ok", mapOf("orderId" to 1L))
        coEvery { apiManager.getReservationRules() } returns ApiResponse.Success(200, "ok", mapOf("maxAdvanceDays" to 7))
        coEvery { apiManager.checkin(1L, 2L) } returns ApiResponse.Success(200, "ok", null)
        coEvery { apiManager.checkout(1L) } returns ApiResponse.Success(200, "ok", null)
        coEvery { apiManager.getCheckinRecords("2026-06-01", "2026-06-02", 1, 20) } returns ApiResponse.Success(200, "ok", emptyMap())
        coEvery { apiManager.getCurrentCheckin() } returns ApiResponse.Success(200, "ok", emptyMap())
        coEvery { apiManager.getUserInfo() } returns ApiResponse.Success(200, "ok", null)
        coEvery { apiManager.updateUserInfo("nick", "avatar", "phone", "mail") } returns ApiResponse.Success(200, "ok", null)
        coEvery { apiManager.changePassword("old", "new") } returns ApiResponse.Success(200, "ok", Unit)
        coEvery { apiManager.createPayment(1L, 12.0, "wechat") } returns paymentResponse
        coEvery { apiManager.getPaymentStatus(1L) } returns ApiResponse.Success(200, "ok", null)
        coEvery { apiManager.getStudyRoomStatistics(1L, "s", "e") } returns ApiResponse.Success(200, "ok", null)
        coEvery { apiManager.getAnnouncements("system", "high", 1, 20) } returns ApiResponse.Success(200, "ok", emptyMap())
        coEvery { apiManager.getAnnouncementDetail(1L) } returns ApiResponse.Success(200, "ok", emptyMap())
        coEvery { apiManager.getRules("1", "booking") } returns ApiResponse.Success(200, "ok", emptyList())
        coEvery { apiManager.getRuleDetail(1L) } returns ApiResponse.Success(200, "ok", emptyMap())
        coEvery { apiManager.getAiCharacters() } returns ApiResponse.Success(200, "ok", emptyList())
        coEvery { apiManager.sendAiMessage("hello", "sid", "cid") } returns ApiResponse.Success(200, "ok", null)
        coEvery { apiManager.getAgentToolCatalog() } returns agentCatalog
        coEvery { apiManager.sendAgentMessage("ask", "sid") } returns agentChat
        coEvery { apiManager.executeAgentTool("tool", mapOf("id" to 1L)) } returns agentExec
        coEvery { apiManager.getCustomerServiceWelcome() } returns ApiResponse.Success(200, "ok", emptyMap())
        coEvery { apiManager.customerServiceChat("s", "m") } returns ApiResponse.Success(200, "ok", emptyMap())
        coEvery { apiManager.getCustomerServiceHistory("s") } returns ApiResponse.Success(200, "ok", emptyMap())
        coEvery { apiManager.getAchievements() } returns ApiResponse.Success(200, "ok", emptyList())
        coEvery { apiManager.getViolations() } returns ApiResponse.Success(200, "ok", emptyMap())
        coEvery { apiManager.appealViolation(1L, "why") } returns ApiResponse.Success(200, "ok", Unit)

        assertEquals(orderResponse, repository.createOrder(1L, 2L, "s", "e", "normal"))
        assertEquals(listResponse, repository.getUserOrders("paid", "2026-06-01", "2026-06-30", 3, 5))
        assertEquals(detailResponse, repository.getOrderDetail(1L))
        assertTrue(repository.cancelOrder(1L) is ApiResponse.Success)
        assertTrue(repository.payOrder(1L) is ApiResponse.Success)
        assertTrue(repository.renewOrder(1L, "end") is ApiResponse.Success)
        assertTrue(repository.getReservationRules() is ApiResponse.Success)
        assertTrue(repository.checkin(1L, 2L) is ApiResponse.Success)
        assertTrue(repository.checkout(1L) is ApiResponse.Success)
        assertTrue(repository.getCheckinRecords("2026-06-01", "2026-06-02", 1, 20) is ApiResponse.Success)
        assertTrue(repository.getCurrentCheckin() is ApiResponse.Success)
        assertTrue(repository.getUserInfo() is ApiResponse.Success)
        assertTrue(repository.updateUserInfo("nick", "avatar", "phone", "mail") is ApiResponse.Success)
        assertTrue(repository.changePassword("old", "new") is ApiResponse.Success)
        assertEquals(paymentResponse, repository.createPayment(1L, 12.0, "wechat"))
        assertTrue(repository.getPaymentStatus(1L) is ApiResponse.Success)
        assertTrue(repository.getStudyRoomStatistics(1L, "s", "e") is ApiResponse.Success)
        assertTrue(repository.getAnnouncements("system", "high", 1, 20) is ApiResponse.Success)
        assertTrue(repository.getAnnouncementDetail(1L) is ApiResponse.Success)
        assertTrue(repository.getRules("1", "booking") is ApiResponse.Success)
        assertTrue(repository.getRuleDetail(1L) is ApiResponse.Success)
        assertTrue(repository.getAiCharacters() is ApiResponse.Success)
        assertTrue(repository.sendAiMessage("hello", "sid", "cid") is ApiResponse.Success)
        assertEquals(agentCatalog, repository.getAgentToolCatalog())
        assertEquals(agentChat, repository.sendAgentMessage("ask", "sid"))
        assertEquals(agentExec, repository.executeAgentTool("tool", mapOf("id" to 1L)))
        assertTrue(repository.getCustomerServiceWelcome() is ApiResponse.Success)
        assertTrue(repository.customerServiceChat("s", "m") is ApiResponse.Success)
        assertTrue(repository.getCustomerServiceHistory("s") is ApiResponse.Success)
        assertTrue(repository.getAchievements() is ApiResponse.Success)
        assertTrue(repository.getViolations() is ApiResponse.Success)
        assertTrue(repository.appealViolation(1L, "why") is ApiResponse.Success)
    }

    @Test
    fun renewOrderShouldWrapThrownException() = runTest {
        coEvery { apiManager.renewOrder(1L, "end") } throws IllegalStateException("boom")

        val result = repository.renewOrder(1L, "end")

        assertTrue(result is ApiResponse.Error)
        assertTrue((result as ApiResponse.Error).message.contains("boom"))
    }

    @Test
    fun getCardListShouldMapCardsAndHandleFailures() = runTest {
        coEvery { apiManager.getCardList("u1") } returns ApiResponse.Success(
            200,
            "ok",
            listOf(
                mapOf(
                    "uuid" to "card-1",
                    "rarity" to "SSR",
                    "borderTheme" to "Gold",
                    "cardTheme" to "Sky",
                    "themeCategory" to "Nature",
                    "markdown" to "Hello",
                    "studyDuration" to 123,
                    "createTime" to "2026-06-01",
                    "imageURL" to "https://x"
                ),
                mapOf("uuid" to "card-2")
            )
        )

        val success = repository.getCardList("u1") as ApiResponse.Success
        assertEquals(2, success.data?.size)
        assertEquals("card-1", success.data?.first()?.uuid)
        assertEquals(123, success.data?.first()?.studyDuration)
        assertEquals("N", success.data?.get(1)?.rarity)

        coEvery { apiManager.getCardList("u2") } returns ApiResponse.Error(500, "bad")
        assertTrue(repository.getCardList("u2") is ApiResponse.Error)

        coEvery { apiManager.getCardList("u3") } throws RuntimeException("explode")
        val thrown = repository.getCardList("u3") as ApiResponse.Error
        assertTrue(thrown.message.contains("explode"))
    }
}
