package com.example.tripsetgo

import android.text.format.DateUtils.formatDateTime
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsetgo.databinding.FolderItemBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CapsuleAdapter(
    private val onCapsuleClick: (PhotoCapsule) -> Unit
) : RecyclerView.Adapter<CapsuleAdapter.CapsuleViewHolder>() {

    private var capsules = listOf<PhotoCapsule>()
    private fun formatDateTime(timestamp: Long): String {
        return SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            .format(Date(timestamp))
    }
    inner class CapsuleViewHolder(
        private val binding: FolderItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(capsule: PhotoCapsule) {
            binding.apply {
                folderName.text = capsule.name

                // Show lock status and remaining time
                if (capsule.isLocked && !capsule.isUnlockTimeReached()) {
                    lockStatus.visibility = View.VISIBLE
                    lockStatus.text = capsule.getRemainingTime()
                    lockIcon.setImageResource(R.drawable.lock)
                } else {
                    lockStatus.visibility = View.GONE
                    lockIcon.setImageResource(R.drawable.unlock)
                }

                root.setOnClickListener {
                    onCapsuleClick(capsule)
                }
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CapsuleViewHolder {
        return CapsuleViewHolder(
            FolderItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CapsuleViewHolder, position: Int) {
        holder.bind(capsules[position])
    }

    override fun getItemCount() = capsules.size

    fun updateCapsules(newCapsules: List<PhotoCapsule>) {
        capsules = newCapsules
        notifyDataSetChanged()
    }
}