package com.example.tripsetgo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class DocumentAdapter(
    private val documents: List<Document>,
    private val onQrButtonClicked: (String) -> Unit,  // Callback for QR code button
    private val onMoreOptionsClicked: (Document) -> Unit // Callback for more options button
) : RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder>() {

    class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val documentNameView: TextView = itemView.findViewById(R.id.textViewDocumentName)
        val qrButton: ImageButton = itemView.findViewById(R.id.imageViewQRCode)
        val moreOptionsButton: ImageButton = itemView.findViewById(R.id.imageButtonMoreOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.document_item, parent, false)
        return DocumentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val document = documents[position]

        holder.documentNameView.text = document.name // Ensure this is correct
        holder.qrButton.setOnClickListener {
            onQrButtonClicked(document.name) // Pass the document name
        }
    }


    override fun getItemCount(): Int {
        return documents.size
    }
}