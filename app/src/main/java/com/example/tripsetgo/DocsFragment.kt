package com.example.tripsetgo

import android.app.AlertDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
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

    private lateinit var activityResultLauncher: ActivityResultLauncher<String> // Corrected type to String
    private lateinit var documentName: String
    private lateinit var progressBar: ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_docs, container, false)
        progressBar = view.findViewById(R.id.progressBar)
        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewDocuments)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize FloatingActionButton
        fabUploadDocument = view.findViewById(R.id.fabUploadDocument)

        // Initialize ActivityResultLauncher for getting content (document)
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                uploadDocumentToFirebase() // Call your upload function
            }
        }

        fabUploadDocument.setOnClickListener { openFileChooser() }

        // Load documents from Firestore
        loadDocuments()


        return view
    }

    private fun openFileChooser() {
        // Launch the file chooser
        activityResultLauncher.launch("application/*")

        // Show a dialog to set the document name
        val builder = AlertDialog.Builder(requireContext())
        val input = EditText(requireContext())
        builder.setView(input)

        builder.setTitle("Set Document Name")
        builder.setPositiveButton("OK") { dialog, _ ->
            documentName = input.text.toString() // Capture the document name
            if (selectedFileUri != null) {
                uploadFileWithName(selectedFileUri, documentName)
            } else {
                Toast.makeText(requireContext(), "No file selected!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun uploadFileWithName(fileUri: Uri?, documentName: String) {
        if (fileUri != null) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val storageRef = FirebaseStorage.getInstance().reference.child("documents/$documentName.pdf")

            // Show progress bar during upload
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
        } else {
            Toast.makeText(requireContext(), "File URI is null!", Toast.LENGTH_SHORT).show()
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
                    })
                    recyclerView.adapter = documentAdapter
                }
        }
    }

    private fun showMoreOptionsDialog(document: Document) {
        val options = arrayOf("Delete Document")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("More Options")
            .setItems(options) { dialog, which ->
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


    private fun uploadDocumentToFirebase() {
        progressBar.visibility = View.VISIBLE
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val fileName = imageUri?.lastPathSegment ?: return

        val storageRef = FirebaseStorage.getInstance().reference.child("documents/$fileName")
        storageRef.putFile(imageUri!!).addOnSuccessListener { taskSnapshot ->
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                // Generate QR code and upload it to Firebase Storage
                val qrCodeBitmap = generateQRCode(downloadUri.toString())
                uploadQRCodeToStorage(qrCodeBitmap, userId, fileName, downloadUri.toString())
            }
        }.addOnCompleteListener {
            progressBar.visibility = View.GONE
        }
    }

    private fun uploadQRCodeToStorage(qrCodeBitmap: Bitmap, userId: String, fileName: String, documentUrl: String) {
        val qrCodeRef = FirebaseStorage.getInstance().reference.child("qrCodes/$fileName.png")

        // Convert bitmap to byte array
        val byteArrayOutputStream = ByteArrayOutputStream()
        qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()

        // Upload to Firebase Storage
        qrCodeRef.putBytes(data).addOnSuccessListener {
            // Retrieve the download URL of the QR code
            qrCodeRef.downloadUrl.addOnSuccessListener { qrCodeUrl ->
                // Save document information in Firestore, including the QR code URL
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
            "name" to name,   // Save the document name here
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

        if (qrCodeBitmap != null) {
            val imageView = ImageView(requireContext())
            imageView.setImageBitmap(qrCodeBitmap)

            val builder = AlertDialog.Builder(requireContext())
            builder.setView(imageView)
            builder.setTitle("QR Code")
            builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            builder.show()
        } else {
            Toast.makeText(requireContext(), "Failed to generate QR Code", Toast.LENGTH_SHORT).show()
        }
    }

}
