package com.example.tripsetgo

import Expense
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpensesAdapter(private val expenses: List<Expense>) : RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)
        val amountText: TextView = itemView.findViewById(R.id.amountText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.descriptionText.text = expense.description
        holder.amountText.text = String.format("%.2f", expense.amount)
        Log.d("ExpensesAdapter", "Binding expense: ${expense.description}, Amount: ${expense.amount}")


        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ExpenseDetailActivity::class.java).apply {
                putExtra("expenseId", expense.id)
                putExtra("expenseTitle", expense.title)
                putExtra("expenseDescription", expense.description)
                putExtra("expenseAmount", expense.amount)
                putExtra("paidBy", expense.creator)
                putStringArrayListExtra("participants", ArrayList(expense.participants))
            }
            context.startActivity(intent)
        }
    }


    override fun getItemCount() = expenses.size
}
