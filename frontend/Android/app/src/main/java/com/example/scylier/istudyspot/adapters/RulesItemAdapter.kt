package com.example.scylier.istudyspot.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.models.RuleItem
import com.example.scylier.istudyspot.models.RuleType
import com.example.scylier.istudyspot.views.FAQItemView
import com.example.scylier.istudyspot.views.RuleItemView

/**
 * 表示RecyclerView中的项目类型
 */
sealed class RulesAdapterItem {
    data class CategoryItem(val category: String) : RulesAdapterItem()
    data class RuleItem(val ruleItem: com.example.scylier.istudyspot.models.RuleItem) : RulesAdapterItem()
}

class RulesItemAdapter(private val groupedItems: Map<String, List<RuleItem>>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 创建一个扁平化的项目列表
    private val items: List<RulesAdapterItem> by lazy {
        val result = mutableListOf<RulesAdapterItem>()
        groupedItems.forEach { (category, items) ->
            result.add(RulesAdapterItem.CategoryItem(category))
            items.forEach {
                result.add(RulesAdapterItem.RuleItem(it))
            }
        }
        result
    }

    // 定义项目类型
    companion object {
        const val TYPE_CATEGORY = 0
        const val TYPE_RULE = 1
        const val TYPE_FAQ = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is RulesAdapterItem.CategoryItem -> TYPE_CATEGORY
            is RulesAdapterItem.RuleItem -> {
                when (item.ruleItem.type) {
                    RuleType.RULE -> TYPE_RULE
                    RuleType.FAQ -> TYPE_FAQ
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_CATEGORY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.rules_category_header, parent, false)
                CategoryViewHolder(view)
            }
            TYPE_RULE -> {
                val view = RuleItemView(parent.context)
                RuleViewHolder(view)
            }
            TYPE_FAQ -> {
                val view = FAQItemView(parent.context)
                FAQViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is RulesAdapterItem.CategoryItem -> {
                (holder as CategoryViewHolder).bind(item.category)
            }
            is RulesAdapterItem.RuleItem -> {
                when (item.ruleItem.type) {
                    RuleType.RULE -> {
                        (holder as RuleViewHolder).bind(item.ruleItem)
                    }
                    RuleType.FAQ -> {
                        (holder as FAQViewHolder).bind(item.ruleItem)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // 分类标题ViewHolder
    class CategoryViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val categoryTextView: android.widget.TextView = itemView.findViewById(R.id.category_title)

        fun bind(category: String) {
            categoryTextView.text = category
        }
    }

    // 规则项ViewHolder
    class RuleViewHolder(private val ruleItemView: RuleItemView) : RecyclerView.ViewHolder(ruleItemView) {
        fun bind(item: RuleItem) {
            ruleItemView.setTitle(item.title)
            ruleItemView.setContent(item.content)
        }
    }

    // 常见问题ViewHolder
    class FAQViewHolder(private val faqItemView: FAQItemView) : RecyclerView.ViewHolder(faqItemView) {
        fun bind(item: RuleItem) {
            faqItemView.setQuestion(item.title)
            faqItemView.setAnswer(item.content)
        }
    }
}