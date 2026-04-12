package com.example.scylier.istudyspot.models

import android.content.res.Resources
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class FunctionItem(
    val id: String,
    @DrawableRes val icon: Int,
    val title: String,
    @ColorRes val color: Int
)