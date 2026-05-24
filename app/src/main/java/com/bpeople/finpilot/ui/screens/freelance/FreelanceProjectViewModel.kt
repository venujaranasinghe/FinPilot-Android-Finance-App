package com.bpeople.finpilot.ui.screens.freelance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.FreelanceProject
import com.bpeople.finpilot.data.repository.FreelanceProjectRepository
import com.bpeople.finpilot.data.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FreelanceProjectViewModel @Inject constructor(
    private val repository: FreelanceProjectRepository,
) : ViewModel() {

    data class UiState(
        val projects: List<FreelanceProject> = emptyList(),
        val isLoading: Boolean = true,
        val showDialog: Boolean = false,
        val editingProject: FreelanceProject? = null,
        val clientName: String = "",
        val projectTitle: String = "",
        val agreedAmount: String = "",
        val paidAmount: String = "",
        val status: String = "OPEN",
        val errorMessage: String? = null,
        val successMessage: String? = null,
    ) {
        val totalAgreed get() = projects.sumOf { it.agreedAmount }
        val totalPaid get() = projects.sumOf { it.paidAmount }
        val totalOutstanding get() = totalAgreed - totalPaid
        val activeCount get() = projects.count { it.status == "ACTIVE" }
        val completedCount get() = projects.count { it.status == "COMPLETED" }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProjects().collect { list ->
                _uiState.update { it.copy(projects = list, isLoading = false) }
            }
        }
    }

    fun openAddDialog() = _uiState.update {
        it.copy(showDialog = true, editingProject = null,
            clientName = "", projectTitle = "", agreedAmount = "", paidAmount = "", status = "OPEN")
    }

    fun openEditDialog(project: FreelanceProject) = _uiState.update {
        it.copy(showDialog = true, editingProject = project,
            clientName = project.clientName, projectTitle = project.projectTitle,
            agreedAmount = project.agreedAmount.toBigDecimal().stripTrailingZeros().toPlainString(),
            paidAmount = project.paidAmount.toBigDecimal().stripTrailingZeros().toPlainString(),
            status = project.status)
    }

    fun closeDialog() = _uiState.update { it.copy(showDialog = false) }

    fun setClientName(v: String) = _uiState.update { it.copy(clientName = v) }
    fun setProjectTitle(v: String) = _uiState.update { it.copy(projectTitle = v) }
    fun setAgreedAmount(v: String) = _uiState.update { it.copy(agreedAmount = v) }
    fun setPaidAmount(v: String) = _uiState.update { it.copy(paidAmount = v) }
    fun setStatus(v: String) = _uiState.update { it.copy(status = v) }

    fun saveProject() {
        val s = _uiState.value
        if (s.clientName.isBlank() || s.projectTitle.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Client name and project title are required") }
            return
        }
        val agreed = s.agreedAmount.toDoubleOrNull() ?: 0.0
        val paid = s.paidAmount.toDoubleOrNull() ?: 0.0
        val project = s.editingProject?.copy(
            clientName = s.clientName, projectTitle = s.projectTitle,
            agreedAmount = agreed, paidAmount = paid, status = s.status,
        ) ?: FreelanceProject(
            clientName = s.clientName, projectTitle = s.projectTitle,
            agreedAmount = agreed, paidAmount = paid, status = s.status,
        )
        viewModelScope.launch {
            repository.addOrUpdate(project).collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update {
                        it.copy(showDialog = false, successMessage = "Project saved successfully")
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(errorMessage = result.throwable.message ?: "Save failed")
                    }
                    else -> {}
                }
            }
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.delete(projectId).collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update { it.copy(successMessage = "Project deleted") }
                    is Result.Error -> _uiState.update {
                        it.copy(errorMessage = result.throwable.message ?: "Delete failed")
                    }
                    else -> {}
                }
            }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }
}
