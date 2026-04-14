package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel

/**
 * 场馆导览ViewModel
 */
class GuideViewModel : ViewModel() {

    data class Facility(
        val name: String,
        val description: String
    )

    // 场馆设施列表
    val facilities = listOf(
        Facility("静音区", "绝对安静的学习区域，禁止交谈，适合深度学习和阅读"),
        Facility("讨论区", "允许低声讨论的区域，适合小组学习和交流"),
        Facility("休息区", "提供沙发和茶水，可以休息放松"),
        Facility("打印区", "提供自助打印、复印服务"),
        Facility("储物柜", "提供临时储物柜存放个人物品"),
        Facility("饮水机", "免费提供冷热饮用水"),
        Facility("WiFi覆盖", "全馆高速WiFi覆盖，支持在线学习"),
        Facility("空调系统", "中央空调，四季恒温舒适")
    )

    // 场馆地址
    val location = "XX市XX区XX路XX号XX大厦X层"

    // 营业时间
    val openingHours = "周一至周日 08:00 - 23:00（节假日照常营业）"

    // 联系方式
    val contact = "电话：400-XXX-XXXX\n邮箱：contact@istudyspot.com"
}
