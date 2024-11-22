package com.example.tripsetgo

import Expense
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ExpenseListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var expensesAdapter: ExpensesAdapter
    private val db = FirebaseFirestore.getInstance()
    private val expenses = mutableListOf<Expense>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses_list) // Ensure you create this layout

        recyclerView = findViewById(R.id.expenses_recycler_view) // Initialize RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load expenses for the group
        val groupId = intent.getStringExtra("GROUP_ID") ?: ""
        loadExpenses(groupId)
    }

    private fun loadExpenses(groupId: String) {
        db.collection("expenses")
            .whereEqualTo("groupId", groupId) // Assuming you save groupId in Expense
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val expense = document.toObject(Expense::class.java)
                    expenses.add(expense)
                }
                expensesAdapter = ExpensesAdapter(expenses)
                recyclerView.adapter = expensesAdapter // Set adapter to RecyclerView
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading expenses: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
