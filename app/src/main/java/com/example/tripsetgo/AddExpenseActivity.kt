package com.example.tripsetgo

import Expense
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var expenseNameEditText: EditText
    private lateinit var expenseDescriptionEditText: EditText
    private lateinit var expenseAmountEditText: EditText
    private lateinit var paidBySpinner: Spinner
    private lateinit var splitExpenseCheckbox: CheckBox
    private lateinit var splitBetweenButton: Button
    private lateinit var addExpenseButton: Button
    private val db = FirebaseFirestore.getInstance() // Initialize Firestore

    private val selectedMembers = mutableListOf<String>() // List to store selected members
    private lateinit var members: List<String> // List of group members

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        // Initialize views
        expenseNameEditText = findViewById(R.id.expense_name)
        expenseDescriptionEditText = findViewById(R.id.expense_description)
        expenseAmountEditText = findViewById(R.id.expense_amount)
        paidBySpinner = findViewById(R.id.paid_by_spinner)
        splitExpenseCheckbox = findViewById(R.id.split_expense_checkbox)
        splitBetweenButton = findViewById(R.id.split_between_button)
        addExpenseButton = findViewById(R.id.add_expense_button)

        // Retrieve group ID from intent (replace with your method of getting the group ID)
        val groupId = intent.getStringExtra("GROUP_ID") ?: ""

        // Load group members from Firestore
        loadGroupMembers(groupId)

        // Set listener for the split expense checkbox
        splitExpenseCheckbox.setOnCheckedChangeListener { _, isChecked ->
            splitBetweenButton.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Set click listener for the Select Members button
        splitBetweenButton.setOnClickListener {
            showMemberSelectionDialog()
        }

        // Set click listener for the Add Expense button
        addExpenseButton.setOnClickListener {
            addExpense()
        }
    }

    private fun loadGroupMembers(groupId: String) {
        db.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Ensure that the members field is of type List<String>
                    val membersList = document.get("members") as? List<String>
                    if (membersList != null) {
                        members = membersList
                        setupPaidBySpinner(members) // Set up spinner with fetched members
                    } else {
                        Toast.makeText(this, "No members found in this group.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Group not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading members: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun setupPaidBySpinner(members: List<String>) {
        val paidByAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, members)
        paidByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paidBySpinner.adapter = paidByAdapter
    }

    private fun showMemberSelectionDialog() {
        val selectedItems = BooleanArray(members.size) // For checkbox selection
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Members to Split Expense")

        // Set the items for the dialog
        builder.setMultiChoiceItems(members.toTypedArray(), selectedItems) { _, which, isChecked ->
            if (isChecked) {
                selectedMembers.add(members[which]) // Add user to selected members
            } else {
                selectedMembers.remove(members[which]) // Remove user from selected members
            }
        }

        // Add positive button to confirm selection
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            // Show selected members for confirmation (optional)
            Toast.makeText(this, "Selected Members: $selectedMembers", Toast.LENGTH_SHORT).show()
        }

        // Add negative button to cancel
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }

    private fun addExpense() {
        val name = expenseNameEditText.text.toString().trim()
        val description = expenseDescriptionEditText.text.toString().trim()
        val amountString = expenseAmountEditText.text.toString().trim()

        if (name.isEmpty() || description.isEmpty() || amountString.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountString.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val paidBy = paidBySpinner.selectedItem.toString()
        val split = splitExpenseCheckbox.isChecked

        // Create an Expense object
        val expense = Expense(
            id = System.currentTimeMillis().toString(),
            description = description,
            amount = amount,
            creator = paidBy,
            split = split,
            participants = if (split) selectedMembers else emptyList()
        )

        // Save the expense to Firestore
        db.collection("expenses")
            .add(expense)
            .addOnSuccessListener { documentReference ->
                // After adding the expense, start the ExpenseDetailActivity
                val intent = Intent(this, ExpenseDetailActivity::class.java).apply {
                    putExtra("expenseId", documentReference.id) // Pass the ID of the added expense
                    putExtra("expenseName", name)
                    putExtra("expenseDescription", description)
                    putExtra("expenseAmount", amount)
                    putExtra("paidBy", paidBy)
                    putExtra("split", split)
                    putStringArrayListExtra("participants", ArrayList(expense.participants))
                }
                startActivity(intent) // Start the detail activity
                finish() // Optionally close this activity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding expense: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
