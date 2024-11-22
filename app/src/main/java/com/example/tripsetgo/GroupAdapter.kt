package com.example.tripsetgo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupAdapter(
    private var groups: List<Group>,
    private val itemClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {
    private var fullList: List<Group> = groups

    // ViewHolder for each group item
    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupName: TextView = itemView.findViewById(R.id.groupNameText)
    }

    // Inflate the layout for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]

        // Set the group name in the TextView
        holder.groupName.text = group.name ?: "Unnamed Group"

        // Set click listener for item
        holder.itemView.setOnClickListener { itemClick(group) }
    }
    fun updateList(newList: List<Group>) {
        groups = newList
        notifyDataSetChanged()
    }
    fun filter(query: String) {
        groups = if (query.isEmpty()) {
            fullList
        } else {
            fullList.filter { it.name.contains(query, ignoreCase = true) } // Adjust based on group properties
        }
        notifyDataSetChanged()
    }

    // Return the total count of items
    override fun getItemCount() = groups.size
}
