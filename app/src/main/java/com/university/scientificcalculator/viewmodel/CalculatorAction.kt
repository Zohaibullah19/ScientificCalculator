package com.university.scientificcalculator.viewmodel

import com.university.scientificcalculator.model.HistoryItem

/**
 * Every interaction the keypad or top bar can send to the [CalculatorViewModel].
 * Keeping this as a sealed class makes the ViewModel's `when` exhaustive and
 * keeps the UI layer free of any calculation logic.
 */
sealed class CalculatorAction {
    data class Digit(val digit: String) : CalculatorAction()
    object Decimal : CalculatorAction()
    data class Operator(val symbol: String) : CalculatorAction()
    object OpenParen : CalculatorAction()
    object CloseParen : CalculatorAction()
    data class Function(val symbol: String) : CalculatorAction()
    data class Constant(val symbol: String) : CalculatorAction()
    object Square : CalculatorAction()
    object Reciprocal : CalculatorAction()
    object Percent : CalculatorAction()
    object Factorial : CalculatorAction()
    object ToggleSign : CalculatorAction()
    object ToggleAngleMode : CalculatorAction()
    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    object Equals : CalculatorAction()
    object ShowHistory : CalculatorAction()
    object HideHistory : CalculatorAction()
    object ClearHistory : CalculatorAction()
    data class SelectHistoryItem(val item: HistoryItem) : CalculatorAction()
}
