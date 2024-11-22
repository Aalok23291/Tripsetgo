package com.example.tripsetgo

import Expense
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class GroupExpensesActivity : AppCompatActivity() {

    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var expensesAdapter: ExpensesAdapter
    private val expenseList = mutableListOf<Expense>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_expenses)

        // Initialize RecyclerView and Adapter
        expensesRecyclerView = findViewById(R.id.expenses_recycler_view)

        // Initialize the adapter here
        expensesAdapter = ExpensesAdapter(expenseList)

        // Now set up the RecyclerView with layout manager and adapter
        expensesRecyclerView.layoutManager = LinearLayoutManager(this)
        expensesRecyclerView.adapter = expensesAdapter

        // Get the group ID passed through intent
        val groupId = intent.getStringExtra("groupId") ?: return

        // Fetch expenses for the specified group
        fetchGroupExpenses(groupId)
    }


    private fun fetchGroupExpenses(groupId: String) {
        db.collection("expenses")
            .whereEqualTo("groupId", groupId)
            .get()
            .addOnSuccessListener { documents ->
                expenseList.clear()  // Clear the list before adding new expenses
                for (document in documents) {
                    val expense = document.toObject(Expense::class.java)
                    expenseList.add(expense)  // Add each expense to the list
                }

                expensesAdapter.notifyDataSetChanged()  // Notify RecyclerView adapter to update UI
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching expenses: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
