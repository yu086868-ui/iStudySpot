package com.example.scylier.istudyspot.models

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

/**
 * 表示MoreFragment中的功能项
 * @param icon 图标资源ID
 * @param title 标题
 * @param description 描述
 * @param bgColor 背景颜色资源ID
 * @param iconColor 图标颜色资源ID
 * @param category 所属分类
 */
data class MoreItem(
    @DrawableRes val icon: Int,
    val title: String,
    val description: String,
    @ColorRes val bgColor: Int,
    @ColorRes val iconColor: Int,
    val category: String
)