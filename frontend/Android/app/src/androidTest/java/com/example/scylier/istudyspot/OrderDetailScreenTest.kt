package com.example.scylier.istudyspot

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.scylier.istudyspot.models.order.OrderDetail
import com.example.scylier.istudyspot.ui.screen.OrderDetailScreen
import org.junit.Rule
import org.junit.Test

class OrderDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockOrder = OrderDetail(
        id = "order1",
        seatId = "seat1",
        userId = "user1",
        studyRoomName = "图书馆自习室",
        seatPosition = "1-1",
        startTime = "2026-10-01T10:00:00",
        endTime = "2026-10-01T12:00:00",
        totalPrice = 20.0,
        status = "paid",
        createdAt = "2026-10-01T09:00:00",
        updatedAt = "2026-10-01T09:00:00"
    )

    @Test
    fun testOrderDetailScreen_displaysTitle() {
        composeTestRule.setContent {
            OrderDetailScreen(
                order = mockOrder,
                isLoading = false,
                onCheckin = {},
                onCheckout = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("订单详情").assertExists()
    }

    @Test
    fun testOrderDetailScreen_loadingState_displaysProgress() {
        composeTestRule.setContent {
            OrderDetailScreen(
                order = null,
                isLoading = true,
                onCheckin = {},
                onCheckout = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Loading").assertExists()
    }

    @Test
    fun testOrderDetailScreen_displaysOrderId() {
        composeTestRule.setContent {
            OrderDetailScreen(
                order = mockOrder,
                isLoading = false,
                onCheckin = {},
                onCheckout = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("order1").assertExists()
    }

    @Test
    fun testOrderDetailScreen_displaysStudyRoomName() {
        composeTestRule.setContent {
            OrderDetailScreen(
                order = mockOrder,
                isLoading = false,
                onCheckin = {},
                onCheckout = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("图书馆自习室").assertExists()
    }

    @Test
    fun testOrderDetailScreen_displaysSeatPosition() {
        composeTestRule.setContent {
            OrderDetailScreen(
                order = mockOrder,
                isLoading = false,
                onCheckin = {},
                onCheckout = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("1-1").assertExists()
    }

    @Test
    fun testOrderDetailScreen_displaysStatus() {
        composeTestRule.setContent {
            OrderDetailScreen(
                order = mockOrder,
                isLoading = false,
                onCheckin = {},
                onCheckout = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("paid").assertExists()
    }

    @Test
    fun testOrderDetailScreen_displaysTotalPrice() {
        composeTestRule.setContent {
            OrderDetailScreen(
                order = mockOrder,
                isLoading = false,
                onCheckin = {},
                onCheckout = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("¥20.0").assertExists()
    }

    @Test
    fun testOrderDetailScreen_paidStatus_displaysCheckinButton() {
        composeTestRule.setContent {
            OrderDetailScreen(
                order = mockOrder,
                isLoading = false,
                onCheckin = {},
                onCheckout = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("签到").assertExists()
    }

    @Test
    fun testOrderDetailScreen_paidStatus_displaysCancelButton() {
        composeTestRule.setContent {
            OrderDetailScreen(
                order = mockOrder,
                isLoading = false,
                onCheckin = {},
                onCheckout = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("取消订单").assertExists()
    }

    @Test
    fun testOrderDetailScreen_inUseStatus_displaysCheckoutButton() {
        val inUseOrder = mockOrder.copy(status = "in_use")

        composeTestRule.setContent {
            OrderDetailScreen(
                order = inUseOrder,
                isLoading = false,
                onCheckin = {},
                onCheckout = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("签退").assertExists()
    }

    @Test
    fun testOrderDetailScreen_clickCheckin_triggersCallback() {
        var checkinClicked = false

        composeTestRule.setContent {
            OrderDetailScreen(
                order = mockOrder,
                isLoading = false,
                onCheckin = { checkinClicked = true },
                onCheckout = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("签到").performClick()

        assert(checkinClicked)
    }

    @Test
    fun testOrderDetailScreen_clickCancel_triggersCallback() {
        var cancelClicked = false

        composeTestRule.setContent {
            OrderDetailScreen(
                order = mockOrder,
                isLoading = false,
                onCheckin = {},
                onCheckout = {},
                onCancel = { cancelClicked = true }
            )
        }

        composeTestRule.onNodeWithText("取消订单").performClick()

        assert(cancelClicked)
    }

    @Test
    fun testOrderDetailScreen_clickCheckout_triggersCallback() {
        var checkoutClicked = false
        val inUseOrder = mockOrder.copy(status = "in_use")

        composeTestRule.setContent {
            OrderDetailScreen(
                order = inUseOrder,
                isLoading = false,
                onCheckin = {},
                onCheckout = { checkoutClicked = true },
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("签退").performClick()

        assert(checkoutClicked)
    }

    @Test
    fun testOrderDetailScreen_nullOrder_noContent() {
        composeTestRule.setContent {
            OrderDetailScreen(
                order = null,
                isLoading = false,
                onCheckin = {},
                onCheckout = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("订单详情").assertExists()
    }
}
