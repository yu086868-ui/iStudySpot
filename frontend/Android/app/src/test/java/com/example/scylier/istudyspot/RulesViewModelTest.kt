package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.viewmodel.RulesViewModel
import com.example.scylier.istudyspot.viewmodel.RulesUiState
import com.example.scylier.istudyspot.models.RuleItem
import com.example.scylier.istudyspot.models.RuleType
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
class RulesViewModelTest {

    private lateinit var viewModel: RulesViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockRepository = mockk<MainRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockRepository.getRules(any(), any()) } returns ApiResponse.Error(500, "Test error")
        viewModel = RulesViewModel(mockRepository)
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
    fun testViewModel_initialState_emptyRuleItems() {
        assertTrue(viewModel.state.value.ruleItems.isEmpty())
    }

    @Test
    fun testViewModel_loadRules_fallsBackToMock() = runTest {
        viewModel.loadRules()
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.ruleItems.isNotEmpty())
    }

    @Test
    fun testViewModel_mockRuleItems_size() = runTest {
        viewModel.loadRules()
        val state = viewModel.state.value
        assertEquals(11, state.ruleItems.size)
    }

    @Test
    fun testViewModel_groupedItems_notEmpty() = runTest {
        viewModel.loadRules()
        assertTrue(viewModel.groupedItems.isNotEmpty())
    }

    @Test
    fun testViewModel_groupedItems_size() = runTest {
        viewModel.loadRules()
        assertEquals(2, viewModel.groupedItems.size)
    }

    @Test
    fun testViewModel_groupedItems_containsMainRules() = runTest {
        viewModel.loadRules()
        assertTrue(viewModel.groupedItems.containsKey("主要规则"))
    }

    @Test
    fun testViewModel_groupedItems_containsFAQ() = runTest {
        viewModel.loadRules()
        assertTrue(viewModel.groupedItems.containsKey("常见问题"))
    }

    @Test
    fun testViewModel_mainRules_size() = runTest {
        viewModel.loadRules()
        val mainRules = viewModel.groupedItems["主要规则"]
        assertEquals(5, mainRules?.size)
    }

    @Test
    fun testViewModel_faq_size() = runTest {
        viewModel.loadRules()
        val faq = viewModel.groupedItems["常见问题"]
        assertEquals(6, faq?.size)
    }

    @Test
    fun testViewModel_firstRule_title() = runTest {
        viewModel.loadRules()
        val firstRule = viewModel.state.value.ruleItems[0]
        assertEquals("预约规则", firstRule.title)
    }

    @Test
    fun testViewModel_firstRule_category() = runTest {
        viewModel.loadRules()
        val firstRule = viewModel.state.value.ruleItems[0]
        assertEquals("主要规则", firstRule.category)
    }

    @Test
    fun testViewModel_bookingRule_content() = runTest {
        viewModel.loadRules()
        val bookingRule = viewModel.state.value.ruleItems.find { it.title == "预约规则" }
        assertNotNull(bookingRule)
        assertTrue(bookingRule!!.content.contains("预约"))
    }

    @Test
    fun testViewModel_checkinRule_content() = runTest {
        viewModel.loadRules()
        val checkinRule = viewModel.state.value.ruleItems.find { it.title == "签到规则" }
        assertNotNull(checkinRule)
        assertTrue(checkinRule!!.content.contains("签到"))
    }

    @Test
    fun testViewModel_leaveRule_content() = runTest {
        viewModel.loadRules()
        val leaveRule = viewModel.state.value.ruleItems.find { it.title == "离开规则" }
        assertNotNull(leaveRule)
        assertTrue(leaveRule!!.content.contains("离开"))
    }

    @Test
    fun testViewModel_violationRule_content() = runTest {
        viewModel.loadRules()
        val violationRule = viewModel.state.value.ruleItems.find { it.title == "违规处理" }
        assertNotNull(violationRule)
        assertTrue(violationRule!!.content.contains("违规"))
    }

    @Test
    fun testViewModel_civilizedRule_content() = runTest {
        viewModel.loadRules()
        val civilizedRule = viewModel.state.value.ruleItems.find { it.title == "文明使用" }
        assertNotNull(civilizedRule)
        assertTrue(civilizedRule!!.content.contains("整洁"))
    }

    @Test
    fun testViewModel_faq_bookingQuestion() = runTest {
        viewModel.loadRules()
        val faq = viewModel.state.value.ruleItems.find { it.title == "如何预约座位？" }
        assertNotNull(faq)
        assertTrue(faq!!.content.contains("预约座位"))
    }

    @Test
    fun testViewModel_faq_cancelQuestion() = runTest {
        viewModel.loadRules()
        val faq = viewModel.state.value.ruleItems.find { it.title == "预约后可以取消吗？" }
        assertNotNull(faq)
        assertTrue(faq!!.content.contains("取消"))
    }

    @Test
    fun testViewModel_allRulesHaveContent() = runTest {
        viewModel.loadRules()
        viewModel.state.value.ruleItems.forEach { rule ->
            assertTrue(rule.title.isNotEmpty())
            assertTrue(rule.content.isNotEmpty())
            assertTrue(rule.category.isNotEmpty())
        }
    }

    @Test
    fun testRuleItem_dataClass() {
        val rule = RuleItem(
            type = RuleType.RULE,
            title = "测试规则",
            content = "测试内容",
            category = "测试分类"
        )
        assertEquals(RuleType.RULE, rule.type)
        assertEquals("测试规则", rule.title)
        assertEquals("测试内容", rule.content)
        assertEquals("测试分类", rule.category)
    }

    @Test
    fun testRuleType_values() {
        val types = RuleType.values()
        assertEquals(2, types.size)
        assertTrue(types.contains(RuleType.RULE))
        assertTrue(types.contains(RuleType.FAQ))
    }
}
