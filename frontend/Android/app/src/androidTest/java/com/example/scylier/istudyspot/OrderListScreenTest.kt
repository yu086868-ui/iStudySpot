package com.example.scylier.istudyspot

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.scylier.istudyspot.models.order.OrderItem
import com.example.scylier.istudyspot.ui.screen.OrderListScreen
import org.junit.Rule
import org.junit.Test

class OrderListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockOrders = listOf(
        OrderItem(
            id = "order1",
            seatId = "seat1",
            studyRoomName = "图书馆自习室",
            seatPosition = "1-1",
            startTime = "2026-10-01T10:00:00",
            endTime = "2026-10-01T12:00:00",
            totalPrice = 20.0,
            status = "pending",
            createdAt = "2026-10-01T09:00:00"
        ),
        OrderItem(
            id = "order2",
            seatId = "seat2",
            studyRoomName = "教学楼自习室",
            seatPosition = "2-3",
            startTime = "2026-10-02T14:00:00",
            endTime = "2026-10-02T16:00:00",
            totalPrice = 20.0,
            status = "paid",
            createdAt = "2026-10-02T13:00:00"
        )
    )

    @Test
    fun testOrderListScreen_displaysTitle() {
        composeTestRule.setContent {
            OrderListScreen(
                orders = emptyList(),
                isLoading = false,
                onOrderClick = {}
            )
        }

        composeTestRule.onNodeWithText("我的订单").assertExists()
    }

    @Test
    fun testOrderListScreen_loadingState_displaysProgress() {
        composeTestRule.setContent {
            OrderListScreen(
                orders = emptyList(),
                isLoading = true,
                onOrderClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Loading").assertExists()
    }

    @Test
    fun testOrderListScreen_emptyState_displaysEmptyMessage() {
        composeTestRule.setContent {
            OrderListScreen(
                orders = emptyList(),
                isLoading = false,
                onOrderClick = {}
            )
        }

        composeTestRule.onNodeWithText("暂无订单").assertExists()
    }

    @Test
    fun testOrderListScreen_displaysOrders() {
        composeTestRule.setContent {
            OrderListScreen(
                orders = mockOrders,
                isLoading = false,
                onOrderClick = {}
            )
        }

        composeTestRule.onNodeWithText("图书馆自习室").assertExists()
        composeTestRule.onNodeWithText("教学楼自习室").assertExists()
    }

    @Test
    fun testOrderListScreen_displaysSeatPosition() {
        composeTestRule.setContent {
            OrderListScreen(
                orders = mockOrders,
                isLoading = false,
                onOrderClick = {}
            )
        }

        composeTestRule.onNodeWithText("座位: 1-1").assertExists()
        composeTestRule.onNodeWithText("座位: 2-3").assertExists()
    }

    @Test
    fun testOrderListScreen_displaysStatus() {
        composeTestRule.setContent {
            OrderListScreen(
                orders = mockOrders,
                isLoading = false,
                onOrderClick = {}
            )
        }

        composeTestRule.onNodeWithText("pending").assertExists()
        composeTestRule.onNodeWithText("paid").assertExists()
    }

    @Test
    fun testOrderListScreen_clickOrder_triggersCallback() {
        var clickedOrder: OrderItem? = null

        composeTestRule.setContent {
            OrderListScreen(
                orders = mockOrders,
                isLoading = false,
                onOrderClick = { order -> clickedOrder = order }
            )
        }

        composeTestRule.onNodeWithText("图书馆自习室").performClick()

        assert(clickedOrder != null)
        assertEquals("order1", clickedOrder?.id)
    }

    @Test
    fun testOrderListScreen_clickSecondOrder_triggersCallback() {
        var clickedOrder: OrderItem? = null

        composeTestRule.setContent {
            OrderListScreen(
                orders = mockOrders,
                isLoading = false,
                onOrderClick = { order -> clickedOrder = order }
            )
        }

        composeTestRule.onNodeWithText("教学楼自习室").performClick()

        assert(clickedOrder != null)
        assertEquals("order2", clickedOrder?.id)
    }
}
