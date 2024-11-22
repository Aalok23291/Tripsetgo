package com.example.tripsetgo

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.KeyEvent
import android.view.Window
import android.widget.ImageView
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.tripsetgo.databinding.ActivityFolderContentBinding
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
class CapsuleContentFragment : Fragment() {
    private var _binding: ActivityFolderContentBinding? = null
    private val binding get() = _binding!!

    private lateinit var capsuleId: String
    private lateinit var capsuleName: String
    private val firebaseRepository = FirebaseRepository()
    private lateinit var photoAdapter: PhotoAdapter
    private var currentPhotoUri: Uri? = null

    // Storage Permission Launcher
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            launchGallery()
        } else {
            showPermissionDeniedMessage("Storage")
        }
    }

    // Camera Permission Launcher
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            showPermissionDeniedMessage("Camera")
        }
    }

    // Activity Result Launchers
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                uploadPhoto(uri)
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { uploadPhoto(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            capsuleId = it.getString("capsuleId", "")
            capsuleName = it.getString("capsuleName", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityFolderContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            parentFragmentManager.popBackStack()
        }
        checkLockStatusAndSetup()

        checkLockStatus()
        setupViews()
        loadPhotos()
    }
    private fun checkLockStatusAndSetup() {
        binding.progressBar.visibility = View.VISIBLE

        firebaseRepository.getCapsule(capsuleId) { capsule ->
            if (capsule.isLocked && !capsule.isUnlockTimeReached()) {
                // If capsule is locked, show message and go back
                val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                val unlockDate = dateFormat.format(Date(capsule.unlockTime))
                showMessage("🔒 This capsule is locked until $unlockDate")
                parentFragmentManager.popBackStack()
                return@getCapsule
            }

            // Only setup views and load photos if not locked
            binding.progressBar.visibility = View.GONE
            setupViews()
            loadPhotos()
        }
    }
    private fun checkLockStatus() {
        firebaseRepository.getCapsule(capsuleId) { capsule ->
            if (!capsule.isAccessible()) {
                showMessage(capsule.getRemainingTime())
                parentFragmentManager.popBackStack()
            } else {
                setupViews()
                loadPhotos()
            }
        }
    }
    private fun showLockedMessage(capsule: PhotoCapsule) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val unlockDate = dateFormat.format(Date(capsule.unlockTime))
        showMessage("This capsule is locked until $unlockDate")
    }

    private fun setupViews() {
        binding.folderContentHeader.text = capsuleName
        setupPhotoAdapter()
        setupClickListeners()
    }


    private fun setupPhotoAdapter() {
        photoAdapter = PhotoAdapter { photo ->
            showPhotoPreview(photo)
        }
        binding.photoRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = photoAdapter
        }
    }

    private fun setupClickListeners() {
        binding.capturePhoto.setOnClickListener {
            checkLockStatusAndProceed { checkCameraPermission() }
        }

        binding.btnUploadPhoto.setOnClickListener {
            checkLockStatusAndProceed { checkStoragePermission() }
        }

        binding.lockButton.setOnClickListener {
            showLockDialog()
        }
    }
    private fun checkLockStatusAndProceed(action: () -> Unit) {
        firebaseRepository.getCapsule(capsuleId) { capsule ->
            if (capsule.isLocked && !capsule.isUnlockTimeReached()) {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                val unlockDate = dateFormat.format(Date(capsule.unlockTime))
                showMessage("🔒 This capsule is locked until $unlockDate")
                parentFragmentManager.popBackStack()
            } else {
                action.invoke()
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showPermissionRationaleDialog(
                    Manifest.permission.CAMERA,
                    "Camera permission is required to take photos"
                )
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    private fun showPermissionRationaleDialog(permission: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permission Required")
            .setMessage(message)
            .setPositiveButton("Grant") { _, _ ->
                when (permission) {
                    Manifest.permission.CAMERA -> {
                        cameraPermissionLauncher.launch(permission)
                    }
                    Manifest.permission.READ_EXTERNAL_STORAGE -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            storagePermissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
                        } else {
                            storagePermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                            )
                        }
                    }
                    Manifest.permission.READ_MEDIA_IMAGES -> {
                        storagePermissionLauncher.launch(arrayOf(permission))
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED -> {
                    launchGallery()
                }
                else -> {
                    storagePermissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
                }
            }
        } else {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            when {
                permissions.all {
                    ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
                } -> {
                    launchGallery()
                }
                else -> {
                    storagePermissionLauncher.launch(permissions)
                }
            }
        }
    }

    private fun showPermissionDeniedMessage(permissionType: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permission Required")
            .setMessage("$permissionType permission is required for this feature. Please enable it in app settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
            startActivity(this)
        }
    }

    private fun launchCamera() {
        try {
            val photoFile = createImageFile()
            currentPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            takePictureLauncher.launch(currentPhotoUri)
        } catch (e: Exception) {
            showError("Error launching camera: ${e.message}")
        }
    }

    private fun launchGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun showPhotoPreview(photo: Photo) {
        try {
            // Create dialog with proper style
            val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            dialog.setContentView(R.layout.layout_photo_preview)

            // Initialize views
            val photoView = dialog.findViewById<PhotoView>(R.id.photoView)
            val closeButton = dialog.findViewById<ImageView>(R.id.closeButton)

            // Load image with Glide
            Glide.with(requireContext())
                .load(photo.url)
                .placeholder(R.drawable.loading_placeholder)  // Add a loading placeholder drawable
                .error(R.drawable.error_placeholder)         // Add an error placeholder drawable
                .into(photoView)

            // Set up close button
            closeButton?.setOnClickListener {
                dialog.dismiss()
            }

            // Show dialog
            dialog.show()
        } catch (e: Exception) {
            Log.e("PhotoPreview", "Error showing photo: ${e.message}", e)
            showError("Could not display photo")
        }
    }

    private fun uploadPhoto(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        firebaseRepository.uploadPhoto(
            uri = uri,
            capsuleId = capsuleId,
            onProgress = { progress ->
                // Update progress if needed
            },
            onSuccess = {
                if (isAdded && _binding != null) {  // Add these checks
                    binding.progressBar.visibility = View.GONE
                    showMessage("Photo uploaded successfully")
                    loadPhotos()
                }
            },
            onError = {
                if (isAdded && _binding != null) {  // Add these checks
                    binding.progressBar.visibility = View.GONE
                    showError("Failed to upload photo")
                }
            }
        )
    }

    private fun loadPhotos() {
        binding.progressBar.visibility = View.VISIBLE

        firebaseRepository.getPhotosForCapsule(
            capsuleId = capsuleId,
            onSuccess = { photos ->
                if (_binding != null && isAdded) {
                    binding.progressBar.visibility = View.GONE
                    photoAdapter.updatePhotos(photos)
                }
            },
            onError = { error ->
                if (_binding != null && isAdded) {
                    binding.progressBar.visibility = View.GONE
                    showError(error.message ?: "Failed to load photos")
                    parentFragmentManager.popBackStack()
                }
            }
        )
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        binding.photoRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showLockDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_time_lock)

        val datePicker = dialog.findViewById<DatePicker>(R.id.datePicker)
        val timePicker = dialog.findViewById<TimePicker>(R.id.timePicker)

        // Set minimum date to tomorrow
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        datePicker.minDate = tomorrow.timeInMillis

        dialog.findViewById<View>(R.id.confirmLockButton).setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(
                datePicker.year,
                datePicker.month,
                datePicker.dayOfMonth,
                timePicker.hour,
                timePicker.minute
            )

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                showError("Please select a future date and time")
                return@setOnClickListener
            }

            lockCapsule(calendar.timeInMillis)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun lockCapsule(unlockTime: Long) {
        if (unlockTime <= System.currentTimeMillis()) {
            showError("Please select a future date and time")
            return
        }

        firebaseRepository.lockCapsule(
            capsuleId = capsuleId,
            unlockTime = unlockTime,
            onSuccess = {
                showMessage("Capsule locked successfully")
                parentFragmentManager.popBackStack()
            },
            onError = {
                showError("Failed to lock capsule")
            }
        )
    }
    private fun updatePhotosWithAnimation(newPhotos: List<Photo>) {
        binding.photoRecyclerView.post {
            photoAdapter.updatePhotos(newPhotos)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}