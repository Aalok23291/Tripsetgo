package com.example.tripsetgo

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CreateGroupAndAddMembersActivity : AppCompatActivity() {

    private lateinit var groupNameInput: EditText
    private lateinit var createGroupButton: Button
    private lateinit var addMembersSection: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var addButton: Button
    private lateinit var continueButton: Button
    private lateinit var membersRecyclerView: RecyclerView
    private lateinit var membersAdapter: MembersAdapter
    private val membersList = mutableListOf<String>()
    private lateinit var selectedGroup: Group
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group_and_add_members)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize views
        groupNameInput = findViewById(R.id.groupNameInput)
        createGroupButton = findViewById(R.id.doneButton)
        addMembersSection = findViewById(R.id.addMembersSection)
        searchInput = findViewById(R.id.searchInput)
        addButton = findViewById(R.id.addButton)
        continueButton = findViewById(R.id.continueButton)
        membersRecyclerView = findViewById(R.id.membersRecyclerView)

        membersRecyclerView.layoutManager = LinearLayoutManager(this)
        membersAdapter = MembersAdapter(membersList) { email -> addMember(email) }
        membersRecyclerView.adapter = membersAdapter

        // Set click listener for create group
        createGroupButton.setOnClickListener {
            createGroup()
        }

        // Set click listener for adding members
        addButton.setOnClickListener {
            val email = searchInput.text.toString().trim()
            if (isValidEmail(email)) {
                if (!membersList.contains(email)) {
                    // Show loading indicator
                    val progressDialog = ProgressDialog(this).apply {
                        setMessage("Adding member...")
                        setCancelable(false)
                        show()
                    }

                    membersList.add(email)
                    membersAdapter.notifyDataSetChanged()

                    // Hide loading indicator
                    progressDialog.dismiss();
                } else {
                    showFeedback("Member already added to the list")
                }
            } else {
                showFeedback("Invalid email format")
            }
        }




        // Continue button click listener
        continueButton.setOnClickListener {
            navigateToGroupDetails()
        }
    }


    private fun showFeedback(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun createGroup() {
        val groupName = groupNameInput.text.toString().trim()

        if (groupName.isNotEmpty()) {
            val currentUserEmail = auth.currentUser?.email ?: ""

            if (currentUserEmail.isNotEmpty()) {
                val group = hashMapOf(
                    "name" to groupName,
                    "members" to arrayListOf(currentUserEmail) // Add creator as first member
                )

                firestore.collection("groups").add(group)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(this, "Group '$groupName' created successfully!", Toast.LENGTH_SHORT).show()

                        // Show Add Members section after group creation
                        addMembersSection.visibility = View.VISIBLE

                        selectedGroup = Group(documentReference.id, groupName, arrayListOf(currentUserEmail))
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
        intent.putExtra("GROUP_ID", selectedGroup.id)
        startActivity(intent)
        finish()
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
