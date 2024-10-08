package com.example.tripsetgo

import Expense
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ExpenseActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var addGroupMembersButton: TextView
    private lateinit var addExpenseButton: Button
    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var noExpensesText: TextView
    private lateinit var expenseTitle: TextView
    private lateinit var expenseDescription: TextView
    private lateinit var amountOwed: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var expensesAdapter: ExpensesAdapter
    private val expensesList = mutableListOf<Expense>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        backButton = findViewById(R.id.backButton)
        addGroupMembersButton = findViewById(R.id.addGroupMembersButton)
        addExpenseButton = findViewById(R.id.addExpenseButton)
        expensesRecyclerView = findViewById(R.id.expensesRecyclerView)
        noExpensesText = findViewById(R.id.noExpensesText)
        expenseTitle = findViewById(R.id.expenseTitle)
        expenseDescription = findViewById(R.id.expenseDescription)
        amountOwed = findViewById(R.id.amountOwed)

        // Set up RecyclerView
        expensesRecyclerView.layoutManager = LinearLayoutManager(this)
        expensesAdapter = ExpensesAdapter(expensesList) // Make sure you implement this adapter
        expensesRecyclerView.adapter = expensesAdapter

        // Load expenses
        loadExpenses()

        // Set click listeners
        backButton.setOnClickListener { finish() }
        addGroupMembersButton.setOnClickListener {
            val intent = Intent(this, AddMembersActivity::class.java)
            startActivity(intent)
        }

        addExpenseButton.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadExpenses() {
        val groupId = intent.getStringExtra("GROUP_ID") ?: return

        firestore.collection("groups").document(groupId)
            .collection("expenses")
            .get()
            .addOnSuccessListener { documents ->
                expensesList.clear()
                var totalAmountOwed = 0.0
                for (document in documents) {
                    val expense = document.toObject(Expense::class.java)
                    expensesList.add(expense)
                    totalAmountOwed += calculateAmountOwed(expense)
                }
                expensesAdapter.notifyDataSetChanged()
                toggleNoExpensesText()

                // Display details of the last added expense
                if (expensesList.isNotEmpty()) {
                    displayExpenseDetails(expensesList.last()) // Display the last expense details
                }

                amountOwed.text = "You owe: $${totalAmountOwed}"
            }
            .addOnFailureListener { exception ->
                // Handle the error
                Toast.makeText(this, "Error loading expenses: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun calculateAmountOwed(expense: Expense): Double {
        // Logic to calculate how much the user owes
        return expense.amount / expense.participants.size // Example logic
    }

    private fun displayExpenseDetails(expense: Expense) {
        expenseTitle.text = expense.title
        expenseDescription.text = expense.description
    }

    private fun toggleNoExpensesText() {
        if (expensesList.isEmpty()) {
            noExpensesText.visibility = TextView.VISIBLE
            expensesRecyclerView.visibility = RecyclerView.GONE
        } else {
            noExpensesText.visibility = TextView.GONE
            expensesRecyclerView.visibility = RecyclerView.VISIBLE
        }
    }
}
