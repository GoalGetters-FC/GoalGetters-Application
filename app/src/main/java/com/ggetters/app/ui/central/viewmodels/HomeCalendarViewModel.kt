package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeCalendarViewModel @Inject constructor() : ViewModel() {
    companion object {
        private const val TAG = "HomeCalendarViewModel"
    }
}