package com.ggetters.app.ui.central.sheets

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ggetters.app.R
import com.ggetters.app.databinding.BottomSheetContactDevelopersBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ContactDevelopersBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetContactDevelopersBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetContactDevelopersBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnClose.setOnClickListener { dismiss() }
        
        binding.btnEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:developers@goalgetters.com")
                putExtra(Intent.EXTRA_SUBJECT, "Goal Getters FC - User Feedback")
            }
            startActivity(Intent.createChooser(intent, "Send Email"))
        }
        
        binding.btnWebsite.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://goalgetters.com"))
            startActivity(intent)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

