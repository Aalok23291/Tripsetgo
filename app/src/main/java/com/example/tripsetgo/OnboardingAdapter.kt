package com.example.tripsetgo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsetgo.R

class OnboardingAdapter(private val images: List<Int>) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {
    private val layouts = listOf(
        R.layout.onboarding_page1,
        R.layout.onboarding_page2,
        R.layout.onboarding_page3,
        R.layout.onboarding_page4,
        R.layout.onboarding_page5,
        R.layout.onboarding_page6
    )

    inner class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layouts[viewType], parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.imageView.setImageResource(images[position])
    }

    override fun getItemCount(): Int {
        return images.size
    }
}
