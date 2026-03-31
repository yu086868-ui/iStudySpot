package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import com.example.scylier.istudyspot.models.RuleItem
import com.example.scylier.istudyspot.models.RuleType

class RulesViewModel : ViewModel() {
    // 规则相关的数据和逻辑
    val ruleItems = listOf(
        // 主要规则分类
        RuleItem(
            RuleType.RULE,
            "预约规则",
            "每位用户每天最多可预约一个座位，预约时间段为开馆至闭馆时间。请在预约成功后30分钟内到达座位签到，否则预约将自动取消。",
            "主要规则"
        ),
        RuleItem(
            RuleType.RULE,
            "签到规则",
            "预约成功后，请在预约时间开始后30分钟内完成签到。签到方式为扫描座位二维码或在APP内点击签到按钮。未按时签到将被记录为违规。",
            "主要规则"
        ),
        RuleItem(
            RuleType.RULE,
            "离开规则",
            "暂时离开座位不超过30分钟无需操作。离开超过30分钟需要在APP内申请暂离，每天最多申请3次暂离，每次暂离不超过2小时。",
            "主要规则"
        ),
        RuleItem(
            RuleType.RULE,
            "违规处理",
            "累计3次违规将被禁止预约7天，累计5次违规将被禁止预约30天。违规行为包括：未按时签到、恶意占座、转让座位等。",
            "主要规则"
        ),
        RuleItem(
            RuleType.RULE,
            "文明使用",
            "请保持座位及周边环境整洁，不得在学习区域大声喧哗。请勿长时间占用座位而不学习。离开时请带走个人物品。",
            "主要规则"
        ),
        
        // 常见问题分类
        RuleItem(
            RuleType.FAQ,
            "如何预约座位？",
            "点击首页的【预约座位】功能，选择日期、时间段和座位，确认后即可完成预约。建议提前一天预约。",
            "常见问题"
        ),
        RuleItem(
            RuleType.FAQ,
            "预约后可以取消吗？",
            "可以。在预约时间开始前2小时可以免费取消预约。2小时内取消或未签到将被记录为违规。",
            "常见问题"
        ),
        RuleItem(
            RuleType.FAQ,
            "忘记签到怎么办？",
            "预约时间开始后30分钟内都可以签到。超过30分钟未签到，系统将自动取消预约并记录为违规。",
            "常见问题"
        ),
        RuleItem(
            RuleType.FAQ,
            "可以帮朋友预约吗？",
            "不可以。每个账号只能为本人预约，不得转让或代他人预约。发现此类行为将被记录违规。",
            "常见问题"
        ),
        RuleItem(
            RuleType.FAQ,
            "座位可以续约吗？",
            "当天使用的座位可以在APP内申请续约，续约需在当前预约结束前30分钟内操作，续约时长最多4小时。",
            "常见问题"
        ),
        RuleItem(
            RuleType.FAQ,
            "违规记录可以申诉吗？",
            "可以。在【更多】页面的【违规记录】中找到对应记录，点击【申诉】按钮提交申诉理由，管理员会在24小时内处理。",
            "常见问题"
        )
    )
    
    // 按分类分组的规则和问题
    val groupedItems: Map<String, List<RuleItem>> by lazy {
        ruleItems.groupBy { it.category }
    }
}