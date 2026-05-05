package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.viewmodel.RulesViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RulesViewModelTest {

    private lateinit var viewModel: RulesViewModel

    @Before
    fun setup() {
        viewModel = RulesViewModel()
    }

    @Test
    fun testViewModel_ruleItems_notEmpty() {
        assertTrue(viewModel.ruleItems.isNotEmpty())
    }

    @Test
    fun testViewModel_ruleItems_size() {
        assertEquals(11, viewModel.ruleItems.size)
    }

    @Test
    fun testViewModel_groupedItems_notEmpty() {
        assertTrue(viewModel.groupedItems.isNotEmpty())
    }

    @Test
    fun testViewModel_groupedItems_size() {
        assertEquals(2, viewModel.groupedItems.size)
    }

    @Test
    fun testViewModel_groupedItems_containsMainRules() {
        assertTrue(viewModel.groupedItems.containsKey("主要规则"))
    }

    @Test
    fun testViewModel_groupedItems_containsFAQ() {
        assertTrue(viewModel.groupedItems.containsKey("常见问题"))
    }

    @Test
    fun testViewModel_mainRules_size() {
        val mainRules = viewModel.groupedItems["主要规则"]
        assertEquals(5, mainRules?.size)
    }

    @Test
    fun testViewModel_faq_size() {
        val faq = viewModel.groupedItems["常见问题"]
        assertEquals(6, faq?.size)
    }

    @Test
    fun testViewModel_firstRule_title() {
        val firstRule = viewModel.ruleItems[0]
        assertEquals("预约规则", firstRule.title)
    }

    @Test
    fun testViewModel_firstRule_category() {
        val firstRule = viewModel.ruleItems[0]
        assertEquals("主要规则", firstRule.category)
    }

    @Test
    fun testViewModel_bookingRule_content() {
        val bookingRule = viewModel.ruleItems.find { it.title == "预约规则" }
        assertNotNull(bookingRule)
        assertTrue(bookingRule!!.content.contains("预约"))
    }

    @Test
    fun testViewModel_checkinRule_content() {
        val checkinRule = viewModel.ruleItems.find { it.title == "签到规则" }
        assertNotNull(checkinRule)
        assertTrue(checkinRule!!.content.contains("签到"))
    }

    @Test
    fun testViewModel_leaveRule_content() {
        val leaveRule = viewModel.ruleItems.find { it.title == "离开规则" }
        assertNotNull(leaveRule)
        assertTrue(leaveRule!!.content.contains("离开"))
    }

    @Test
    fun testViewModel_violationRule_content() {
        val violationRule = viewModel.ruleItems.find { it.title == "违规处理" }
        assertNotNull(violationRule)
        assertTrue(violationRule!!.content.contains("违规"))
    }

    @Test
    fun testViewModel_civilizedRule_content() {
        val civilizedRule = viewModel.ruleItems.find { it.title == "文明使用" }
        assertNotNull(civilizedRule)
        assertTrue(civilizedRule!!.content.contains("整洁"))
    }

    @Test
    fun testViewModel_faq_bookingQuestion() {
        val faq = viewModel.ruleItems.find { it.title == "如何预约座位？" }
        assertNotNull(faq)
        assertTrue(faq!!.content.contains("预约座位"))
    }

    @Test
    fun testViewModel_faq_cancelQuestion() {
        val faq = viewModel.ruleItems.find { it.title == "预约后可以取消吗？" }
        assertNotNull(faq)
        assertTrue(faq!!.content.contains("取消"))
    }

    @Test
    fun testViewModel_faq_forgetCheckinQuestion() {
        val faq = viewModel.ruleItems.find { it.title == "忘记签到怎么办？" }
        assertNotNull(faq)
        assertTrue(faq!!.content.contains("签到"))
    }

    @Test
    fun testViewModel_faq_helpFriendQuestion() {
        val faq = viewModel.ruleItems.find { it.title == "可以帮朋友预约吗？" }
        assertNotNull(faq)
        assertTrue(faq!!.content.contains("不可以"))
    }

    @Test
    fun testViewModel_faq_renewQuestion() {
        val faq = viewModel.ruleItems.find { it.title == "座位可以续约吗？" }
        assertNotNull(faq)
        assertTrue(faq!!.content.contains("续约"))
    }

    @Test
    fun testViewModel_faq_appealQuestion() {
        val faq = viewModel.ruleItems.find { it.title == "违规记录可以申诉吗？" }
        assertNotNull(faq)
        assertTrue(faq!!.content.contains("申诉"))
    }

    @Test
    fun testViewModel_allRulesHaveContent() {
        viewModel.ruleItems.forEach { rule ->
            assertTrue(rule.title.isNotEmpty())
            assertTrue(rule.content.isNotEmpty())
            assertTrue(rule.category.isNotEmpty())
        }
    }
}
