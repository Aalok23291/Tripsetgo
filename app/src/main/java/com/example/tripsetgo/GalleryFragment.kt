package com.example.tripsetgo

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.google.firebase.firestore.ListenerRegistration
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.tripsetgo.databinding.FragmentGalleryBinding
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import java.util.UUID
import androidx.core.os.bundleOf
import androidx.navigation.findNavController

class GalleryFragment : Fragment() {
    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private val firebaseRepository = FirebaseRepository()
    private lateinit var capsuleAdapter: CapsuleAdapter
    private var listenerRegistration: ListenerRegistration? = null // Add this

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        loadCapsules()
    }

    private fun setupViews() {
        if (!isAdded || _binding == null) return
        setupCapsuleAdapter()
        setupClickListeners()
    }

    private fun setupCapsuleAdapter() {
        capsuleAdapter = CapsuleAdapter { capsule ->
            if (!capsule.isLocked || capsule.isUnlockTimeReached()) {
                navigateToCapsuleContent(capsule)
            } else {
                showLockedMessage(capsule)
            }
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = capsuleAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            showNewCapsuleDialog()
        }
    }

    private fun navigateToCapsuleContent(capsule: PhotoCapsule) {
        // Check lock status first
        if (capsule.isLocked) {
            // If still locked, show message and prevent navigation
            if (!capsule.isUnlockTimeReached()) {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                val unlockDate = dateFormat.format(Date(capsule.unlockTime))
                showMessage("🔒 Capsule is locked until $unlockDate")
                return  // Don't navigate if locked
            }
        }

        try {
            val capsuleContentFragment = CapsuleContentFragment().apply {
                arguments = Bundle().apply {
                    putString("capsuleId", capsule.id)
                    putString("capsuleName", capsule.name)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, capsuleContentFragment)
                .addToBackStack(null)
                .commit()
        } catch (e: Exception) {
            Log.e("Navigation", "Error navigating: ${e.message}")
            showError("Navigation error")
        }
    }

    private fun showNewCapsuleDialog() {
        if (!isAdded) return

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_new_capsule, null)
        val input = dialogView.findViewById<TextInputEditText>(R.id.capsuleNameInput)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create New Capsule")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val capsuleName = input.text.toString()
                if (capsuleName.isNotEmpty()) {
                    createNewCapsule(capsuleName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createNewCapsule(name: String) {
        if (!isAdded) return

        firebaseRepository.createCapsule(
            name = name,
            onSuccess = {
                if (_binding != null && isAdded) {
                    showMessage("Capsule created successfully")
                    loadCapsules() // Reload capsules after creation
                }
            },
            onError = {
                if (_binding != null && isAdded) {
                    showError("Failed to create capsule")
                }
            }
        )
    }

    private fun loadCapsules() {
        if (!isAdded || _binding == null) return

        binding.progressBar.visibility = View.VISIBLE

        // Remove previous listener if exists
        listenerRegistration?.remove()

        listenerRegistration = firebaseRepository.getCapsules(
            onSuccess = { capsules ->
                if (_binding != null && isAdded) {
                    binding.progressBar.visibility = View.GONE
                    capsuleAdapter.updateCapsules(capsules)
                    toggleEmptyState(capsules.isEmpty())
                }
            },
            onError = { exception ->
                if (_binding != null && isAdded) {
                    binding.progressBar.visibility = View.GONE
                    showError("Failed to load capsules")
                }
            }
        )
    }
    private fun toggleEmptyState(isEmpty: Boolean) {
        if (!isAdded || _binding == null) return

        binding.apply {
            emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
            recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    private fun showLockedMessage(capsule: PhotoCapsule) {
        if (!isAdded) return

        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val unlockDate = dateFormat.format(Date(capsule.unlockTime))
        showMessage("This capsule is locked until $unlockDate")
    }

    private fun showError(message: String) {
        if (isAdded) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMessage(message: String) {
        if (isAdded) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove() // Remove the listener
        _binding = null
    }

    override fun onDestroy() {
        listenerRegistration?.remove() // Safety check
        super.onDestroy()
    }
}