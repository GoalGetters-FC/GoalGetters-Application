package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ggetters.app.ui.central.models.HomeUiConfiguration
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    companion object {
        private const val TAG = "HomeViewModel"
    }
    
    
// --- Fields
    
    
    private val _uiConfiguration = MutableLiveData<HomeUiConfiguration>()
    val uiConfiguration: LiveData<HomeUiConfiguration> = _uiConfiguration
    
    
// --- Functions
    
    
    fun useViewConfiguration(configuration: HomeUiConfiguration) {
        _uiConfiguration.value = configuration
    }
}