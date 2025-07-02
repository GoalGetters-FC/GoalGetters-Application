package com.ggetters.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R

class OnboardingAdapter : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {
    // TODO: Provide onboarding data (images, titles, descriptions)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding, parent, false)
        return OnboardingViewHolder(view)
    }
    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        // TODO: Bind onboarding data to views
    }
    override fun getItemCount(): Int = 3 // TODO: Set to actual number of onboarding screens
    class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
} 