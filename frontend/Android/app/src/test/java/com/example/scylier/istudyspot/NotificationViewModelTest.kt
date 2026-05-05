package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.viewmodel.NotificationViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NotificationViewModelTest {

    private lateinit var viewModel: NotificationViewModel

    @Before
    fun setup() {
        viewModel = NotificationViewModel()
    }

    @Test
    fun testViewModel_notifications_notEmpty() {
        assertTrue(viewModel.notifications.isNotEmpty())
    }

    @Test
    fun testViewModel_notifications_size() {
        assertEquals(6, viewModel.notifications.size)
    }

    @Test
    fun testViewModel_unreadCount() {
        assertEquals(2, viewModel.unreadCount)
    }

    @Test
    fun testViewModel_firstNotification_title() {
        val first = viewModel.notifications[0]
        assertEquals("预约成功", first.title)
    }

    @Test
    fun testViewModel_firstNotification_isNotRead() {
        val first = viewModel.notifications[0]
        assertFalse(first.isRead)
    }

    @Test
    fun testViewModel_firstNotification_type() {
        val first = viewModel.notifications[0]
        assertEquals(NotificationViewModel.NotificationType.BOOKING, first.type)
    }

    @Test
    fun testViewModel_secondNotification_isReminder() {
        val second = viewModel.notifications[1]
        assertEquals("签到提醒", second.title)
        assertEquals(NotificationViewModel.NotificationType.REMINDER, second.type)
    }

    @Test
    fun testViewModel_thirdNotification_isSystem() {
        val third = viewModel.notifications[2]
        assertEquals("系统维护通知", third.title)
        assertEquals(NotificationViewModel.NotificationType.SYSTEM, third.type)
    }

    @Test
    fun testViewModel_fourthNotification_isActivity() {
        val fourth = viewModel.notifications[3]
        assertEquals("优惠活动", fourth.title)
        assertEquals(NotificationViewModel.NotificationType.ACTIVITY, fourth.type)
    }

    @Test
    fun testViewModel_notifications_containsBookingType() {
        val bookingNotifications = viewModel.notifications.filter { 
            it.type == NotificationViewModel.NotificationType.BOOKING 
        }
        assertTrue(bookingNotifications.isNotEmpty())
    }

    @Test
    fun testViewModel_notifications_containsSystemType() {
        val systemNotifications = viewModel.notifications.filter { 
            it.type == NotificationViewModel.NotificationType.SYSTEM 
        }
        assertTrue(systemNotifications.isNotEmpty())
    }

    @Test
    fun testViewModel_notifications_containsReminderType() {
        val reminderNotifications = viewModel.notifications.filter { 
            it.type == NotificationViewModel.NotificationType.REMINDER 
        }
        assertTrue(reminderNotifications.isNotEmpty())
    }

    @Test
    fun testViewModel_notifications_containsActivityType() {
        val activityNotifications = viewModel.notifications.filter { 
            it.type == NotificationViewModel.NotificationType.ACTIVITY 
        }
        assertTrue(activityNotifications.isNotEmpty())
    }

    @Test
    fun testNotification_dataClass() {
        val notification = NotificationViewModel.Notification(
            title = "测试标题",
            content = "测试内容",
            time = "刚刚",
            isRead = false,
            type = NotificationViewModel.NotificationType.SYSTEM
        )
        assertEquals("测试标题", notification.title)
        assertEquals("测试内容", notification.content)
        assertEquals("刚刚", notification.time)
        assertFalse(notification.isRead)
    }

    @Test
    fun testNotificationType_values() {
        val types = NotificationViewModel.NotificationType.values()
        assertEquals(4, types.size)
        assertTrue(types.contains(NotificationViewModel.NotificationType.SYSTEM))
        assertTrue(types.contains(NotificationViewModel.NotificationType.BOOKING))
        assertTrue(types.contains(NotificationViewModel.NotificationType.REMINDER))
        assertTrue(types.contains(NotificationViewModel.NotificationType.ACTIVITY))
    }

    @Test
    fun testViewModel_notifications_contentNotEmpty() {
        viewModel.notifications.forEach { notification ->
            assertTrue(notification.title.isNotEmpty())
            assertTrue(notification.content.isNotEmpty())
            assertTrue(notification.time.isNotEmpty())
        }
    }

    @Test
    fun testViewModel_readNotificationsCount() {
        val readCount = viewModel.notifications.count { it.isRead }
        assertEquals(4, readCount)
    }
}
