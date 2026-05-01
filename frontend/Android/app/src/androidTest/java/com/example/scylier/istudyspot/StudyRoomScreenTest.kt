package com.example.scylier.istudyspot

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.scylier.istudyspot.models.studyroom.StudyRoomItem
import com.example.scylier.istudyspot.ui.screen.StudyRoomScreen
import org.junit.Rule
import org.junit.Test

class StudyRoomScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockStudyRooms = listOf(
        StudyRoomItem(
            id = "1",
            name = "图书馆自习室",
            address = "图书馆三楼",
            openingHours = "08:00-22:00",
            occupancyRate = 0.8,
            imageUrl = "https://example.com/room1.jpg"
        ),
        StudyRoomItem(
            id = "2",
            name = "教学楼自习室",
            address = "教学楼五楼",
            openingHours = "07:00-23:00",
            occupancyRate = 0.6,
            imageUrl = "https://example.com/room2.jpg"
        )
    )

    @Test
    fun testStudyRoomScreen_displaysTitle() {
        composeTestRule.setContent {
            StudyRoomScreen(
                studyRooms = emptyList(),
                isLoading = false,
                onStudyRoomClick = {}
            )
        }

        composeTestRule.onNodeWithText("自习室列表").assertExists()
    }

    @Test
    fun testStudyRoomScreen_loadingState_displaysProgress() {
        composeTestRule.setContent {
            StudyRoomScreen(
                studyRooms = emptyList(),
                isLoading = true,
                onStudyRoomClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Loading").assertExists()
    }

    @Test
    fun testStudyRoomScreen_displaysStudyRooms() {
        composeTestRule.setContent {
            StudyRoomScreen(
                studyRooms = mockStudyRooms,
                isLoading = false,
                onStudyRoomClick = {}
            )
        }

        composeTestRule.onNodeWithText("图书馆自习室").assertExists()
        composeTestRule.onNodeWithText("教学楼自习室").assertExists()
    }

    @Test
    fun testStudyRoomScreen_displaysAddress() {
        composeTestRule.setContent {
            StudyRoomScreen(
                studyRooms = mockStudyRooms,
                isLoading = false,
                onStudyRoomClick = {}
            )
        }

        composeTestRule.onNodeWithText("图书馆三楼").assertExists()
        composeTestRule.onNodeWithText("教学楼五楼").assertExists()
    }

    @Test
    fun testStudyRoomScreen_displaysOpeningHours() {
        composeTestRule.setContent {
            StudyRoomScreen(
                studyRooms = mockStudyRooms,
                isLoading = false,
                onStudyRoomClick = {}
            )
        }

        composeTestRule.onNodeWithText("营业时间: 08:00-22:00").assertExists()
        composeTestRule.onNodeWithText("营业时间: 07:00-23:00").assertExists()
    }

    @Test
    fun testStudyRoomScreen_displaysOccupancyRate() {
        composeTestRule.setContent {
            StudyRoomScreen(
                studyRooms = mockStudyRooms,
                isLoading = false,
                onStudyRoomClick = {}
            )
        }

        composeTestRule.onNodeWithText("上座率: 80%").assertExists()
        composeTestRule.onNodeWithText("上座率: 60%").assertExists()
    }

    @Test
    fun testStudyRoomScreen_clickStudyRoom_triggersCallback() {
        var clickedRoom: StudyRoomItem? = null

        composeTestRule.setContent {
            StudyRoomScreen(
                studyRooms = mockStudyRooms,
                isLoading = false,
                onStudyRoomClick = { room -> clickedRoom = room }
            )
        }

        composeTestRule.onNodeWithText("图书馆自习室").performClick()

        assert(clickedRoom != null)
        assertEquals("1", clickedRoom?.id)
        assertEquals("图书馆自习室", clickedRoom?.name)
    }

    @Test
    fun testStudyRoomScreen_clickSecondStudyRoom_triggersCallback() {
        var clickedRoom: StudyRoomItem? = null

        composeTestRule.setContent {
            StudyRoomScreen(
                studyRooms = mockStudyRooms,
                isLoading = false,
                onStudyRoomClick = { room -> clickedRoom = room }
            )
        }

        composeTestRule.onNodeWithText("教学楼自习室").performClick()

        assert(clickedRoom != null)
        assertEquals("2", clickedRoom?.id)
    }

    @Test
    fun testStudyRoomScreen_emptyList_noItemsDisplayed() {
        composeTestRule.setContent {
            StudyRoomScreen(
                studyRooms = emptyList(),
                isLoading = false,
                onStudyRoomClick = {}
            )
        }

        composeTestRule.onNodeWithText("自习室列表").assertExists()
    }
}
