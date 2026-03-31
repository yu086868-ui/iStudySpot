package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.models.MoreItem

class MoreViewModel : ViewModel() {
    // 更多功能相关的数据和逻辑
    val moreItems = listOf(
        // 记录查询分类
        MoreItem(
            R.drawable.ic_history,
            "预约记录",
            "查看历史预约信息",
            R.color.blue_50,
            R.color.blue_600,
            "记录查询"
        ),
        MoreItem(
            R.drawable.ic_alert_triangle,
            "违规记录",
            "查看违规详情",
            R.color.red_50,
            R.color.red_600,
            "记录查询"
        ),
        MoreItem(
            R.drawable.ic_trending_up,
            "学习统计",
            "查看学习时长统计",
            R.color.green_50,
            R.color.green_600,
            "记录查询"
        ),
        MoreItem(
            R.drawable.ic_award,
            "成就徽章",
            "查看获得的成就",
            R.color.yellow_50,
            R.color.yellow_600,
            "记录查询"
        ),
        
        // 积分福利分类
        MoreItem(
            R.drawable.ic_gift,
            "积分兑换",
            "使用积分兑换奖励",
            R.color.purple_50,
            R.color.purple_600,
            "积分福利"
        ),
        MoreItem(
            R.drawable.ic_file_text,
            "积分明细",
            "查看积分收支记录",
            R.color.indigo_50,
            R.color.indigo_600,
            "积分福利"
        ),
        
        // 帮助与支持分类
        MoreItem(
            R.drawable.ic_help_circle,
            "帮助中心",
            "常见问题解答",
            R.color.teal_50,
            R.color.teal_600,
            "帮助与支持"
        ),
        MoreItem(
            R.drawable.ic_message_square,
            "意见反馈",
            "提交意见和建议",
            R.color.orange_50,
            R.color.orange_600,
            "帮助与支持"
        ),
        MoreItem(
            R.drawable.ic_info,
            "关于我们",
            "了解更多信息",
            R.color.gray_50,
            R.color.gray_600,
            "帮助与支持"
        ),
        
        // 其他功能分类
        MoreItem(
            R.drawable.ic_share2,
            "推荐好友",
            "邀请好友得积分",
            R.color.pink_50,
            R.color.pink_600,
            "其他功能"
        ),
        MoreItem(
            R.drawable.ic_download,
            "数据导出",
            "导出个人学习数据",
            R.color.cyan_50,
            R.color.cyan_600,
            "其他功能"
        ),
        MoreItem(
            R.drawable.ic_shield,
            "隐私政策",
            "查看隐私保护政策",
            R.color.slate_50,
            R.color.slate_600,
            "其他功能"
        )
    )
    
    // 按分类分组的功能项
    val groupedItems: Map<String, List<MoreItem>> by lazy {
        moreItems.groupBy { it.category }
    }
}