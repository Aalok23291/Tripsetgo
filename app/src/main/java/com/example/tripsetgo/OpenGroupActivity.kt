package com.example.tripsetgo

import Expense
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class OpenGroupActivity : AppCompatActivity() {

    private lateinit var groupNameText: TextView
    private lateinit var addExpenseButton: Button
    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var expensesAdapter: ExpensesAdapter
    private val expensesList = mutableListOf<Expense>()
    private lateinit var selectedGroup: Group
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_group)

        firestore = FirebaseFirestore.getInstance()
        groupNameText = findViewById(R.id.groupNameText)
        addExpenseButton = findViewById(R.id.addExpenseButton)
        expensesRecyclerView = findViewById(R.id.expensesRecyclerView)

        // Retrieve the selected group from the intent
        selectedGroup = intent.getSerializableExtra("group") as Group
        groupNameText.text = selectedGroup.name

        expensesRecyclerView.layoutManager = LinearLayoutManager(this)
        expensesAdapter = ExpensesAdapter(expensesList)
        expensesRecyclerView.adapter = expensesAdapter

        loadExpenses()

        addExpenseButton.setOnClickListener {
            // Implement logic to add an expense, e.g., open a new Activity or Dialog
        }
    }

    private fun loadExpenses() {
        firestore.collection("groups").document(selectedGroup.id)
            .collection("expenses")
            .get()
            .addOnSuccessListener { documents ->
                expensesList.clear()
                for (document in documents) {
                    val expense = document.toObject(Expense::class.java)
                    expensesList.add(expense)
                }
                expensesAdapter.notifyDataSetChanged()

                // Optionally, handle empty expenses list
                if (expensesList.isEmpty()) {
                    // Show a placeholder message or update the UI accordingly
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error loading expenses", e)
                // Optionally, show an error message to the user
            }
    }
}
