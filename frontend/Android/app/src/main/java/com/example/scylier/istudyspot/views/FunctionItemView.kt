package com.example.scylier.istudyspot.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.scylier.istudyspot.R

class FunctionItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val imageView: ImageView
    private val textView: TextView

    init {
        // 加载XML布局
        LayoutInflater.from(context).inflate(R.layout.function_item_view, this, true)

        // 获取ImageView和TextView的引用
        imageView = findViewById(R.id.icon_image)
        textView = findViewById(R.id.title_text)
    }

    fun setIcon(iconRes: Int) {
        imageView.setImageResource(iconRes)
    }

    fun setTitle(title: String) {
        textView.text = title
    }

    fun setColor(colorRes: Int) {
        imageView.setColorFilter(context.resources.getColor(colorRes, context.theme))
    }
}