package com.example.myfirstapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstapplication.data.IrRemoteCodeItem

class IrRemoteCodeAdapter(
    private val onClick: (IrRemoteCodeItem) -> Unit,
    private val onLongClick: (IrRemoteCodeItem) -> Boolean
) :
    ListAdapter<IrRemoteCodeItem, IrRemoteCodeAdapter.IrRemoconCodeViewHolder>(
        IrRemoconCodeDiffCallback
    ) {

    class IrRemoconCodeViewHolder(
        itemView: View,
        val onClick: (IrRemoteCodeItem) -> Unit,
        val onLongClick: (IrRemoteCodeItem) -> Boolean
    ) :
        RecyclerView.ViewHolder(itemView) {
        private val irRemoteCodeName =
            itemView.findViewById<TextView>(R.id.ir_remote_code_item_name)
        private val irRemoteCodeNumber =
            itemView.findViewById<TextView>(R.id.ir_remote_code_item_number)
        private var currentIrRemoteCodeItem: IrRemoteCodeItem? = null

        init {
            itemView.setOnClickListener {
                currentIrRemoteCodeItem?.let {
                    onClick(it)
                }
            }
            itemView.setOnLongClickListener {
                currentIrRemoteCodeItem?.let {
                    onLongClick(it)
                } ?: false
            }
        }

        fun bind(irRemoteCodeItem: IrRemoteCodeItem) {
            currentIrRemoteCodeItem = irRemoteCodeItem
            irRemoteCodeName.text = irRemoteCodeItem.name
            irRemoteCodeNumber.text = irRemoteCodeItem.id.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IrRemoconCodeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ir_remote_code_item, parent, false)
        return IrRemoconCodeViewHolder(view, onClick, onLongClick)
    }

    override fun onBindViewHolder(holder: IrRemoconCodeViewHolder, position: Int) {
        val ircode = getItem(position)
        holder.bind(ircode)
    }
}

object IrRemoconCodeDiffCallback : DiffUtil.ItemCallback<IrRemoteCodeItem>() {
    override fun areItemsTheSame(oldItem: IrRemoteCodeItem, newItem: IrRemoteCodeItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: IrRemoteCodeItem, newItem: IrRemoteCodeItem): Boolean {
        return oldItem.id == newItem.id
    }
}