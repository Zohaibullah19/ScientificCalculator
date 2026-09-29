package com.university.scientificcalculator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.university.scientificcalculator.ui.components.DisplaySection
import com.university.scientificcalculator.ui.components.HistorySheet
import com.university.scientificcalculator.ui.components.Keypad
import com.university.scientificcalculator.ui.components.ModeSelector
import com.university.scientificcalculator.viewmodel.CalculatorAction
import com.university.scientificcalculator.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scientific Calculator") },
                actions = {
                    IconButton(
                        onClick = { viewModel.onAction(CalculatorAction.ShowHistory) },
                        modifier = Modifier.semantics { contentDescription = "Show calculation history" }
                    ) {
                        Icon(Icons.Filled.History, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeSelector(
                    angleMode = uiState.angleMode,
                    onToggle = { viewModel.onAction(CalculatorAction.ToggleAngleMode) }
                )
            }

            DisplaySection(
                expression = uiState.expression,
                result = uiState.result,
                isError = uiState.isError,
                modifier = Modifier.weight(1f, fill = true)
            )

            Keypad(onAction = viewModel::onAction)
        }
    }

    if (uiState.isHistoryVisible) {
        HistorySheet(
            history = uiState.history,
            onDismiss = { viewModel.onAction(CalculatorAction.HideHistory) },
            onClear = { viewModel.onAction(CalculatorAction.ClearHistory) },
            onSelect = { viewModel.onAction(CalculatorAction.SelectHistoryItem(it)) }
        )
    }
}
