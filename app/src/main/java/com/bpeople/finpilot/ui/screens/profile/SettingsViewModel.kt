package com.bpeople.finpilot.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.ExpenseEntry
import com.bpeople.finpilot.data.model.IncomeEntry
import com.bpeople.finpilot.data.model.AuthResult
import com.bpeople.finpilot.data.repository.AuthRepository
import com.bpeople.finpilot.data.repository.ExpenseRepository
import com.bpeople.finpilot.data.repository.IncomeRepository
import com.bpeople.finpilot.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
) : ViewModel() {

    sealed class SettingsEvent {
        data class ShowMessage(val message: String) : SettingsEvent()
        data class ExportReady(val csvContent: String) : SettingsEvent()
        object AccountDeleted : SettingsEvent()
    }

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events = _events.asSharedFlow()

    data class SettingsUiState(
        val notificationsEnabled: Boolean = true,
        val darkModeEnabled: Boolean = false,
        val cloudSyncEnabled: Boolean = true,
        val biometricsEnabled: Boolean = true,
    )

    val uiState: StateFlow<SettingsUiState> = settingsRepository.settings
        .map { prefs ->
            SettingsUiState(
                notificationsEnabled = prefs.notificationsEnabled,
                darkModeEnabled = prefs.darkModeEnabled,
                cloudSyncEnabled = prefs.cloudSyncEnabled,
                biometricsEnabled = prefs.biometricsEnabled,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onNotificationsChange(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    fun onDarkModeChange(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkModeEnabled(enabled)
        }
    }

    fun onCloudSyncChange(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCloudSyncEnabled(enabled)
        }
    }

    fun onBiometricsChange(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBiometricsEnabled(enabled)
        }
    }

    fun onChangePassword() {
        val email = authRepository.getCurrentUserEmail()
        if (email.isNullOrBlank()) {
            viewModelScope.launch {
                _events.emit(SettingsEvent.ShowMessage("No email found for this account"))
            }
            return
        }
        viewModelScope.launch {
            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is AuthResult.Success -> _events.emit(
                    SettingsEvent.ShowMessage("Password reset email sent to $email")
                )
                is AuthResult.Error -> _events.emit(SettingsEvent.ShowMessage(result.message))
            }
        }
    }

    fun onExportData() {
        viewModelScope.launch {
            val csv = withContext(Dispatchers.Default) {
                val expenses = expenseRepository.observeExpenses().first()
                val incomes = incomeRepository.observeIncome().first()
                buildCsv(expenses, incomes)
            }
            _events.emit(SettingsEvent.ExportReady(csv))
        }
    }

    fun onDeleteAccount() {
        viewModelScope.launch {
            when (val result = authRepository.deleteAccount()) {
                is AuthResult.Success -> {
                    _events.emit(SettingsEvent.ShowMessage("Account deleted"))
                    _events.emit(SettingsEvent.AccountDeleted)
                }
                is AuthResult.Error -> _events.emit(SettingsEvent.ShowMessage(result.message))
            }
        }
    }

    private fun buildCsv(expenses: List<ExpenseEntry>, incomes: List<IncomeEntry>): String {
        val builder = StringBuilder()
        builder.append("expenses\n")
        builder.append(
            "id,amount,category,subCategory,paymentMethod,dateMillis,note,isRecurring,tags,originalCurrency,originalAmount\n"
        )
        expenses.forEach { entry ->
            builder.append(
                listOf(
                    entry.id,
                    entry.amount.toString(),
                    entry.category,
                    entry.subCategory.orEmpty(),
                    entry.paymentMethod,
                    entry.date?.toDate()?.time?.toString().orEmpty(),
                    entry.note.orEmpty(),
                    entry.isRecurring.toString(),
                    entry.tags.joinToString("|"),
                    entry.originalCurrency.orEmpty(),
                    entry.originalAmount?.toString().orEmpty(),
                ).joinToString(",") { escapeCsv(it) }
            )
            builder.append('\n')
        }

        builder.append("\n")
        builder.append("incomes\n")
        builder.append(
            "id,source,amountOriginal,currencyOriginal,amountLKR,exchangeRate,dateMillis,label,type,projectRef\n"
        )
        incomes.forEach { entry ->
            builder.append(
                listOf(
                    entry.id,
                    entry.source,
                    entry.amountOriginal.toString(),
                    entry.currencyOriginal,
                    entry.amountLKR.toString(),
                    entry.exchangeRate.toString(),
                    entry.date?.toDate()?.time?.toString().orEmpty(),
                    entry.label.orEmpty(),
                    entry.type,
                    entry.projectRef.orEmpty(),
                ).joinToString(",") { escapeCsv(it) }
            )
            builder.append('\n')
        }

        return builder.toString()
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\n") || value.contains('"')
        if (!needsQuotes) return value
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
