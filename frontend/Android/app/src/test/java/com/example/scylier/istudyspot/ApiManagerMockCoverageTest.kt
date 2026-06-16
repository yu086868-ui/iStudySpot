package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.infra.network.ApiManager
import com.example.scylier.istudyspot.models.ApiResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiManagerMockCoverageTest {

    private val apiManager = ApiManager(useMockData = true)

    @Test
    fun authMockApisShouldReturnSuccessPayloads() = runTest {
        val login = apiManager.login("alice", "password").successData()
        assertEquals("alice", login.user.username)
        assertEquals("mock_token", login.token)

        val register = apiManager.register("bob", "password", "Bob").successData()
        assertEquals("bob", register.user?.username)
        assertEquals("Bob", register.user?.nickname)

        val refresh = apiManager.refreshToken("refresh-token").successData()
        assertEquals("mock_new_token", refresh.token)

        val logout = apiManager.logout()
        assertTrue(logout is ApiResponse.Success)
    }

    @Test
    fun studyRoomMockApisShouldReturnRoomsGuidesAndSeats() = runTest {
        val rooms = apiManager.getStudyRooms(keyword = "library").successData()
        assertEquals(2, rooms.total)
        assertEquals("Study Room A", rooms.list.first().name)

        val detail = apiManager.getStudyRoomDetail(3L).successData()
        assertEquals(3L, detail.id)
        assertTrue(detail.openingHours.contains("08:00"))

        val guides = apiManager.getStudyRoomGuides().successData()
        assertEquals(2, guides.size)

        val guideDetail = apiManager.getStudyRoomGuideDetail(1L).successData()
        assertEquals(1L, guideDetail.studyRoomId)
        assertTrue(guideDetail.contactInfo.isNotBlank())

        val seats = apiManager.getStudyRoomSeats(1L).successData()
        assertEquals(40, seats.size)
        assertTrue(seats.any { it.status == "booked" })

        val layout = apiManager.getStudyRoomSeatLayout(1L).successData()
        assertEquals("hybrid", layout.layoutMode)
        assertEquals(3, layout.seats.size)
        assertTrue(layout.items.any { it.isSeat })

        val seatDetail = apiManager.getSeatDetail(101L).successData()
        assertEquals(101L, seatDetail.id)
        assertEquals("normal", seatDetail.type)
    }

    @Test
    fun orderAndCheckinMockApisShouldReturnExpectedStates() = runTest {
        val order = apiManager.createOrder(
            studyRoomId = 1L,
            seatId = 2L,
            startTime = "2026-06-17 10:00:00",
            endTime = "2026-06-17 12:00:00",
            bookingType = "normal"
        ).successData()
        assertEquals(2L, order.seatId)
        assertEquals("pending", order.status)

        val orders = apiManager.getUserOrders(status = "paid").successData()
        assertEquals(2, orders.total)
        assertTrue(orders.list.isNotEmpty())

        val detail = apiManager.getOrderDetail(1L).successData()
        assertEquals(1L, detail.id)

        val cancel = apiManager.cancelOrder(1L).successData()
        assertEquals("cancelled", cancel.status)

        val paid = apiManager.payOrder(1L).successData()
        assertEquals("paid", paid["status"])

        val renewed = apiManager.renewOrder(1L, "2026-06-17 13:00:00").successData()
        assertEquals(1L, renewed["orderId"])

        val rules = apiManager.getReservationRules().successData()
        assertEquals(7, rules["maxAdvanceDays"])

        val checkin = apiManager.checkin(1L, 2L).successData()
        assertEquals("in_use", checkin.status)

        val checkout = apiManager.checkout(1L).successData()
        assertEquals("completed", checkout.status)

        val records = apiManager.getCheckinRecords().successData()
        assertEquals(0, records["total"])

        val current = apiManager.getCurrentCheckin().successData()
        assertEquals(false, current["isCheckedIn"])
    }

    @Test
    fun userPaymentStatisticsAnnouncementAndRulesMocksShouldReturnData() = runTest {
        val user = apiManager.getUserInfo().successData()
        assertEquals(1L, user.id)

        val update = apiManager.updateUserInfo(nickname = "New name").successData()
        assertEquals("New name", update.nickname)

        val password = apiManager.changePassword("old", "new")
        assertTrue(password is ApiResponse.Success)

        val payment = apiManager.createPayment(1L, 20.0, "mock").successData()
        assertEquals(1L, payment.orderId)

        val paymentStatus = apiManager.getPaymentStatus(1L).successData()
        assertEquals("success", paymentStatus.status)

        val statistics = apiManager.getStudyRoomStatistics(1L, "2026-06-01", "2026-06-17").successData()
        assertEquals(1L, statistics.studyRoomId)

        val announcements = apiManager.getAnnouncements().successData()
        @Suppress("UNCHECKED_CAST")
        assertTrue((announcements["list"] as List<Map<String, Any?>>).isEmpty())

        val announcementDetail = apiManager.getAnnouncementDetail(1L).successData()
        assertEquals(1L, announcementDetail["id"])

        val rules = apiManager.getRules().successData()
        assertTrue(rules.isEmpty())

        val ruleDetail = apiManager.getRuleDetail(1L).successData()
        assertEquals(1L, ruleDetail["id"])
    }

    @Test
    fun aiAgentCustomerCardAchievementViolationAndTodoMocksShouldReturnData() = runTest {
        val characters = apiManager.getAiCharacters().successData()
        assertTrue(characters.isNotEmpty())

        val aiReply = apiManager.sendAiMessage("hello").successData()
        assertNotNull(aiReply.reply)

        val catalog = apiManager.getAgentToolCatalog().successData()
        assertTrue(catalog.any { it.name == "list_study_rooms" })

        val agentRules = apiManager.sendAgentMessage("查看预约规则").successData()
        assertEquals("get_reservation_rules", agentRules.toolResult?.tool)

        val toolRooms = apiManager.executeAgentTool("list_study_rooms").successData()
        assertEquals("list_study_rooms", toolRooms.tool)

        val toolSeats = apiManager.executeAgentTool("list_room_seats", mapOf("studyRoomId" to 2L)).successData()
        assertEquals(2L, toolSeats.data["studyRoomId"])

        val reservations = apiManager.executeAgentTool("get_my_reservations").successData()
        assertEquals("get_my_reservations", reservations.tool)

        val cards = apiManager.getCardList("1").successData()
        assertEquals(3, cards.size)

        val welcome = apiManager.getCustomerServiceWelcome().successData()
        assertNotNull(welcome["welcomeMessage"])

        val chat = apiManager.customerServiceChat("session", "help").successData()
        assertNotNull(chat["response"])

        val history = apiManager.getCustomerServiceHistory("session").successData()
        assertNotNull(history["messages"])

        val achievements = apiManager.getAchievements().successData()
        assertEquals(8, achievements.size)

        val violations = apiManager.getViolations().successData()
        assertEquals(0, violations["total"])

        val appeal = apiManager.appealViolation(1L, "reason")
        assertTrue(appeal is ApiResponse.Success)

        val todos = apiManager.getTodos().successData()
        assertEquals(3, todos.size)

        val created = apiManager.createTodo("Read", priority = 1, dueTime = "2026-06-18 10:00:00").successData()
        assertEquals("Read", created.title)

        val updated = apiManager.updateTodo(3L, "Write", priority = 2).successData()
        assertEquals(3L, updated.id)

        val toggled = apiManager.toggleTodo(3L).successData()
        assertEquals("completed", toggled.status)

        val deleted = apiManager.deleteTodo(3L)
        assertTrue(deleted is ApiResponse.Success)
    }

    private fun <T> ApiResponse<T>.successData(): T {
        return when (this) {
            is ApiResponse.Success -> requireNotNull(data)
            is ApiResponse.Error -> error("Expected success but got $code: $message")
        }
    }
}
