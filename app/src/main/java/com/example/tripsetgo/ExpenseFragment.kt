package com.example.tripsetgo

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ExpenseFragment : Fragment() {

    private lateinit var createGroupButton: Button
    private lateinit var groupsRecyclerView: RecyclerView
    private lateinit var groupsAdapter: GroupAdapter
    private val groupsList = mutableListOf<Group>()
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)  // Enable options menu for this fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_expense_tracker, container, false)

        // Initialize views
        firestore = FirebaseFirestore.getInstance()
        groupsRecyclerView = view.findViewById(R.id.groupsRecyclerView)
        groupsRecyclerView.layoutManager = LinearLayoutManager(context)
        groupsAdapter = GroupAdapter(groupsList) { group -> openGroup(group) }
        groupsRecyclerView.adapter = groupsAdapter

        createGroupButton = view.findViewById(R.id.addGroupButton)

        // Set click listener for the create group button
        createGroupButton.setOnClickListener {
            val intent = Intent(activity, CreateGroupAndAddMembersActivity::class.java)
            startActivity(intent)
        }

        loadGroups()
        return view
    }

    private fun loadGroups() {
        firestore.collection("groups")
            .get()
            .addOnSuccessListener { documents ->
                groupsList.clear()
                for (document in documents) {
                    val group = document.toObject(Group::class.java)
                    group.id = document.id
                    groupsList.add(group)
                }
                groupsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting groups: ", exception)
                Toast.makeText(
                    context,
                    "Error loading groups: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_group -> {
                // Navigate to CreateGroupActivity
                val intent = Intent(activity, CreateGroupAndAddMembersActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openGroup(group: Group) {
        // Open the ExpenseActivity for the selected group
        val intent = Intent(activity, ExpenseActivity::class.java)
        intent.putExtra("GROUP_ID", group.id)
        startActivity(intent)
    }
}
