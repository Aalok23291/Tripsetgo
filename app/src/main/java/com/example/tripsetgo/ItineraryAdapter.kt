
package com.example.tripsetgo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItineraryAdapter(private val itineraries: List<Itinerary>) :
    RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder>() {

    inner class ItineraryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.itinerary_name)
        val address: TextView = itemView.findViewById(R.id.itinerary_address)
        val distance: TextView = itemView.findViewById(R.id.itinerary_distance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItineraryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itinerary_item, parent, false)
        return ItineraryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItineraryViewHolder, position: Int) {
        val itinerary = itineraries[position]
        holder.name.text = itinerary.name
        holder.address.text = itinerary.address
        holder.distance.text = itinerary.distance
    }

    override fun getItemCount(): Int = itineraries.size
}
