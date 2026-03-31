package com.example.scylier.istudyspot.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.example.scylier.istudyspot.R

class FAQItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val questionTextView: TextView
    private val answerTextView: TextView

    init {
        // 加载XML布局
        LayoutInflater.from(context).inflate(R.layout.faq_item_view, this, true)

        // 获取组件引用
        questionTextView = findViewById(R.id.faq_question)
        answerTextView = findViewById(R.id.faq_answer)
    }

    /**
     * 设置问题
     */
    fun setQuestion(question: String) {
        questionTextView.text = question
    }

    /**
     * 设置答案
     */
    fun setAnswer(answer: String) {
        answerTextView.text = answer
    }
}