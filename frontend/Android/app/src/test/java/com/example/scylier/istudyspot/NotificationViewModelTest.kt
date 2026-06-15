package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.viewmodel.NotificationViewModel
import com.example.scylier.istudyspot.viewmodel.NotificationItem
import com.example.scylier.istudyspot.viewmodel.NotificationType
import com.example.scylier.istudyspot.viewmodel.NotificationUiState
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

    private lateinit var viewModel: NotificationViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockRepository = mockk<MainRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockRepository.getAnnouncements(any(), any(), any(), any()) } returns ApiResponse.Error(500, "Test error")
        viewModel = NotificationViewModel(mockRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testViewModel_initialState_isLoading() {
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun testViewModel_initialState_emptyNotifications() {
        assertTrue(viewModel.state.value.notifications.isEmpty())
    }

    @Test
    fun testViewModel_loadNotifications_fallsBackToMock() = runTest {
        viewModel.loadNotifications()
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.notifications.isNotEmpty())
    }

    @Test
    fun testViewModel_mockNotifications_size() = runTest {
        viewModel.loadNotifications()
        assertEquals(6, viewModel.state.value.notifications.size)
    }

    @Test
    fun testViewModel_unreadCount() = runTest {
        viewModel.loadNotifications()
        assertEquals(2, viewModel.unreadCount)
    }

    @Test
    fun testViewModel_firstNotification_title() = runTest {
        viewModel.loadNotifications()
        val first = viewModel.state.value.notifications[0]
        assertEquals("预约成功", first.title)
    }

    @Test
    fun testViewModel_firstNotification_isNotRead() = runTest {
        viewModel.loadNotifications()
        val first = viewModel.state.value.notifications[0]
        assertFalse(first.isRead)
    }

    @Test
    fun testViewModel_firstNotification_type() = runTest {
        viewModel.loadNotifications()
        val first = viewModel.state.value.notifications[0]
        assertEquals(NotificationType.BOOKING, first.type)
    }

    @Test
    fun testViewModel_secondNotification_isReminder() = runTest {
        viewModel.loadNotifications()
        val second = viewModel.state.value.notifications[1]
        assertEquals("签到提醒", second.title)
        assertEquals(NotificationType.REMINDER, second.type)
    }

    @Test
    fun testViewModel_thirdNotification_isSystem() = runTest {
        viewModel.loadNotifications()
        val third = viewModel.state.value.notifications[2]
        assertEquals("系统维护通知", third.title)
        assertEquals(NotificationType.SYSTEM, third.type)
    }

    @Test
    fun testViewModel_fourthNotification_isActivity() = runTest {
        viewModel.loadNotifications()
        val fourth = viewModel.state.value.notifications[3]
        assertEquals("优惠活动", fourth.title)
        assertEquals(NotificationType.ACTIVITY, fourth.type)
    }

    @Test
    fun testViewModel_notifications_containsBookingType() = runTest {
        viewModel.loadNotifications()
        val bookingNotifications = viewModel.state.value.notifications.filter {
            it.type == NotificationType.BOOKING
        }
        assertTrue(bookingNotifications.isNotEmpty())
    }

    @Test
    fun testViewModel_notifications_containsSystemType() = runTest {
        viewModel.loadNotifications()
        val systemNotifications = viewModel.state.value.notifications.filter {
            it.type == NotificationType.SYSTEM
        }
        assertTrue(systemNotifications.isNotEmpty())
    }

    @Test
    fun testViewModel_notifications_containsReminderType() = runTest {
        viewModel.loadNotifications()
        val reminderNotifications = viewModel.state.value.notifications.filter {
            it.type == NotificationType.REMINDER
        }
        assertTrue(reminderNotifications.isNotEmpty())
    }

    @Test
    fun testViewModel_notifications_containsActivityType() = runTest {
        viewModel.loadNotifications()
        val activityNotifications = viewModel.state.value.notifications.filter {
            it.type == NotificationType.ACTIVITY
        }
        assertTrue(activityNotifications.isNotEmpty())
    }

    @Test
    fun testNotificationItem_dataClass() {
        val notification = NotificationItem(
            id = "1",
            title = "测试标题",
            content = "测试内容",
            time = "刚刚",
            isRead = false,
            type = NotificationType.SYSTEM
        )
        assertEquals("1", notification.id)
        assertEquals("测试标题", notification.title)
        assertEquals("测试内容", notification.content)
        assertEquals("刚刚", notification.time)
        assertFalse(notification.isRead)
    }

    @Test
    fun testNotificationType_values() {
        val types = NotificationType.values()
        assertEquals(4, types.size)
        assertTrue(types.contains(NotificationType.SYSTEM))
        assertTrue(types.contains(NotificationType.BOOKING))
        assertTrue(types.contains(NotificationType.REMINDER))
        assertTrue(types.contains(NotificationType.ACTIVITY))
    }

    @Test
    fun testViewModel_notifications_contentNotEmpty() = runTest {
        viewModel.loadNotifications()
        viewModel.state.value.notifications.forEach { notification ->
            assertTrue(notification.title.isNotEmpty())
            assertTrue(notification.content.isNotEmpty())
            assertTrue(notification.time.isNotEmpty())
        }
    }

    @Test
    fun testViewModel_readNotificationsCount() = runTest {
        viewModel.loadNotifications()
        val readCount = viewModel.state.value.notifications.count { it.isRead }
        assertEquals(4, readCount)
    }
}
