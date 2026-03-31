package com.example.scylier.istudyspot.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.scylier.istudyspot.R

class MoreItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val iconContainer: LinearLayout
    private val iconImageView: ImageView
    private val titleTextView: TextView
    private val descriptionTextView: TextView

    init {
        // 加载XML布局
        LayoutInflater.from(context).inflate(R.layout.more_item_view, this, true)

        // 获取组件引用
        iconContainer = findViewById(R.id.icon_container)
        iconImageView = findViewById(R.id.icon_image)
        titleTextView = findViewById(R.id.title_text)
        descriptionTextView = findViewById(R.id.description_text)
    }

    /**
     * 设置图标
     */
    fun setIcon(iconRes: Int) {
        iconImageView.setImageResource(iconRes)
    }

    /**
     * 设置标题
     */
    fun setTitle(title: String) {
        titleTextView.text = title
    }

    /**
     * 设置描述
     */
    fun setDescription(description: String) {
        descriptionTextView.text = description
    }

    /**
     * 设置背景颜色
     */
    fun setIconBackgroundColor(colorRes: Int) {
        iconContainer.setBackgroundColor(context.resources.getColor(colorRes, context.theme))
    }

    /**
     * 设置图标颜色
     */
    fun setIconColor(colorRes: Int) {
        iconImageView.setColorFilter(context.resources.getColor(colorRes, context.theme))
    }
}