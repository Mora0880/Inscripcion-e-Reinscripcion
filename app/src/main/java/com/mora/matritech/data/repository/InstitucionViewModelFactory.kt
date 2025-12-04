package com.mora.matritech.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mora.matritech.data.repository.InstitucionRepository
import com.mora.matritech.screens.superadmin.InstitucionViewModel

class InstitucionViewModelFactory(
    private val repository: InstitucionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InstitucionViewModel::class.java)) {
            return InstitucionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}