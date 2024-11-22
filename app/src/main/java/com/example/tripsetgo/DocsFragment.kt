package com.example.tripsetgo

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream

class DocsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var documentAdapter: DocumentAdapter
    private lateinit var fabUploadDocument: FloatingActionButton
    private var imageUri: Uri? = null
    private var selectedFileUri: Uri? = null

    private lateinit var activityResultLauncher: ActivityResultLauncher<String>
    private lateinit var documentName: String
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_docs, container, false)
        progressBar = view.findViewById(R.id.progressBar)

        recyclerView = view.findViewById(R.id.recyclerViewDocuments)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        fabUploadDocument = view.findViewById(R.id.fabUploadDocument)

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedFileUri = it
                openFileNameDialog() // Prompt for document name after file selection
            }
        }

        fabUploadDocument.setOnClickListener { openFileChooser() }

        loadDocuments()

        return view
    }

    private fun openFileChooser() {
        activityResultLauncher.launch("application/pdf") // Limiting to PDF file types
    }

    private fun openFileNameDialog() {
        // Show a dialog to set the document name
        val builder = AlertDialog.Builder(requireContext())
        val input = EditText(requireContext())
        builder.setView(input)

        builder.setTitle("Set Document Name")
        builder.setPositiveButton("OK") { _, _ ->
            documentName = input.text.toString() // Capture the document name
            if (documentName.isNotBlank() && selectedFileUri != null) {
                uploadFileWithName(selectedFileUri!!, documentName)
            } else {
                Toast.makeText(requireContext(), "Please provide a valid name and file!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun uploadFileWithName(fileUri: Uri, documentName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("documents/$documentName.pdf")

        progressBar.visibility = View.VISIBLE

        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val qrCodeBitmap = generateQRCode(downloadUri.toString())
                    uploadQRCodeToStorage(qrCodeBitmap, userId, documentName, downloadUri.toString())
                }
            }
            .addOnCompleteListener {
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to upload document", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadDocuments() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            FirebaseFirestore.getInstance().collection("users").document(it)
                .collection("documents").get().addOnSuccessListener { documents ->
                    val documentList = documents.map { doc ->
                        Document(
                            name = doc.getString("name") ?: "Unnamed",
                            url = doc.getString("url") ?: "",
                            qrCode = doc.getString("qrCode") ?: ""
                        )
                    }
                    documentAdapter = DocumentAdapter(documentList, { qrCode ->
                        showQRCode(qrCode) // Show QR code when QR button is clicked
                    }, { document ->
                        showMoreOptionsDialog(document) // Show options when more button is clicked
                    }, { documentUrl ->
                        openDocument(documentUrl) // Open document when clicked
                    })
                    recyclerView.adapter = documentAdapter
                }
        }
    }
    private fun openDocument(url: String) {
        Log.d("DocsFragment", "Opening document URL: $url")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(url), "application/pdf") // Ensure the MIME type is correct
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        // Check if there is an app available to open the document
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "No application available to open this document", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showMoreOptionsDialog(document: Document) {
        val options = arrayOf("Delete Document")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("More Options")
            .setItems(options) { _, which ->
                if (which == 0) {
                    deleteDocument(document)
                }
            }
        builder.show()
    }

    private fun deleteDocument(document: Document) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .collection("documents").whereEqualTo("name", document.name).get()
            .addOnSuccessListener { querySnapshot ->
                for (doc in querySnapshot.documents) {
                    doc.reference.delete().addOnSuccessListener {
                        Toast.makeText(requireContext(), "Document deleted", Toast.LENGTH_SHORT).show()
                        loadDocuments() // Reload documents after deletion
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to delete document", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun uploadQRCodeToStorage(qrCodeBitmap: Bitmap, userId: String, fileName: String, documentUrl: String) {
        val qrCodeRef = FirebaseStorage.getInstance().reference.child("qrCodes/$fileName.png")

        val byteArrayOutputStream = ByteArrayOutputStream()
        qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()

        qrCodeRef.putBytes(data).addOnSuccessListener {
            qrCodeRef.downloadUrl.addOnSuccessListener { qrCodeUrl ->
                saveDocumentToFirestore(userId, fileName, documentUrl, qrCodeUrl.toString())
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to retrieve QR code URL", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to upload QR code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDocumentToFirestore(userId: String, name: String, url: String, qrCode: String) {
        val documentData = hashMapOf(
            "name" to name,
            "url" to url,
            "qrCode" to qrCode
        )

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .collection("documents").add(documentData)
            .addOnSuccessListener {
                loadDocuments()  // Reload documents to show the new one
                Toast.makeText(requireContext(), "Document saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save document", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateQRCode(text: String): Bitmap {
        val qrCodeWriter = QRCodeWriter()
        return try {
            val bitMatrix: BitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            bmp
        } catch (e: WriterException) {
            Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565) // Return empty bitmap on error
        }
    }

    private fun showQRCode(qrCode: String) {
        val qrCodeBitmap = generateQRCode(qrCode)

        val imageView = ImageView(requireContext())
        imageView.setImageBitmap(qrCodeBitmap)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(imageView)
        builder.setTitle("QR Code")
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}
