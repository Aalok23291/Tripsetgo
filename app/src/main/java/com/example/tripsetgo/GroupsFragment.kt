package com.example.tripsetgo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsetgo.Group
import com.example.tripsetgo.GroupAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class GroupsFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private val groupsList = mutableListOf<Group>()
    private lateinit var groupListener: ListenerRegistration

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.groupsRecyclerView)
        groupAdapter = GroupAdapter(groupsList) { group ->
            // Handle group click (optional)
        }
        recyclerView.adapter = groupAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        fetchGroups()
    }

    private fun fetchGroups() {
        val userId = auth.currentUser?.uid ?: return
        groupListener = firestore.collection("groups")
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Error fetching groups: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                groupsList.clear() // Clear previous data
                snapshot?.documents?.forEach { document ->
                    val group = document.toObject(Group::class.java)
                    group?.let { groupsList.add(it) }
                }
                groupAdapter.notifyDataSetChanged() // Notify the adapter of data changes
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        groupListener.remove() // Stop listening to changes when the fragment is destroyed
    }
}
