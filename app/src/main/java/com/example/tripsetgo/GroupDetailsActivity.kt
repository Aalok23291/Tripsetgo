package com.example.tripsetgo

import Expense
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class GroupDetailsActivity : AppCompatActivity() {

    private lateinit var groupId: String
    private lateinit var groupNameTextView: TextView
    private lateinit var addExpenseButton: Button
    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var expensesAdapter: ExpensesAdapter
    private val expensesList = mutableListOf<Expense>()
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_details)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Get the group ID from the intent
        groupId = intent.getStringExtra("GROUP_ID") ?: ""

        // Log the groupId to see what value is being passed
        Log.d("GroupDetailsActivity", "Group ID: $groupId")

        // Check if groupId is valid
        if (groupId.isEmpty()) {
            Toast.makeText(this, "Invalid group ID", Toast.LENGTH_SHORT).show()
            finish() // Close the activity if the group ID is invalid
            return
        }

        // Initialize views
        groupNameTextView = findViewById(R.id.groupNameTextView)
        addExpenseButton = findViewById(R.id.addExpenseButton)
        expensesRecyclerView = findViewById(R.id.expensesRecyclerView)
        expensesRecyclerView.layoutManager = LinearLayoutManager(this)
        expensesAdapter = ExpensesAdapter(expensesList) // Implement ExpenseAdapter to bind expenses
        expensesRecyclerView.adapter = expensesAdapter

        // Load group details
        loadGroupDetails()

        // Load existing expenses
        loadExpenses()

        // Set up button click listener
        addExpenseButton.setOnClickListener {
            // Navigate to AddExpenseActivity
            val intent = Intent(this, AddExpenseActivity::class.java)
            intent.putExtra("GROUP_ID", groupId) // Pass the group ID
            startActivity(intent)
        }
    }

    private fun loadGroupDetails() {
        // Access the document in the 'groups' collection using the groupId
        firestore.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Fetch groupname and display it
                    val groupName = document.getString("name") // Ensure the field is "groupname"
                    groupNameTextView.text = groupName ?: "No group name"

                    // You can also retrieve the list of members here if needed
                    val members = document.get("members") as? List<String>
                    // Use 'members' if needed
                } else {
                    Toast.makeText(this, "No such group", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting group details: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadExpenses() {
        firestore.collection("groups").document(groupId)
            .collection("expenses")
            .get()
            .addOnSuccessListener { documents ->
                expensesList.clear() // Clear the list to avoid duplication
                for (document in documents) {
                    val expense = document.toObject(Expense::class.java)
                    expensesList.add(expense)
                }
                expensesAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting expenses: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
