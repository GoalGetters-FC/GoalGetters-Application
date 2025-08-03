package com.ggetters.app.ui.management.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TeamViewerViewModel @Inject constructor() : ViewModel() {
    companion object {
        private const val TAG = "TeamViewerViewModel"
    }
}