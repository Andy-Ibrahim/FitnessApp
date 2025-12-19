package com.example.classpass.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.classpass.ClassPassApplication
import com.example.classpass.data.model.User
import kotlinx.coroutines.launch

/**
 * ViewModel for Settings Sheet.
 * Manages user settings, preferences, and account actions.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userRepository = (application as ClassPassApplication).userRepository
    private val sharedPrefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    
    // User data
    val currentUser: LiveData<User?> = userRepository.currentUser
    
    // Preferences state
    private val _selectedUnits = MutableLiveData(sharedPrefs.getString("units", "Metric") ?: "Metric")
    val selectedUnits: LiveData<String> = _selectedUnits
    
    private val _trainingDaysPerWeek = MutableLiveData(sharedPrefs.getInt("training_days", 4))
    val trainingDaysPerWeek: LiveData<Int> = _trainingDaysPerWeek
    
    // Haptic feedback state
    private val _hapticFeedbackEnabled = MutableLiveData(true)
    val hapticFeedbackEnabled: LiveData<Boolean> = _hapticFeedbackEnabled
    
    // Loading state
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Error state
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    /**
     * Toggle haptic feedback on/off.
     */
    fun toggleHapticFeedback() {
        _hapticFeedbackEnabled.value = !(_hapticFeedbackEnabled.value ?: true)
        // TODO: Save to SharedPreferences or database
    }
    
    /**
     * Set haptic feedback state.
     */
    fun setHapticFeedback(enabled: Boolean) {
        _hapticFeedbackEnabled.value = enabled
        // TODO: Save to SharedPreferences or database
    }
    
    /**
     * Handle profile navigation.
     */
    fun onProfileClick() {
        // TODO: Navigate to profile screen
    }
    
    /**
     * Handle billing navigation.
     */
    fun onBillingClick() {
        // TODO: Navigate to billing screen
    }
    
    /**
     * Handle capabilities navigation.
     */
    fun onCapabilitiesClick() {
        // TODO: Navigate to capabilities screen
    }
    
    /**
     * Handle connectors navigation.
     */
    fun onConnectorsClick() {
        // TODO: Navigate to connectors screen
    }
    
    /**
     * Handle permissions navigation.
     */
    fun onPermissionsClick() {
        // TODO: Navigate to permissions screen
    }
    
    /**
     * Handle appearance navigation.
     */
    fun onAppearanceClick() {
        // TODO: Navigate to appearance screen
    }
    
    /**
     * Handle speech language navigation.
     */
    fun onSpeechLanguageClick() {
        // TODO: Navigate to speech language screen
    }
    
    /**
     * Handle notifications navigation.
     */
    fun onNotificationsClick() {
        // TODO: Navigate to notifications screen
    }
    
    /**
     * Handle privacy navigation.
     */
    fun onPrivacyClick() {
        // TODO: Navigate to privacy screen
    }
    
    /**
     * Log out the current user.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // TODO: Clear user session
                // TODO: Clear cached data
                // TODO: Navigate to login screen
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to log out: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clear error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    // ========================================
    // PROFILE UPDATE METHODS
    // ========================================
    
    /**
     * Update user name.
     */
    fun updateUserName(name: String) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    val updatedUser = user.copy(name = name)
                    userRepository.updateUser(updatedUser)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update name: ${e.message}"
            }
        }
    }
    
    /**
     * Update user email.
     */
    fun updateUserEmail(email: String) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    val updatedUser = user.copy(email = email)
                    userRepository.updateUser(updatedUser)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update email: ${e.message}"
            }
        }
    }
    
    /**
     * Update user age and gender.
     */
    fun updateUserAgeGender(age: Int, gender: String) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    val updatedUser = user.copy(age = age, gender = gender)
                    userRepository.updateUser(updatedUser)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update age/gender: ${e.message}"
            }
        }
    }
    
    /**
     * Update user height and weight.
     */
    fun updateUserHeightWeight(height: Int, weight: Int) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    val updatedUser = user.copy(height = height, weight = weight)
                    userRepository.updateUser(updatedUser)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update height/weight: ${e.message}"
            }
        }
    }
    
    /**
     * Update user fitness level.
     */
    fun updateUserFitnessLevel(level: String) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    val updatedUser = user.copy(fitnessLevel = level)
                    userRepository.updateUser(updatedUser)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update fitness level: ${e.message}"
            }
        }
    }
    
    /**
     * Update user primary goal.
     */
    fun updateUserPrimaryGoal(goal: String) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    val updatedUser = user.copy(primaryGoal = goal)
                    userRepository.updateUser(updatedUser)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update primary goal: ${e.message}"
            }
        }
    }
    
    /**
     * Update user injuries/limitations.
     */
    fun updateUserInjuries(injuries: String) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    val updatedUser = user.copy(injuries = injuries)
                    userRepository.updateUser(updatedUser)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update injuries: ${e.message}"
            }
        }
    }
    
    // ========================================
    // PREFERENCES METHODS
    // ========================================
    
    /**
     * Update units preference (Metric/Imperial).
     */
    fun updateUnits(units: String) {
        _selectedUnits.value = units
        sharedPrefs.edit().putString("units", units).apply()
    }
    
    /**
     * Update training days per week preference.
     */
    fun updateTrainingDays(days: Int) {
        _trainingDaysPerWeek.value = days
        sharedPrefs.edit().putInt("training_days", days).apply()
    }
    
    /**
     * Delete user account.
     */
    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                currentUser.value?.let { user ->
                    userRepository.deleteUser(user)
                    // Clear preferences
                    sharedPrefs.edit().clear().apply()
                    onSuccess()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete account: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

