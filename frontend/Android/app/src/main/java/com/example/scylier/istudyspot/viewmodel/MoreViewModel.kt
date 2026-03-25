package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel

class MoreViewModel : ViewModel() {
    // 更多功能相关的数据和逻辑
    val features = listOf(
        FeatureCategory(
            "记录查询",
            listOf(
                FeatureItem("预约记录", "查看历史预约信息", "blue_600", "blue_50"),
                FeatureItem("违规记录", "查看违规详情", "red_600", "red_50"),
                FeatureItem("学习统计", "查看学习时长统计", "green_600", "green_50"),
                FeatureItem("成就徽章", "查看获得的成就", "yellow_600", "yellow_50")
            )
        ),
        FeatureCategory(
            "积分福利",
            listOf(
                FeatureItem("积分兑换", "使用积分兑换奖励", "purple_600", "purple_50"),
                FeatureItem("积分明细", "查看积分收支记录", "indigo_600", "indigo_50")
            )
        ),
        FeatureCategory(
            "帮助与支持",
            listOf(
                FeatureItem("帮助中心", "常见问题解答", "teal_600", "teal_50"),
                FeatureItem("意见反馈", "提交意见和建议", "orange_600", "orange_50"),
                FeatureItem("关于我们", "了解更多信息", "gray_600", "gray_50")
            )
        ),
        FeatureCategory(
            "其他功能",
            listOf(
                FeatureItem("推荐好友", "邀请好友得积分", "pink_600", "pink_50"),
                FeatureItem("数据导出", "导出个人学习数据", "cyan_600", "cyan_50"),
                FeatureItem("隐私政策", "查看隐私保护政策", "slate_600", "slate_50")
            )
        )
    )

    data class FeatureCategory(val category: String, val items: List<FeatureItem>)
    data class FeatureItem(val label: String, val desc: String, val color: String, val bgColor: String)
}
