package com.example.tripsetgo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RentalAdapter(private val rentals: List<RentalLocation>) :
    RecyclerView.Adapter<RentalAdapter.RentalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rental_item, parent, false)
        return RentalViewHolder(view)
    }

    override fun onBindViewHolder(holder: RentalViewHolder, position: Int) {
        val rental = rentals[position]
        holder.nameTextView.text = rental.name
        holder.priceTextView.text = rental.price
    }

    override fun getItemCount(): Int = rentals.size

    class RentalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
    }
}
