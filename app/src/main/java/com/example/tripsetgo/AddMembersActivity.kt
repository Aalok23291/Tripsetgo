package com.example.tripsetgo

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue

class AddMembersActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var addButton: Button
    private lateinit var continueButton: Button // New button for navigation
    private lateinit var membersRecyclerView: RecyclerView
    private lateinit var membersAdapter: MembersAdapter
    private val membersList = mutableListOf<String>()
    private lateinit var selectedGroup: Group
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_members)

        firestore = FirebaseFirestore.getInstance()
        searchInput = findViewById(R.id.searchInput)
        addButton = findViewById(R.id.addButton)
        continueButton = findViewById(R.id.continueButton) // Initialize the new button
        membersRecyclerView = findViewById(R.id.membersRecyclerView)

        membersRecyclerView.layoutManager = LinearLayoutManager(this)
        membersAdapter = MembersAdapter(membersList) { email -> addMember(email) }
        membersRecyclerView.adapter = membersAdapter

        selectedGroup = intent.getSerializableExtra("group") as Group

        addButton.setOnClickListener {
            val email = searchInput.text.toString().trim()
            if (isValidEmail(email)) {
                if (!membersList.contains(email)) {
                    membersList.add(email)
                    membersAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "Member already added to the list", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            }
        }

        continueButton.setOnClickListener {
            navigateToGroupDetails() // Navigate to GroupDetailsActivity
        }
    }

    private fun addMember(email: String) {
        if (!selectedGroup.members.contains(email)) {
            selectedGroup.members.add(email)
            firestore.collection("groups").document(selectedGroup.id)
                .update("members", FieldValue.arrayUnion(email))
                .addOnSuccessListener {
                    Toast.makeText(this, "Member added to the group!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding member", e)
                }
        } else {
            Toast.makeText(this, "Member already in the group", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToGroupDetails() {
        val intent = Intent(this, GroupDetailsActivity::class.java)
        intent.putExtra("GROUP_ID", selectedGroup.id) // Pass the group ID
        startActivity(intent)
        finish() // Close the current activity
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
