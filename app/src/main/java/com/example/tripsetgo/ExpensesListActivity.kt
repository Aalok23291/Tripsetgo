package com.example.tripsetgo

import Expense
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ExpensesListActivity : AppCompatActivity() {

    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var expensesAdapter: ExpensesAdapter
    private val db = FirebaseFirestore.getInstance()
    private lateinit var expenses: List<Expense> // List to store expenses
    private lateinit var groupId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses_list)

        expensesRecyclerView = findViewById(R.id.expenses_recycler_view)
        expensesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Get the group ID from intent
        groupId = intent.getStringExtra("GROUP_ID") ?: ""

        // Load expenses for the group
        loadExpenses()
    }

    private fun loadExpenses() {
        db.collection("expenses")
            .whereEqualTo("groupId", groupId) // Filter by group ID
            .get()
            .addOnSuccessListener { result ->
                expenses = result.toObjects(Expense::class.java)
                expensesAdapter = ExpensesAdapter(expenses) // Initialize the adapter
                expensesRecyclerView.adapter = expensesAdapter // Set the adapter to RecyclerView
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading expenses: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
