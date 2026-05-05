package com.bpeople.finpilot.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.AuthResult
import com.bpeople.finpilot.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _authSuccess = MutableStateFlow(false)
    val authSuccess: StateFlow<Boolean> = _authSuccess.asStateFlow()

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isLoggedIn: Boolean get() = authRepository.isLoggedIn()

    fun getCurrentUserId(): String? = authRepository.getCurrentUserId()

    fun onEmailChange(value: String) { _email.value = value.trim(); clearError() }
    fun onPasswordChange(value: String) { _password.value = value; clearError() }
    fun onConfirmPasswordChange(value: String) { _confirmPassword.value = value; clearError() }
    fun onFullNameChange(value: String) { _fullName.value = value; clearError() }

    fun login() {
        if (!validateLogin()) return
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = authRepository.login(_email.value, _password.value)) {
                is AuthResult.Success -> _authSuccess.value = true
                is AuthResult.Error -> _errorMessage.value = result.message
            }
            _isLoading.value = false
        }
    }

    fun register() {
        if (!validateRegister()) return
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = authRepository.register(_email.value, _password.value)) {
                is AuthResult.Success -> _authSuccess.value = true
                is AuthResult.Error -> _errorMessage.value = result.message
            }
            _isLoading.value = false
        }
    }

    fun signOut() {
        authRepository.signOut()
        _authSuccess.value = false
    }

    private fun validateLogin(): Boolean {
        if (_email.value.isBlank()) { _errorMessage.value = "Email is required"; return false }
        if (_password.value.isBlank()) { _errorMessage.value = "Password is required"; return false }
        return true
    }

    private fun validateRegister(): Boolean {
        if (_fullName.value.isBlank()) { _errorMessage.value = "Name is required"; return false }
        if (_email.value.isBlank()) { _errorMessage.value = "Email is required"; return false }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()) {
            _errorMessage.value = "Enter a valid email"; return false
        }
        if (_password.value.length < 6) { _errorMessage.value = "Password must be at least 6 characters"; return false }
        if (_password.value != _confirmPassword.value) { _errorMessage.value = "Passwords do not match"; return false }
        return true
    }

    fun clearError() { _errorMessage.value = null }
    fun clearAuthSuccess() { _authSuccess.value = false }
}