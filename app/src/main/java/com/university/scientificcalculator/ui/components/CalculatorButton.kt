package com.university.scientificcalculator.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Visual role of a calculator key; drives its color so meaning never relies on color alone. */
enum class ButtonRole {
    NUMBER,
    OPERATOR,
    SCIENTIFIC,
    CONTROL,
    EQUALS
}

/**
 * A single calculator key. Uses [Surface] directly (rather than a Material Button)
 * so we get full control over shape, ripple and minimum touch-target size while
 * keeping semantics (content description) accurate for screen readers.
 */
@Composable
fun CalculatorButton(
    label: String,
    role: ButtonRole,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    fontSize: androidx.compose.ui.unit.TextUnit = 20.sp,
    square: Boolean = true
) {
    val colors = calculatorButtonColors(role)

    Surface(
        onClick = onClick,
        modifier = modifier
            .then(if (square) Modifier.aspectRatio(1f) else Modifier)
            .semantics { contentDescription?.let { this.contentDescription = it } },
        shape = RoundedCornerShape(20.dp),
        color = colors.first,
        contentColor = colors.second,
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = fontSize,
                fontWeight = if (role == ButtonRole.EQUALS) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun calculatorButtonColors(role: ButtonRole): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    val scheme = MaterialTheme.colorScheme
    return when (role) {
        ButtonRole.NUMBER -> scheme.surface to scheme.onBackground
        ButtonRole.OPERATOR -> scheme.secondary to scheme.onSecondary
        ButtonRole.SCIENTIFIC -> scheme.surfaceVariant to scheme.onSurfaceVariant
        ButtonRole.CONTROL -> scheme.surfaceVariant to scheme.onSurfaceVariant
        ButtonRole.EQUALS -> scheme.primary to scheme.onPrimary
    }
}
