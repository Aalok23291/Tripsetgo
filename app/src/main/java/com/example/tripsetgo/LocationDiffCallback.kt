package com.example.tripsetgo

import androidx.recyclerview.widget.DiffUtil

class LocationDiffCallback : DiffUtil.ItemCallback<LocationData>() {
    override fun areItemsTheSame(oldItem: LocationData, newItem: LocationData): Boolean {
        // Compare unique identifiers, such as an ID or name
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: LocationData, newItem: LocationData): Boolean {
        // Compare the actual data for equality
        return oldItem == newItem
    }
}
