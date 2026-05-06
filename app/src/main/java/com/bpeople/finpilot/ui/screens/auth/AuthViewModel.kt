package com.bpeople.finpilot.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.AuthResult
import com.bpeople.finpilot.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    data class AuthUiState(
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val fullName: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val authSuccess: Boolean = false,
    )

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    val isLoading: StateFlow<Boolean> = authState
        .map { it.isLoading }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val errorMessage: StateFlow<String?> = authState
        .map { it.errorMessage }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val authSuccess: StateFlow<Boolean> = authState
        .map { it.authSuccess }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isLoggedIn: Boolean get() = authRepository.isLoggedIn()

    fun getCurrentUserId(): String? = authRepository.getCurrentUserId()

    fun onEmailChange(value: String) {
        _authState.update { it.copy(email = value.trim(), errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _authState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _authState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    fun onFullNameChange(value: String) {
        _authState.update { it.copy(fullName = value, errorMessage = null) }
    }

    fun login() {
        if (!validateLogin()) return
        _authState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = authRepository.login(_authState.value.email, _authState.value.password)) {
                is AuthResult.Success -> _authState.update {
                    it.copy(authSuccess = true, isLoading = false)
                }
                is AuthResult.Error -> _authState.update {
                    it.copy(errorMessage = result.message, isLoading = false)
                }
            }
        }
    }

    fun register() {
        if (!validateRegister()) return
        _authState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = authRepository.register(_authState.value.email, _authState.value.password)) {
                is AuthResult.Success -> _authState.update {
                    it.copy(authSuccess = true, isLoading = false)
                }
                is AuthResult.Error -> _authState.update {
                    it.copy(errorMessage = result.message, isLoading = false)
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _authState.update { it.copy(authSuccess = false) }
    }

    private fun validateLogin(): Boolean {
        val state = _authState.value
        if (state.email.isBlank()) {
            _authState.update { it.copy(errorMessage = "Email is required") }
            return false
        }
        if (state.password.isBlank()) {
            _authState.update { it.copy(errorMessage = "Password is required") }
            return false
        }
        return true
    }

    private fun validateRegister(): Boolean {
        val state = _authState.value
        if (state.fullName.isBlank()) {
            _authState.update { it.copy(errorMessage = "Name is required") }
            return false
        }
        if (state.email.isBlank()) {
            _authState.update { it.copy(errorMessage = "Email is required") }
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _authState.update { it.copy(errorMessage = "Enter a valid email") }
            return false
        }
        if (state.password.length < 6) {
            _authState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            return false
        }
        if (state.password != state.confirmPassword) {
            _authState.update { it.copy(errorMessage = "Passwords do not match") }
            return false
        }
        return true
    }

    fun clearError() {
        _authState.update { it.copy(errorMessage = null) }
    }

    fun clearAuthSuccess() {
        _authState.update { it.copy(authSuccess = false) }
    }
}