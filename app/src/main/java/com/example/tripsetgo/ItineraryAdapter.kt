package com.example.tripsetgo

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView

class ItineraryAdapter : ListAdapter<Activity, ItineraryAdapter.ViewHolder>(ItineraryDiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val timeText: TextView = view.findViewById(R.id.timeText)
        private val activityTitle: TextView = view.findViewById(R.id.activityTitle)
        private val activityDescription: TextView = view.findViewById(R.id.activityDescription)
        private val locationText: TextView = view.findViewById(R.id.locationText)
        private val durationText: TextView = view.findViewById(R.id.durationText)
        private val bookButton: Button = view.findViewById(R.id.bookButton)
        private val activityImage: ImageView = view.findViewById(R.id.activityImage)
        private val imageContainer: MaterialCardView = view.findViewById(R.id.imageContainer)
        private val dayHeader: TextView = view.findViewById(R.id.dayHeader)

        fun bind(activity: Activity, showDayHeader: Boolean, dayNumber: Int) {
            // Handle day header
            if (showDayHeader) {
                dayHeader.visibility = View.VISIBLE
                dayHeader.text = "Day $dayNumber"
            } else {
                dayHeader.visibility = View.GONE
            }

            // Set basic information
            timeText.text = activity.time
            activityTitle.text = activity.title
            activityDescription.text = activity.description
            locationText.text = activity.location
            durationText.text = activity.duration

            // Handle booking URL
            if (activity.bookingUrl != null) {
                bookButton.visibility = View.VISIBLE
                bookButton.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(activity.bookingUrl))
                    itemView.context.startActivity(intent)
                }
            } else {
                bookButton.visibility = View.GONE
            }

            // Set color based on activity type
            val activityColor = when (activity.type) {
                ActivityType.HOTEL -> "#FF9800"
                ActivityType.ATTRACTION -> "#2196F3"
                ActivityType.RESTAURANT -> "#4CAF50"
                ActivityType.SHOPPING -> "#E91E63"
                ActivityType.LEISURE -> "#9C27B0"
            }
            bookButton.setBackgroundColor(Color.parseColor(activityColor))

            // Handle image loading
            activity.photoUrl?.let { url ->
                imageContainer.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(url)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(activityImage)
            } ?: run {
                imageContainer.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = getItem(position)
        // Show day header for first activity of each day
        val showDayHeader = position == 0 || position == 5 || position == 10
        val dayNumber = when {
            position < 5 -> 1
            position < 10 -> 2
            else -> 3
        }
        holder.bind(activity, showDayHeader, dayNumber)
    }
}

class ItineraryDiffCallback : DiffUtil.ItemCallback<Activity>() {
    override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean {
        return oldItem.time == newItem.time && oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean {
        return oldItem == newItem
    }
}