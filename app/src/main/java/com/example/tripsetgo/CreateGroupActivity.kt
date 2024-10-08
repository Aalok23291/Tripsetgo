package com.example.tripsetgo

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateGroupActivity : AppCompatActivity() {

    private lateinit var groupNameInput: EditText
    private lateinit var createGroupButton: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        // Initialize views
        groupNameInput = findViewById(R.id.groupNameInput)
        createGroupButton = findViewById(R.id.doneButton)

        // Initialize Firestore and FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Set click listener for the create group button
        createGroupButton.setOnClickListener {
            createGroup()
        }
    }

    private fun createGroup() {
        val groupName = groupNameInput.text.toString().trim()

        if (groupName.isNotEmpty()) {
            val currentUserEmail = auth.currentUser?.email ?: ""

            // Ensure email is not empty
            if (currentUserEmail.isNotEmpty()) {
                val group = hashMapOf(
                    "name" to groupName,
                    "members" to arrayListOf(currentUserEmail) // Add the creator's email as the first member
                )

                firestore.collection("groups").add(group)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(this, "Group '$groupName' created successfully!", Toast.LENGTH_SHORT).show()

                        // Create a Group object to pass to AddMembersActivity
                        val newGroup = Group(documentReference.id, groupName, arrayListOf(currentUserEmail))
                        val intent = Intent(this, AddMembersActivity::class.java)
                        intent.putExtra("group", newGroup) // Pass the new group object
                        startActivity(intent)
                        finish() // Close the current activity
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error creating group: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "User not logged in. Please log in to create a group.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }
}
