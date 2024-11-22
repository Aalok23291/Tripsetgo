package com.example.tripsetgo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsetgo.databinding.ItemLocationBinding

class LocationAdapter : ListAdapter<LocationSearchResult, LocationAdapter.LocationViewHolder>(LocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLocationBinding.inflate(inflater, parent, false)
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LocationViewHolder(private val binding: ItemLocationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(location: LocationSearchResult) {
            binding.apply {
                locationName.text = location.name
                locationAddress.text = location.address_obj.address_string ?:
                        buildAddressString(location.address_obj)
            }
        }

        private fun buildAddressString(address: AddressObject): String {
            return listOfNotNull(
                address.street1,
                address.street2,
                address.city,
                address.state,
                address.country
            ).joinToString(", ")
        }
    }

    class LocationDiffCallback : DiffUtil.ItemCallback<LocationSearchResult>() {
        override fun areItemsTheSame(oldItem: LocationSearchResult, newItem: LocationSearchResult): Boolean {
            return oldItem.location_id == newItem.location_id
        }

        override fun areContentsTheSame(oldItem: LocationSearchResult, newItem: LocationSearchResult): Boolean {
            return oldItem == newItem
        }
    }
}

