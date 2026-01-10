package com.mora.matritech.screens.admin.users.com.mora.matritech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mora.matritech.data.repository.UserRepository
import com.mora.matritech.data.repository.InstitucionRepository
import com.mora.matritech.ui.screens.superadmin.AdminViewModelCR

class ViewModelFactory(
    private val userRepository: UserRepository,
    private val institucionRepository: InstitucionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AdminViewModelCR::class.java) -> {
                AdminViewModelCR(userRepository, institucionRepository) as T
            }
            // ... otros ViewModels
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}