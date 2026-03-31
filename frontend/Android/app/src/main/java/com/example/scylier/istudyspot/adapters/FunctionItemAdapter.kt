package com.example.scylier.istudyspot.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scylier.istudyspot.models.FunctionItem
import com.example.scylier.istudyspot.views.FunctionItemView

class FunctionItemAdapter(
    private val items: List<FunctionItem>,
    private val onItemClick: (FunctionItem) -> Unit
) : RecyclerView.Adapter<FunctionItemAdapter.FunctionItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FunctionItemViewHolder {
        val functionItemView = FunctionItemView(parent.context)
        return FunctionItemViewHolder(functionItemView)
    }

    override fun onBindViewHolder(holder: FunctionItemViewHolder, position: Int) {
        val item = items[position]
        holder.functionItemView.setIcon(item.icon)
        holder.functionItemView.setTitle(item.title)
        holder.functionItemView.setColor(item.color)
        holder.functionItemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class FunctionItemViewHolder(val functionItemView: FunctionItemView) : RecyclerView.ViewHolder(functionItemView)
}