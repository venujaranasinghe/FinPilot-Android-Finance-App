package com.bpeople.finpilot.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bpeople.finpilot.ui.theme.FinPilotTheme

@Composable
fun DashboardScreen(
    state: DashboardViewModel.DashboardUiState,
    onAddExpense: () -> Unit,
    onLogout: () -> Unit,
    insightMessage: String? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        SummaryCard(title = "Total Income", value = "LKR ${"%.2f".format(state.totalIncome)}")
        Spacer(modifier = Modifier.height(8.dp))
        SummaryCard(title = "Total Expenses", value = "LKR ${"%.2f".format(state.totalExpenses)}")
        Spacer(modifier = Modifier.height(8.dp))
        SummaryCard(title = "Net Position", value = "LKR ${"%.2f".format(state.netPosition)}")

        if (!insightMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = insightMessage,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAddExpense,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Add Expense")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Logout")
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun DashboardScreenPreview() {
    FinPilotTheme {
        DashboardScreen(
            state = DashboardViewModel.DashboardUiState(
                totalIncome = 250000.0,
                totalExpenses = 56000.0,
                netPosition = 194000.0,
            ),
            onAddExpense = {},
            onLogout = {},
            insightMessage = "Expense saved. At your current pace, this may delay MacBook Goal by about 2 day(s).",
        )
    }
}
