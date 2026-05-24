package com.bpeople.finpilot.ui.screens.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.repository.PinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PinViewModel @Inject constructor(
    private val pinRepository: PinRepository,
) : ViewModel() {

    enum class Mode { SETUP_ENTER, SETUP_CONFIRM, ENTRY }

    data class PinUiState(
        val mode: Mode = Mode.ENTRY,
        val enteredDigits: String = "",
        val firstPassPin: String = "",
        val errorMessage: String? = null,
        val shakeError: Boolean = false,
        val failedAttempts: Int = 0,
        val pinVerified: Boolean = false,
        val pinSaved: Boolean = false,
    )

    private val _uiState = MutableStateFlow(PinUiState())
    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()

    val hasPinSet: StateFlow<Boolean> = pinRepository.hasPinSet
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    /** Called when the app goes to background. Locks only if a PIN has been set. */
    fun lock() {
        if (hasPinSet.value) {
            _isLocked.value = true
            _uiState.value = PinUiState(mode = Mode.ENTRY)
        }
    }

    /** Called after the user successfully enters the correct PIN on the lock overlay. */
    fun unlock() {
        _isLocked.value = false
    }

    fun initMode(mode: Mode) {
        _uiState.value = PinUiState(mode = mode)
    }

    fun onDigit(digit: Char) {
        val current = _uiState.value
        if (current.enteredDigits.length >= 4) return
        val updated = current.enteredDigits + digit
        _uiState.update { it.copy(enteredDigits = updated, errorMessage = null) }
        if (updated.length == 4) onPinComplete(updated)
    }

    fun onBackspace() {
        _uiState.update {
            it.copy(enteredDigits = it.enteredDigits.dropLast(1), errorMessage = null)
        }
    }

    fun clearShake() {
        _uiState.update { it.copy(shakeError = false) }
    }

    private fun onPinComplete(pin: String) {
        when (_uiState.value.mode) {
            Mode.SETUP_ENTER -> {
                _uiState.update {
                    it.copy(
                        mode = Mode.SETUP_CONFIRM,
                        firstPassPin = pin,
                        enteredDigits = "",
                    )
                }
            }
            Mode.SETUP_CONFIRM -> {
                if (pin == _uiState.value.firstPassPin) {
                    viewModelScope.launch {
                        pinRepository.savePin(pin)
                        _uiState.update { it.copy(pinSaved = true) }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            enteredDigits = "",
                            firstPassPin = "",
                            mode = Mode.SETUP_ENTER,
                            errorMessage = "PINs didn't match. Please try again.",
                            shakeError = true,
                        )
                    }
                }
            }
            Mode.ENTRY -> {
                viewModelScope.launch {
                    val valid = pinRepository.verifyPin(pin)
                    if (valid) {
                        _uiState.update { it.copy(pinVerified = true) }
                    } else {
                        val attempts = _uiState.value.failedAttempts + 1
                        val msg = if (attempts >= 5) {
                            "Too many incorrect attempts."
                        } else {
                            "Incorrect PIN. ${5 - attempts} attempt(s) remaining."
                        }
                        _uiState.update {
                            it.copy(
                                enteredDigits = "",
                                failedAttempts = attempts,
                                errorMessage = msg,
                                shakeError = true,
                            )
                        }
                    }
                }
            }
        }
    }
}
