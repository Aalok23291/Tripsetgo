package com.example.tripsetgo

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FirebaseRepository {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val storage = Firebase.storage.reference

    // Capsule Operations
    fun createCapsule(
        name: String,
        onSuccess: (PhotoCapsule) -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.currentUser?.let { user ->
            val capsule = PhotoCapsule(
                id = UUID.randomUUID().toString(),
                name = name,
                ownerId = user.uid
            )

            db.collection("capsules")
                .document(capsule.id)
                .set(capsule)
                .addOnSuccessListener { onSuccess(capsule) }
                .addOnFailureListener { onError(it) }
        }
    }

    fun getCapsule(
        capsuleId: String,
        onComplete: (PhotoCapsule) -> Unit
    ) {
        db.collection("capsules")
            .document(capsuleId)
            .get()
            .addOnSuccessListener { document ->
                document.toObject(PhotoCapsule::class.java)?.let { capsule ->
                    onComplete(capsule)
                }
            }
    }

    // For getting all capsules with real-time updates
    fun getCapsules(
        onSuccess: (List<PhotoCapsule>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection("capsules")
            .whereEqualTo("ownerId", auth.currentUser?.uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }

                val capsules = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(PhotoCapsule::class.java)
                } ?: emptyList()
                onSuccess(capsules)
            }
    }


    fun lockCapsule(
        capsuleId: String,
        unlockTime: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (unlockTime <= System.currentTimeMillis()) {
            onError(Exception("Cannot set unlock time in the past"))
            return
        }

        val updates = mapOf<String, Any>(
            "isLocked" to true,
            "unlockTime" to unlockTime
        )

        db.collection("capsules")
            .document(capsuleId)
            .update(updates)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    // Photo Operations
    fun uploadPhoto(
        uri: Uri,
        capsuleId: String,
        onProgress: (Int) -> Unit,
        onSuccess: (Photo) -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.currentUser?.let { user ->
            val photoId = UUID.randomUUID().toString()
            val photoRef = storage.child("photos/${user.uid}/$photoId.jpg")

            photoRef.putFile(uri)
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                    onProgress(progress)
                }
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { downloadUrl ->
                        val photo = Photo(
                            id = photoId,
                            capsuleId = capsuleId,
                            url = downloadUrl.toString(),
                            ownerId = user.uid
                        )
                        savePhotoReference(photo, onSuccess, onError)
                    }
                }
                .addOnFailureListener { onError(it) }
        }
    }

    private fun savePhotoReference(
        photo: Photo,
        onSuccess: (Photo) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("photos")
            .document(photo.id)
            .set(photo)
            .addOnSuccessListener {
                updateCapsulePhotoCount(photo.capsuleId)
                onSuccess(photo)
            }
            .addOnFailureListener { onError(it) }
    }

    private fun updateCapsulePhotoCount(capsuleId: String) {
        db.collection("capsules")
            .document(capsuleId)
            .update("photoCount", FieldValue.increment(1))
    }

    fun getPhotosForCapsule(
        capsuleId: String,
        onSuccess: (List<Photo>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        // Check lock status first
        getCapsule(capsuleId) { capsule ->
            if (capsule.isLocked && !capsule.isUnlockTimeReached()) {
                onError(Exception("Capsule is locked"))
                return@getCapsule
            }

            // If not locked, proceed with getting photos
            db.collection("photos")
                .whereEqualTo("capsuleId", capsuleId)
                .get()
                .addOnSuccessListener { documents ->
                    val photos = documents.mapNotNull { it.toObject(Photo::class.java) }
                    onSuccess(photos)
                }
                .addOnFailureListener { e ->
                    onError(e)
                }
        }
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            .format(Date(timestamp))
    }
}