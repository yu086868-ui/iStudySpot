package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.viewmodel.MoreViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MoreViewModelTest {

    private lateinit var viewModel: MoreViewModel

    @Before
    fun setup() {
        viewModel = MoreViewModel()
    }

    @Test
    fun testViewModel_moreItems_notEmpty() {
        assertTrue(viewModel.moreItems.isNotEmpty())
    }

    @Test
    fun testViewModel_moreItems_size() {
        assertTrue(viewModel.moreItems.size >= 10)
    }

    @Test
    fun testViewModel_groupedItems_notEmpty() {
        assertTrue(viewModel.groupedItems.isNotEmpty())
    }

    @Test
    fun testViewModel_groupedItems_containsCategories() {
        val categories = viewModel.groupedItems.keys
        assertTrue(categories.contains("记录查询"))
        assertTrue(categories.contains("积分福利"))
        assertTrue(categories.contains("帮助与支持"))
        assertTrue(categories.contains("其他功能"))
    }

    @Test
    fun testViewModel_groupedItems_recordQueryCategory() {
        val recordItems = viewModel.groupedItems["记录查询"]
        assertNotNull(recordItems)
        assertTrue(recordItems!!.size >= 4)
    }

    @Test
    fun testViewModel_groupedItems_pointsCategory() {
        val pointsItems = viewModel.groupedItems["积分福利"]
        assertNotNull(pointsItems)
        assertTrue(pointsItems!!.size >= 2)
    }

    @Test
    fun testViewModel_groupedItems_helpCategory() {
        val helpItems = viewModel.groupedItems["帮助与支持"]
        assertNotNull(helpItems)
        assertTrue(helpItems!!.size >= 3)
    }

    @Test
    fun testViewModel_groupedItems_otherCategory() {
        val otherItems = viewModel.groupedItems["其他功能"]
        assertNotNull(otherItems)
        assertTrue(otherItems!!.size >= 3)
    }

    @Test
    fun testViewModel_firstItem_title() {
        val firstItem = viewModel.moreItems[0]
        assertEquals("预约记录", firstItem.title)
    }

    @Test
    fun testViewModel_firstItem_description() {
        val firstItem = viewModel.moreItems[0]
        assertEquals("查看历史预约信息", firstItem.description)
    }

    @Test
    fun testViewModel_firstItem_category() {
        val firstItem = viewModel.moreItems[0]
        assertEquals("记录查询", firstItem.category)
    }
}
