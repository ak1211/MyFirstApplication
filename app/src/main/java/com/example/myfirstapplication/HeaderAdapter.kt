package com.example.myfirstapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HeaderAdapter : RecyclerView.Adapter<HeaderAdapter.HeaderViewHolder>() {
    private var irRemoconCodeCount: Int = 0

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val numberTextView =
            itemView.findViewById<TextView>(R.id.header_item_num_of_items_text)

        fun bind(irRemoconCodeCount: Int) {
            numberTextView.text = irRemoconCodeCount.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.header_item, parent, false)
        return HeaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind(irRemoconCodeCount)
    }

    override fun getItemCount(): Int {
        return 1
    }

    fun updateIrRemoconCodeCount(updatedIrRemoconCodeCount: Int) {
        irRemoconCodeCount = updatedIrRemoconCodeCount
        notifyDataSetChanged()
    }
}