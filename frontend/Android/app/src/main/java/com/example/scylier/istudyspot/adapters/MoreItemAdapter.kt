package com.example.scylier.istudyspot.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.models.MoreItem
import com.example.scylier.istudyspot.views.MoreItemView

/**
 * 表示RecyclerView中的项目类型
 */
sealed class MoreAdapterItem {
    data class CategoryItem(val category: String) : MoreAdapterItem()
    data class FunctionItem(val moreItem: MoreItem) : MoreAdapterItem()
}

class MoreItemAdapter(
    private val groupedItems: Map<String, List<MoreItem>>,
    private val onItemClickListener: (MoreItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 创建一个扁平化的项目列表
    private val items: List<MoreAdapterItem> by lazy {
        val result = mutableListOf<MoreAdapterItem>()
        groupedItems.forEach { (category, items) ->
            result.add(MoreAdapterItem.CategoryItem(category))
            items.forEach {
                result.add(MoreAdapterItem.FunctionItem(it))
            }
        }
        result
    }

    // 定义项目类型
    companion object {
        const val TYPE_CATEGORY = 0
        const val TYPE_FUNCTION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is MoreAdapterItem.CategoryItem -> TYPE_CATEGORY
            is MoreAdapterItem.FunctionItem -> TYPE_FUNCTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_CATEGORY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.more_category_header, parent, false)
                CategoryViewHolder(view)
            }
            TYPE_FUNCTION -> {
                val view = MoreItemView(parent.context)
                FunctionViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is MoreAdapterItem.CategoryItem -> {
                (holder as CategoryViewHolder).bind(item.category)
            }
            is MoreAdapterItem.FunctionItem -> {
                (holder as FunctionViewHolder).bind(item.moreItem)
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

    // 功能项ViewHolder
    inner class FunctionViewHolder(private val moreItemView: MoreItemView) : RecyclerView.ViewHolder(moreItemView) {
        fun bind(item: MoreItem) {
            moreItemView.setIcon(item.icon)
            moreItemView.setTitle(item.title)
            moreItemView.setDescription(item.description)
            moreItemView.setIconBackgroundColor(item.bgColor)
            moreItemView.setIconColor(item.iconColor)
            
            moreItemView.setOnClickListener {
                onItemClickListener(item)
            }
        }
    }
}