package com.example.scylier.istudyspot.models.card

data class CardItem(
    val uuid: String,
    val rarity: String,
    val borderTheme: String,
    val cardTheme: String,
    val themeCategory: String,
    val markdown: String,
    val studyDuration: Int,
    val createTime: String? = null,
    val imageURL: String? = null
)
