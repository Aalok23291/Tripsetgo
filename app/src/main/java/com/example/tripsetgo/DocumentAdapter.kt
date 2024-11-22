package com.example.tripsetgo

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DocumentAdapter(
    private val documents: List<Document>,
    private val onQrButtonClicked: (String) -> Unit,  // Callback for QR code button
    private val onMoreOptionsClicked: (Document) -> Unit,
    private val onDocumentClick: (String) -> Unit

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

        // Set the document name
        holder.documentNameView.text = document.name

        // Handle QR button click
        holder.qrButton.setOnClickListener {
            onQrButtonClicked(document.qrCode) // Pass the QR code instead of the document name
        }
        holder.itemView.setOnClickListener {
            Log.d("DocumentAdapter", "Document clicked: ${document.url}")
            onDocumentClick(document.url) // Use the document URL to open the file
        }

        // Handle more options button click
        holder.moreOptionsButton.setOnClickListener {
            onMoreOptionsClicked(document) // Pass the document object to handle options
        }
    }

    override fun getItemCount(): Int {
        return documents.size
    }
}
