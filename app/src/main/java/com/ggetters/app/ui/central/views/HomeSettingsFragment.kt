package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.ggetters.app.R
import com.ggetters.app.ui.central.viewmodels.HomeSettingsViewModel
import com.ggetters.app.ui.central.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeSettingsFragment : Fragment() {


    private val activeModel: HomeSettingsViewModel by viewModels()
    private val sharedModel: HomeViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
}