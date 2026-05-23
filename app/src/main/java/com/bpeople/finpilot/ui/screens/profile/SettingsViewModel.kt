package com.bpeople.finpilot.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.ExpenseEntry
import com.bpeople.finpilot.data.model.IncomeEntry
import com.bpeople.finpilot.data.model.AuthResult
import com.bpeople.finpilot.data.model.ThemeMode
import com.bpeople.finpilot.data.repository.AuthRepository
import com.bpeople.finpilot.data.repository.ExpenseRepository
import com.bpeople.finpilot.data.repository.IncomeRepository
import com.bpeople.finpilot.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
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
        val cloudSyncEnabled: Boolean = true,
        val biometricsEnabled: Boolean = true,
        val themeMode: ThemeMode = ThemeMode.SYSTEM,
        // Currency
        val usdEnabled: Boolean = true,
        val usdtEnabled: Boolean = false,
        val autoConvert: Boolean = true,
        val rateLastUpdated: String = "Updated 2h ago",
        // Detailed notifications
        val notifySalaryReminder: Boolean = true,
        val notifyWeeklySummary: Boolean = true,
        val notifyGoalMilestone: Boolean = true,
        val notifyBudgetOverspend: Boolean = true,
        val budgetOverspendThreshold: String = "10000",
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { prefs ->
                _uiState.update { current ->
                    current.copy(
                        notificationsEnabled = prefs.notificationsEnabled,
                        cloudSyncEnabled = prefs.cloudSyncEnabled,
                        biometricsEnabled = prefs.biometricsEnabled,
                        themeMode = prefs.themeMode,
                    )
                }
            }
        }
    }

    fun setUsdEnabled(v: Boolean) { _uiState.update { it.copy(usdEnabled = v) } }
    fun setUsdtEnabled(v: Boolean) { _uiState.update { it.copy(usdtEnabled = v) } }
    fun setAutoConvert(v: Boolean) { _uiState.update { it.copy(autoConvert = v) } }
    fun setNotifySalaryReminder(v: Boolean) { _uiState.update { it.copy(notifySalaryReminder = v) } }
    fun setNotifyWeeklySummary(v: Boolean) { _uiState.update { it.copy(notifyWeeklySummary = v) } }
    fun setNotifyGoalMilestone(v: Boolean) { _uiState.update { it.copy(notifyGoalMilestone = v) } }
    fun setNotifyBudgetOverspend(v: Boolean) { _uiState.update { it.copy(notifyBudgetOverspend = v) } }
    fun setBudgetOverspendThreshold(v: String) { _uiState.update { it.copy(budgetOverspendThreshold = v) } }

    fun onClearCache() {
        viewModelScope.launch {
            _events.emit(SettingsEvent.ShowMessage("Cache cleared"))
        }
    }

    fun onNotificationsChange(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
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

    fun onThemeModeChange(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
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
