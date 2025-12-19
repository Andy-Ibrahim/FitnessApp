package com.example.classpass.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.classpass.data.database.AppDatabase
import com.example.classpass.data.model.User
import com.example.classpass.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userRepository: UserRepository
    
    val currentUser: LiveData<User?>
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess
    
    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)
        currentUser = userRepository.currentUser
    }
    
    fun createUser(user: User) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                userRepository.insertUser(user)
                _updateSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Error creating profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateUser(user: User) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                userRepository.updateUser(user)
                _updateSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Error updating profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }
}
