package com.university.scientificcalculator.viewmodel

import com.university.scientificcalculator.model.AngleMode
import com.university.scientificcalculator.model.HistoryItem

/**
 * Everything the Composable calculator screen needs to render itself.
 * Immutable: every user action produces a brand-new copy via [CalculatorViewModel].
 */
data class CalculatorUiState(
    val expression: String = "",
    val result: String = "0",
    val isError: Boolean = false,
    val angleMode: AngleMode = AngleMode.DEG,
    val history: List<HistoryItem> = emptyList(),
    val isHistoryVisible: Boolean = false,
    /** True once '=' has been pressed, so the next digit starts a fresh expression. */
    val isResultFinal: Boolean = false
)
