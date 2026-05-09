package com.bpeople.finpilot.ui.screens.expense

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit,
    onExpenseAdded: (String) -> Unit,
) {
    val state by viewModel.expenseState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
    }

    LaunchedEffect(state.insightMessage) {
        val insight = state.insightMessage ?: return@LaunchedEffect
        viewModel.consumeInsight()
        onExpenseAdded(insight)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.amount,
                onValueChange = viewModel::onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Amount") },
                leadingIcon = { Text("LKR") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )

            Text("Category", style = MaterialTheme.typography.titleSmall)
            ChipRow(
                items = ExpenseViewModel.CATEGORIES,
                selected = state.category,
                onSelected = viewModel::onCategoryChange,
            )

            Text("Payment Method", style = MaterialTheme.typography.titleSmall)
            ChipRow(
                items = ExpenseViewModel.PAYMENT_METHODS,
                selected = state.paymentMethod,
                onSelected = viewModel::onPaymentMethodChange,
            )

            AssistChip(
                onClick = {
                    val calendar = Calendar.getInstance().apply { timeInMillis = state.dateMillis }
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val selected = Calendar.getInstance().apply {
                                set(year, month, dayOfMonth, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            viewModel.onDateChange(selected.timeInMillis)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                    ).show()
                },
                label = { Text("Date: ${dateFormat.format(Date(state.dateMillis))}") },
            )

            OutlinedTextField(
                value = state.subCategory,
                onValueChange = viewModel::onSubCategoryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Sub-category (optional)") },
                placeholder = { Text("UberEats, PickMe, Gym") },
            )

            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Note (optional)") },
            )

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Recurring expense")
                Switch(
                    checked = state.isRecurring,
                    onCheckedChange = viewModel::onRecurringChange,
                )
            }

            Button(
                onClick = viewModel::addExpense,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text("Add Expense")
                }
            }

            Text(
                text = "Tip: Fast path is amount + Add Expense with default chips (about 3 taps).",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRow(
    items: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelected(option) },
                label = { Text(option) },
            )
        }
    }
}
