package com.graytsar.timezoneconverter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.graytsar.timezoneconverter.databinding.ItemSearchBinding
import kotlinx.android.synthetic.main.item_search.view.*

class AdapterTimeZone(private val activity:MainActivity): ListAdapter<ModelTimeZone, ViewHolderTimeZone>(DiffCallbackTimeZone())  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderTimeZone {
        val binding = DataBindingUtil.inflate<ItemSearchBinding>(LayoutInflater.from(activity), R.layout.item_search, parent, false)
        return ViewHolderTimeZone(binding.root, binding)
    }

    override fun onBindViewHolder(holder: ViewHolderTimeZone, position: Int) {
        val item = getItem(position)

        holder.binding.lifecycleOwner = activity
        holder.binding.modelTimeZone = item

        val a = 0
    }
}

class ViewHolderTimeZone(view: View, val binding:ItemSearchBinding): RecyclerView.ViewHolder(view){
}

class DiffCallbackTimeZone: DiffUtil.ItemCallback<ModelTimeZone>(){
    override fun areItemsTheSame(oldItem: ModelTimeZone, newItem: ModelTimeZone): Boolean {
        return oldItem.id == newItem.id && oldItem.longName == newItem.longName && oldItem.offset == newItem.offset
    }

    override fun areContentsTheSame(oldItem: ModelTimeZone, newItem: ModelTimeZone): Boolean {
        return oldItem.id == newItem.id && oldItem.longName == newItem.longName && oldItem.offset == newItem.offset
    }
}