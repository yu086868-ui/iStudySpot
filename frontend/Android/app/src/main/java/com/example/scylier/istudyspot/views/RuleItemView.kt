package com.example.scylier.istudyspot.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.example.scylier.istudyspot.R

class RuleItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val titleTextView: TextView
    private val contentTextView: TextView

    init {
        // 加载XML布局
        LayoutInflater.from(context).inflate(R.layout.rule_item_view, this, true)

        // 获取组件引用
        titleTextView = findViewById(R.id.rule_title)
        contentTextView = findViewById(R.id.rule_content)
    }

    /**
     * 设置规则标题
     */
    fun setTitle(title: String) {
        titleTextView.text = title
    }

    /**
     * 设置规则内容
     */
    fun setContent(content: String) {
        contentTextView.text = content
    }
}