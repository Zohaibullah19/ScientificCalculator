package com.university.scientificcalculator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.university.scientificcalculator.viewmodel.CalculatorAction

private data class Key(
    val label: String,
    val role: ButtonRole,
    val action: CalculatorAction,
    val description: String,
    val fontSize: TextUnit = 20.sp
)

// -------------------------------------------------------------------------
// Scientific keypad
// -------------------------------------------------------------------------

private val scientificRows: List<List<Key>> = listOf(

    listOf(
        Key(
            "sin",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.Function("sin("),
            "Sine"
        ),
        Key(
            "cos",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.Function("cos("),
            "Cosine"
        ),
        Key(
            "tan",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.Function("tan("),
            "Tangent"
        ),
        Key(
            "asin",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.Function("asin("),
            "Inverse sine",
            15.sp
        ),
        Key(
            "acos",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.Function("acos("),
            "Inverse cosine",
            15.sp
        )
    ),

    listOf(
        Key(
            "atan",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.Function("atan("),
            "Inverse tangent",
            15.sp
        ),
        Key(
            "log",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.Function("log("),
            "Log base 10"
        ),
        Key(
            "ln",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.Function("ln("),
            "Natural logarithm"
        ),
        Key(
            "√",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.Function("√("),
            "Square root"
        ),
        Key(
            "x²",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.Square,
            "Square"
        )
    ),

    listOf(
        Key(
            "xʸ",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.Operator("^"),
            "Power"
        ),
        Key(
            "1/x",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.Reciprocal,
            "Reciprocal"
        ),
        Key(
            "|x|",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.Function("abs("),
            "Absolute value"
        ),
        Key(
            "x!",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.Factorial,
            "Factorial"
        ),
        Key(
            "π",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.Constant("π"),
            "Pi"
        )
    ),

    listOf(
        Key(
            "e",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.Constant("e"),
            "Euler's number"
        ),
        Key(
            "(",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.OpenParen,
            "Open parenthesis"
        ),
        Key(
            ")",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.CloseParen,
            "Close parenthesis"
        ),
        Key(
            "%",
            ButtonRole.SCIENTIFIC,
            CalculatorAction.Percent,
            "Percent"
        ),
        Key(
            "AC",
            ButtonRole.CONTROL,
            CalculatorAction.Clear,
            "All clear"
        )
    )
)

// -------------------------------------------------------------------------
// Numeric keypad
// -------------------------------------------------------------------------

private val numericRows: List<List<Key>> = listOf(

    listOf(
        Key(
            "⌫",
            ButtonRole.CONTROL,
            CalculatorAction.Delete,
            "Delete"
        ),
        Key(
            "7",
            ButtonRole.NUMBER,
            CalculatorAction.Digit("7"),
            "Seven"
        ),
        Key(
            "8",
            ButtonRole.NUMBER,
            CalculatorAction.Digit("8"),
            "Eight"
        ),
        Key(
            "9",
            ButtonRole.NUMBER,
            CalculatorAction.Digit("9"),
            "Nine"
        ),
        Key(
            "÷",
            ButtonRole.OPERATOR,
            CalculatorAction.Operator("÷"),
            "Divide"
        )
    ),

    listOf(
        Key(
            "+/-",
            ButtonRole.CONTROL,
            CalculatorAction.ToggleSign,
            "Toggle sign",
            15.sp
        ),
        Key(
            "4",
            ButtonRole.NUMBER,
            CalculatorAction.Digit("4"),
            "Four"
        ),
        Key(
            "5",
            ButtonRole.NUMBER,
            CalculatorAction.Digit("5"),
            "Five"
        ),
        Key(
            "6",
            ButtonRole.NUMBER,
            CalculatorAction.Digit("6"),
            "Six"
        ),
        Key(
            "×",
            ButtonRole.OPERATOR,
            CalculatorAction.Operator("×"),
            "Multiply"
        )
    ),

    listOf(
        Key(
            "C",
            ButtonRole.CONTROL,
            CalculatorAction.Clear,
            "Clear"
        ),
        Key(
            "1",
            ButtonRole.NUMBER,
            CalculatorAction.Digit("1"),
            "One"
        ),
        Key(
            "2",
            ButtonRole.NUMBER,
            CalculatorAction.Digit("2"),
            "Two"
        ),
        Key(
            "3",
            ButtonRole.NUMBER,
            CalculatorAction.Digit("3"),
            "Three"
        ),
        Key(
            "−",
            ButtonRole.OPERATOR,
            CalculatorAction.Operator("-"),
            "Subtract"
        )
    ),

    listOf(
        Key(
            ".",
            ButtonRole.NUMBER,
            CalculatorAction.Decimal,
            "Decimal point"
        ),
        Key(
            "0",
            ButtonRole.NUMBER,
            CalculatorAction.Digit("0"),
            "Zero"
        ),
        Key(
            "00",
            ButtonRole.NUMBER,
            CalculatorAction.Digit("00"),
            "Double zero"
        ),
        Key(
            "=",
            ButtonRole.EQUALS,
            CalculatorAction.Equals,
            "Equals"
        ),
        Key(
            "+",
            ButtonRole.OPERATOR,
            CalculatorAction.Operator("+"),
            "Add"
        )
    )
)

// -------------------------------------------------------------------------
// Keypad
// -------------------------------------------------------------------------

@Composable
fun Keypad(
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),

        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        // Scientific rows
        scientificRows.forEach { row ->

            KeyRow(
                row = row,
                onAction = onAction,
                rowHeight = 46.dp
            )
        }

        // Numeric rows
        numericRows.forEach { row ->

            KeyRow(
                row = row,
                onAction = onAction,
                rowHeight = 54.dp
            )
        }
    }
}

// -------------------------------------------------------------------------
// Individual row
// -------------------------------------------------------------------------

@Composable
private fun KeyRow(
    row: List<Key>,
    onAction: (CalculatorAction) -> Unit,
    rowHeight: Dp
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight),

        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        row.forEach { key ->

            if (key.label.isEmpty()) {

                Spacer(
                    modifier = Modifier.weight(1f)
                )

            } else {

                CalculatorButton(
                    label = key.label,
                    role = key.role,

                    onClick = {
                        onAction(key.action)
                    },

                    contentDescription = key.description,

                    fontSize = key.fontSize,

                    modifier = Modifier.weight(1f),

                    square = false
                )
            }
        }
    }
}