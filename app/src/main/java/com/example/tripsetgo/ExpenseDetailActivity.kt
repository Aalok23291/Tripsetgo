package com.example.tripsetgo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ExpenseDetailActivity : AppCompatActivity() {

    private lateinit var expenseNameTextView: TextView
    private lateinit var expenseDescriptionTextView: TextView
    private lateinit var expenseAmountTextView: TextView
    private lateinit var paidByTextView: TextView
    private lateinit var splitTextView: TextView
    private lateinit var participantsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_detail) // Make sure to create this layout

        // Initialize views
        expenseNameTextView = findViewById(R.id.expense_name_text_view)
        expenseDescriptionTextView = findViewById(R.id.expense_description_text_view)
        expenseAmountTextView = findViewById(R.id.expense_amount_text_view)
        paidByTextView = findViewById(R.id.paid_by_text_view)
        splitTextView = findViewById(R.id.split_text_view)
        participantsTextView = findViewById(R.id.participants_text_view)

        // Get data from Intent
        val expenseName = intent.getStringExtra("expenseName")
        val expenseDescription = intent.getStringExtra("expenseDescription")
        val expenseAmount = intent.getDoubleExtra("expenseAmount", 0.0)
        val paidBy = intent.getStringExtra("paidBy")
        val split = intent.getBooleanExtra("split", false)
        val participants = intent.getStringArrayListExtra("participants") ?: ArrayList()

        // Set data to views
        expenseNameTextView.text = expenseName
        expenseDescriptionTextView.text = expenseDescription
        expenseAmountTextView.text = "$$expenseAmount" // Format as needed
        paidByTextView.text = paidBy
        splitTextView.text = if (split) "Yes" else "No"
        participantsTextView.text = participants.joinToString(", ") // Display participants
    }
}