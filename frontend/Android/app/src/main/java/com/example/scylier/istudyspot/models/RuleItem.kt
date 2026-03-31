package com.example.scylier.istudyspot.models

/**
 * 表示规则类型
 */
enum class RuleType {
    RULE, // 规则条目
    FAQ // 常见问题条目
}

/**
 * 表示RulesFragment中的规则和问题条目
 * @param type 条目类型
 * @param title 标题（规则标题或问题）
 * @param content 内容（规则描述或答案）
 * @param category 所属分类
 */
data class RuleItem(
    val type: RuleType,
    val title: String,
    val content: String,
    val category: String
)