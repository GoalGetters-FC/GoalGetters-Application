package com.ggetters.app.ui.central.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ggetters.app.R
import com.ggetters.app.databinding.BottomSheetNotificationSettingsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NotificationSettingsBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetNotificationSettingsBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetNotificationSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener { 
            // TODO: Save notification settings
            dismiss() 
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

