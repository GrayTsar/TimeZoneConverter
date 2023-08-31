package com.graytsar.timezoneconverter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.graytsar.timezoneconverter.databinding.ItemTimeZoneBinding

class AdapterTimeZone(
    private val activity: CronActivity,
    private val listener: (UITimeZone) -> Unit
) : ListAdapter<UITimeZone, AdapterTimeZone.ViewHolderTimeZone>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderTimeZone {
        val binding = ItemTimeZoneBinding.inflate(
            LayoutInflater.from(activity),
            parent,
            false
        )
        return ViewHolderTimeZone(binding)
    }

    override fun onBindViewHolder(holder: ViewHolderTimeZone, position: Int) {
        when (val item = getItem(position)) {
            is UITimeZone -> {
                holder.longName.text = item.longName
                holder.zoneId.text = item.id
                holder.offset.text = item.offset
                holder.itemView.setOnClickListener { listener(item) }
            }

            else -> {
                holder.longName.text = ""
                holder.zoneId.text = ""
                holder.offset.text = ""
                holder.itemView.setOnClickListener(null)
            }
        }
    }

    inner class ViewHolderTimeZone(
        binding: ItemTimeZoneBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val longName = binding.longName
        val zoneId = binding.zoneId
        val offset = binding.offset
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UITimeZone>() {
            override fun areItemsTheSame(oldItem: UITimeZone, newItem: UITimeZone) =
                oldItem.id == newItem.id && oldItem.longName == newItem.longName && oldItem.offset == newItem.offset

            override fun areContentsTheSame(oldItem: UITimeZone, newItem: UITimeZone) =
                oldItem.id == newItem.id && oldItem.longName == newItem.longName && oldItem.offset == newItem.offset
        }
    }
}