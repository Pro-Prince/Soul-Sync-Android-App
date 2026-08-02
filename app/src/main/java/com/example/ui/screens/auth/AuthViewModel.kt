package com.example.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.SettingsRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.SignInResult
import com.example.data.repository.SignUpResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    fun performSignIn(email: String, passwordKey: String, onResult: (AuthResult) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            val result = authRepository.signIn(email, passwordKey)
            _loading.value = false
            when (result) {
                is SignInResult.Success -> {
                    settingsRepository.setUserInfo(result.userAccount.id, result.userAccount.email, result.userAccount.displayName)
                    onResult(AuthResult.Success(result.userAccount.email, result.userAccount.displayName))
                }
                is SignInResult.Error -> {
                    onResult(AuthResult.Error(result.message))
                }
            }
        }
    }

    fun performSignUp(email: String, passwordKey: String, displayName: String, onResult: (AuthResult) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            val result = authRepository.signUp(email, passwordKey, displayName)
            _loading.value = false
            when (result) {
                is SignUpResult.Success -> {
                    settingsRepository.setUserInfo(result.userAccount.id, result.userAccount.email, result.userAccount.displayName)
                    onResult(AuthResult.Success(result.userAccount.email, result.userAccount.displayName))
                }
                is SignUpResult.Error -> {
                    onResult(AuthResult.Error(result.message))
                }
            }
        }
    }

    fun continueWithGoogleMock(onResult: (AuthResult) -> Unit) {
        // Fallback or demo fast click Google button
        viewModelScope.launch {
            val mockEmail = "peaceful.seeker@soulsync.com"
            val mockName = "Peaceful Seeker"
            settingsRepository.setUserInfo("00000000-0000-0000-0000-000000000000", mockEmail, mockName)
            onResult(AuthResult.Success(mockEmail, mockName))
        }
    }

    sealed class AuthResult {
        data class Success(val email: String, val name: String) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(settingsRepository, authRepository) as T
        }
    }
}
