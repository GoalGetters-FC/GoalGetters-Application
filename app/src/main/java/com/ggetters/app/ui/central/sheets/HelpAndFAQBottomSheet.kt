package com.ggetters.app.ui.central.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ggetters.app.R
import com.ggetters.app.databinding.BottomSheetHelpFaqBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class HelpAndFAQBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetHelpFaqBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetHelpFaqBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnClose.setOnClickListener { dismiss() }
        
        // TODO: Populate FAQ items
        binding.tvFaqContent.text = """
            Frequently Asked Questions:
            
            Q: How do I join a team?
            A: Use the team code provided by your coach or manager.
            
            Q: How do I update my profile?
            A: Go to Account > Edit Account to update your information.
            
            Q: How do I view match schedules?
            A: Check the Calendar tab for upcoming matches and events.
            
            Q: How do I report a bug?
            A: Contact the developers through the Contact Developers option.
        """.trimIndent()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

