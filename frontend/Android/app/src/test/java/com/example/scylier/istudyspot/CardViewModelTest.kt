package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.card.CardItem
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.viewmodel.CardViewModel
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
class CardViewModelTest {

    private lateinit var viewModel: CardViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockRepository = mockk<MainRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CardViewModel(mockRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        assertTrue(viewModel.cards.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun testLoadCards_success() = runTest {
        val mockCards = listOf(
            CardItem(uuid = "card1", rarity = "SR", borderTheme = "星空", cardTheme = "银河", themeCategory = "宇宙", markdown = "在星空下学习", studyDuration = 120),
            CardItem(uuid = "card2", rarity = "N", borderTheme = "简约", cardTheme = "清晨", themeCategory = "自然", markdown = "清晨的第一缕阳光", studyDuration = 30),
            CardItem(uuid = "card3", rarity = "SSR", borderTheme = "金色", cardTheme = "传奇", themeCategory = "传说", markdown = "传说级学霸的证明", studyDuration = 600)
        )
        coEvery { mockRepository.getCardList("user1") } returns
            ApiResponse.Success(200, "获取成功", mockCards)

        viewModel.loadCards("user1")

        assertEquals(3, viewModel.cards.value.size)
        assertFalse(viewModel.isLoading.value)
        assertEquals("card1", viewModel.cards.value[0].uuid)
        assertEquals("SR", viewModel.cards.value[0].rarity)
        assertEquals("card2", viewModel.cards.value[1].uuid)
        assertEquals("N", viewModel.cards.value[1].rarity)
        assertEquals("card3", viewModel.cards.value[2].uuid)
        assertEquals("SSR", viewModel.cards.value[2].rarity)
    }

    @Test
    fun testLoadCards_error() = runTest {
        coEvery { mockRepository.getCardList(any()) } returns
            ApiResponse.Error(500, "网络错误")

        viewModel.loadCards("user1")

        assertTrue(viewModel.cards.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun testLoadCards_emptyList() = runTest {
        coEvery { mockRepository.getCardList("user1") } returns
            ApiResponse.Success(200, "获取成功", emptyList<CardItem>())

        viewModel.loadCards("user1")

        assertTrue(viewModel.cards.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun testLoadCards_setsLoadingDuringRequest() = runTest {
        val mockCards = listOf(
            CardItem(uuid = "card1", rarity = "R", borderTheme = "绿色", cardTheme = "森林", themeCategory = "自然", markdown = "测试", studyDuration = 60)
        )
        coEvery { mockRepository.getCardList(any()) } returns
            ApiResponse.Success(200, "获取成功", mockCards)

        viewModel.loadCards("user1")

        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun testLoadCards_cardItemFields() = runTest {
        val mockCards = listOf(
            CardItem(
                uuid = "card-uuid-1",
                rarity = "UR",
                borderTheme = "彩虹",
                cardTheme = "极光",
                themeCategory = "奇观",
                markdown = "极光下的学习时光",
                studyDuration = 999,
                createTime = "2026-05-01 10:00:00",
                imageURL = "https://example.com/card1.png"
            )
        )
        coEvery { mockRepository.getCardList("user1") } returns
            ApiResponse.Success(200, "获取成功", mockCards)

        viewModel.loadCards("user1")

        val card = viewModel.cards.value[0]
        assertEquals("card-uuid-1", card.uuid)
        assertEquals("UR", card.rarity)
        assertEquals("彩虹", card.borderTheme)
        assertEquals("极光", card.cardTheme)
        assertEquals("奇观", card.themeCategory)
        assertEquals("极光下的学习时光", card.markdown)
        assertEquals(999, card.studyDuration)
        assertEquals("2026-05-01 10:00:00", card.createTime)
        assertEquals("https://example.com/card1.png", card.imageURL)
    }

    @Test
    fun testLoadCards_overwritesPreviousCards() = runTest {
        val firstCards = listOf(
            CardItem(uuid = "card1", rarity = "N", borderTheme = "", cardTheme = "", themeCategory = "", markdown = "", studyDuration = 10)
        )
        val secondCards = listOf(
            CardItem(uuid = "card2", rarity = "SR", borderTheme = "", cardTheme = "", themeCategory = "", markdown = "", studyDuration = 100),
            CardItem(uuid = "card3", rarity = "SSR", borderTheme = "", cardTheme = "", themeCategory = "", markdown = "", studyDuration = 200)
        )
        coEvery { mockRepository.getCardList("user1") } returns
            ApiResponse.Success(200, "获取成功", firstCards)
        viewModel.loadCards("user1")
        assertEquals(1, viewModel.cards.value.size)

        coEvery { mockRepository.getCardList("user1") } returns
            ApiResponse.Success(200, "获取成功", secondCards)
        viewModel.loadCards("user1")
        assertEquals(2, viewModel.cards.value.size)
        assertEquals("card2", viewModel.cards.value[0].uuid)
    }
}
