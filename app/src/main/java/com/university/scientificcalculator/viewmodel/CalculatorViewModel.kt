package com.university.scientificcalculator.viewmodel

import androidx.lifecycle.ViewModel
import com.university.scientificcalculator.calculator.CalculatorEngine
import com.university.scientificcalculator.calculator.EngineResult
import com.university.scientificcalculator.model.AngleMode
import com.university.scientificcalculator.model.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val MAX_EXPRESSION_LENGTH = 100
private const val MAX_HISTORY_SIZE = 50

// Explicitly declare this as a Set of Char values.
// This matches base.last() and lastOrNull(), which return Char.
private val OPERATOR_SYMBOLS: Set<Char> = setOf(
    '+',
    '-',
    '×',
    '÷',
    '^',
    '%'
)

/**
 * Owns CalculatorUiState and is the single place where keypad actions
 * turn into edits of the expression string and calls into CalculatorEngine.
 *
 * Contains no Android UI code, which keeps it easy to reason about and test.
 */
class CalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())

    val uiState: StateFlow<CalculatorUiState> =
        _uiState.asStateFlow()

    // ---------------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------------

    fun onAction(action: CalculatorAction) {
        when (action) {

            is CalculatorAction.Digit ->
                appendDigit(action.digit)

            CalculatorAction.Decimal ->
                appendDecimal()

            is CalculatorAction.Operator ->
                appendOperator(action.symbol)

            CalculatorAction.OpenParen ->
                appendOpenParen()

            CalculatorAction.CloseParen ->
                appendCloseParen()

            is CalculatorAction.Function ->
                appendFunction(action.symbol)

            is CalculatorAction.Constant ->
                appendConstant(action.symbol)

            CalculatorAction.Square ->
                wrapExpression(
                    prefix = "(",
                    suffix = ")^2"
                )

            CalculatorAction.Reciprocal ->
                wrapExpression(
                    prefix = "1/(",
                    suffix = ")"
                )

            CalculatorAction.Percent ->
                appendPostfix("%")

            CalculatorAction.Factorial ->
                appendPostfix("!")

            CalculatorAction.ToggleSign ->
                toggleSign()

            CalculatorAction.ToggleAngleMode ->
                toggleAngleMode()

            CalculatorAction.Clear ->
                clearAll()

            CalculatorAction.Delete ->
                deleteLastChar()

            CalculatorAction.Equals ->
                evaluateFinal()

            CalculatorAction.ShowHistory ->
                _uiState.update {
                    it.copy(isHistoryVisible = true)
                }

            CalculatorAction.HideHistory ->
                _uiState.update {
                    it.copy(isHistoryVisible = false)
                }

            CalculatorAction.ClearHistory ->
                _uiState.update {
                    it.copy(history = emptyList())
                }

            is CalculatorAction.SelectHistoryItem ->
                selectHistoryItem(action.item)
        }
    }

    // ---------------------------------------------------------------------
    // Expression editing
    // ---------------------------------------------------------------------

    /**
     * If the previous action produced a final result, start a new
     * expression when the user enters something new.
     */
    private fun startFreshIfNeeded(
        current: CalculatorUiState
    ): String {
        return if (current.isResultFinal) {
            ""
        } else {
            current.expression
        }
    }

    /**
     * Adds a digit to the expression.
     */
    private fun appendDigit(digit: String) {
        _uiState.update { state ->

            val base = startFreshIfNeeded(state)

            if (base.length >= MAX_EXPRESSION_LENGTH) {
                return@update state
            }

            state.copy(
                expression = base + digit,
                isResultFinal = false,
                isError = false
            )
        }

        refreshLivePreview()
    }

    /**
     * Adds a decimal point.
     *
     * Prevents multiple decimal points inside the same number.
     */
    private fun appendDecimal() {
        _uiState.update { state ->

            val base = startFreshIfNeeded(state)

            // Find the beginning of the current number.
            val segmentStart =
                base.indexOfLast {
                    it in "+-×÷^(%"
                } + 1

            val currentSegment =
                base.substring(segmentStart)

            // Do not allow another decimal point in the same number.
            if (currentSegment.contains('.')) {
                return@update state.copy(
                    isResultFinal = false
                )
            }

            val addition =
                if (
                    currentSegment.isEmpty() ||
                    currentSegment.last() !in "0123456789"
                ) {
                    "0."
                } else {
                    "."
                }

            state.copy(
                expression = base + addition,
                isResultFinal = false,
                isError = false
            )
        }

        refreshLivePreview()
    }

    /**
     * Adds an operator.
     *
     * If the expression already ends with an operator,
     * the previous operator is replaced.
     */
    private fun appendOperator(symbol: String) {

        _uiState.update { state ->

            var base = startFreshIfNeeded(state)

            // Allow a leading minus for negative numbers.
            if (base.isEmpty()) {

                return@update if (symbol == "-") {

                    state.copy(
                        expression = "-",
                        isResultFinal = false,
                        isError = false
                    )

                } else {
                    state
                }
            }

            // If the last character is already an operator,
            // replace it instead of adding two operators.
            if (
                base.isNotEmpty() &&
                base.last() in OPERATOR_SYMBOLS
            ) {
                base = base.dropLast(1)
            }

            state.copy(
                expression = base + symbol,
                isResultFinal = false,
                isError = false
            )
        }

        refreshLivePreview()
    }

    /**
     * Adds an opening parenthesis.
     */
    private fun appendOpenParen() {

        _uiState.update { state ->

            val base = startFreshIfNeeded(state)

            if (base.length >= MAX_EXPRESSION_LENGTH) {
                return@update state
            }

            state.copy(
                expression = base + "(",
                isResultFinal = false,
                isError = false
            )
        }

        refreshLivePreview()
    }

    /**
     * Adds a closing parenthesis only when there is an unmatched
     * opening parenthesis and the expression does not currently
     * end with an operator or another opening parenthesis.
     */
    private fun appendCloseParen() {

        _uiState.update { state ->

            val base = startFreshIfNeeded(state)

            val openCount =
                base.count { it == '(' }

            val closeCount =
                base.count { it == ')' }

            // No unmatched opening parenthesis.
            if (openCount <= closeCount) {
                return@update state
            }

            val lastChar =
                base.lastOrNull()

            // Do not allow:
            // ()
            // 5+
            // 5+)
            if (
                lastChar == null ||
                lastChar == '(' ||
                lastChar in OPERATOR_SYMBOLS
            ) {
                return@update state
            }

            state.copy(
                expression = base + ")",
                isResultFinal = false,
                isError = false
            )
        }

        refreshLivePreview()
    }

    /**
     * Adds a calculator function such as sin(, cos(, tan(, etc.
     */
    private fun appendFunction(symbol: String) {

        _uiState.update { state ->

            val base = startFreshIfNeeded(state)

            if (
                base.length + symbol.length >
                MAX_EXPRESSION_LENGTH
            ) {
                return@update state
            }

            state.copy(
                expression = base + symbol,
                isResultFinal = false,
                isError = false
            )
        }

        refreshLivePreview()
    }

    /**
     * Adds a mathematical constant such as π or e.
     */
    private fun appendConstant(symbol: String) {

        _uiState.update { state ->

            val base = startFreshIfNeeded(state)

            if (
                base.length + symbol.length >
                MAX_EXPRESSION_LENGTH
            ) {
                return@update state
            }

            state.copy(
                expression = base + symbol,
                isResultFinal = false,
                isError = false
            )
        }

        refreshLivePreview()
    }

    /**
     * Adds postfix operators such as % and !.
     */
    private fun appendPostfix(symbol: String) {

        _uiState.update { state ->

            val base = startFreshIfNeeded(state)

            val lastChar =
                base.lastOrNull()
                    ?: return@update state

            // Postfix operators are valid after:
            // 5
            // 5.5
            // )
            // e
            // π
            if (
                lastChar.isDigit() ||
                lastChar == ')' ||
                lastChar == 'e' ||
                lastChar == 'π'
            ) {

                if (
                    base.length + symbol.length >
                    MAX_EXPRESSION_LENGTH
                ) {
                    return@update state
                }

                state.copy(
                    expression = base + symbol,
                    isResultFinal = false,
                    isError = false
                )

            } else {
                state
            }
        }

        refreshLivePreview()
    }

    /**
     * Used by x² and 1/x.
     *
     * These operations apply to the whole current expression.
     */
    private fun wrapExpression(
        prefix: String,
        suffix: String
    ) {

        _uiState.update { state ->

            val base =
                if (state.expression.isNotBlank()) {

                    state.expression

                } else if (
                    state.isResultFinal &&
                    !state.isError
                ) {

                    state.result

                } else {

                    return@update state
                }

            val newExpression =
                "$prefix$base$suffix"

            if (
                newExpression.length >
                MAX_EXPRESSION_LENGTH
            ) {
                return@update state
            }

            state.copy(
                expression = newExpression,
                isResultFinal = false,
                isError = false
            )
        }

        refreshLivePreview()
    }

    /**
     * Toggles the sign of the trailing number.
     */
    private fun toggleSign() {

        _uiState.update { state ->

            val base =
                startFreshIfNeeded(state)

            if (base.isEmpty()) {
                return@update state
            }

            state.copy(
                expression =
                    toggleTrailingNumberSign(base),
                isResultFinal = false,
                isError = false
            )
        }

        refreshLivePreview()
    }

    /**
     * Adds or removes a negative sign from the last number.
     */
    private fun toggleTrailingNumberSign(
        expr: String
    ): String {

        var i = expr.length

        // Move backwards over the current number.
        while (
            i > 0 &&
            (
                    expr[i - 1].isDigit() ||
                            expr[i - 1] == '.'
                    )
        ) {
            i--
        }

        val numberStart = i

        // The expression does not end with a number.
        if (numberStart == expr.length) {
            return expr
        }

        // Check whether there is already a unary minus.
        if (
            numberStart > 0 &&
            expr[numberStart - 1] == '-'
        ) {

            val minusIndex =
                numberStart - 1

            val isUnary =
                minusIndex == 0 ||
                        expr[minusIndex - 1] in OPERATOR_SYMBOLS ||
                        expr[minusIndex - 1] == '('

            if (isUnary) {

                return (
                        expr.substring(0, minusIndex) +
                                expr.substring(numberStart)
                        )
            }
        }

        // Add a negative sign.
        return (
                expr.substring(0, numberStart) +
                        "-" +
                        expr.substring(numberStart)
                )
    }

    /**
     * Changes between degrees and radians.
     */
    private fun toggleAngleMode() {

        _uiState.update { state ->

            val newMode =
                if (state.angleMode == AngleMode.DEG) {
                    AngleMode.RAD
                } else {
                    AngleMode.DEG
                }

            state.copy(
                angleMode = newMode
            )
        }

        refreshLivePreview()
    }

    /**
     * Clears the current calculation.
     */
    private fun clearAll() {

        _uiState.update {

            it.copy(
                expression = "",
                result = "0",
                isError = false,
                isResultFinal = false
            )
        }
    }

    /**
     * Deletes the last character.
     */
    private fun deleteLastChar() {

        _uiState.update { state ->

            when {

                // If a final result is displayed,
                // delete starts a new calculation.
                state.isResultFinal -> {

                    state.copy(
                        expression = "",
                        result = "0",
                        isError = false,
                        isResultFinal = false
                    )
                }

                // Delete the last character.
                state.expression.isNotEmpty() -> {

                    state.copy(
                        expression =
                            state.expression.dropLast(1),
                        isError = false
                    )
                }

                else -> {
                    state
                }
            }
        }

        refreshLivePreview()
    }

    // ---------------------------------------------------------------------
    // Evaluation
    // ---------------------------------------------------------------------

    /**
     * Evaluates silently as the user types,
     * so the result line updates live.
     */
    private fun refreshLivePreview() {

        val state = _uiState.value

        // Empty expression.
        if (state.expression.isBlank()) {

            _uiState.update {
                it.copy(result = "0")
            }

            return
        }

        when (
            val outcome =
                CalculatorEngine.evaluate(
                    state.expression,
                    state.angleMode
                )
        ) {

            is EngineResult.Success -> {

                _uiState.update {

                    it.copy(
                        result =
                            CalculatorEngine.formatResult(
                                outcome.value
                            )
                    )
                }
            }

            is EngineResult.Error -> {

                // Do not show an error while the user is
                // still typing an incomplete expression.
            }
        }
    }

    /**
     * Evaluates the expression when the user presses "=".
     */
    private fun evaluateFinal() {

        val state = _uiState.value

        if (state.expression.isBlank()) {
            return
        }

        when (
            val outcome =
                CalculatorEngine.evaluate(
                    state.expression,
                    state.angleMode
                )
        ) {

            is EngineResult.Success -> {

                val formatted =
                    CalculatorEngine.formatResult(
                        outcome.value
                    )

                val newHistory =
                    (
                            state.history +
                                    HistoryItem(
                                        state.expression,
                                        formatted
                                    )
                            )
                        .takeLast(MAX_HISTORY_SIZE)

                _uiState.update {

                    it.copy(
                        result = formatted,
                        isError = false,
                        isResultFinal = true,
                        history = newHistory
                    )
                }
            }

            is EngineResult.Error -> {

                _uiState.update {

                    it.copy(
                        result = outcome.message,
                        isError = true,
                        isResultFinal = true
                    )
                }
            }
        }
    }

    /**
     * Selects a previous calculation from history.
     */
    private fun selectHistoryItem(
        item: HistoryItem
    ) {

        _uiState.update {

            it.copy(
                expression = item.expression,
                result = item.result,
                isError = false,
                isResultFinal = true,
                isHistoryVisible = false
            )
        }
    }
}