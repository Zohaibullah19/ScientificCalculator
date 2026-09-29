package com.university.scientificcalculator.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shows the expression currently being typed on top, and the large live
 * result (or error message) below it -- the two most important pieces of
 * information on a scientific calculator.
 */
@Composable
fun DisplaySection(
    expression: String,
    result: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val expressionScroll = rememberScrollState()
    LaunchedEffect(expression) { expressionScroll.scrollTo(expressionScroll.maxValue) }

    val resultColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = expression.ifEmpty { " " },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(expressionScroll)
                .semantics { contentDescription = "Expression: ${expression.ifEmpty { "empty" }}" }
        )
        Text(
            text = result,
            style = MaterialTheme.typography.displayMedium,
            color = resultColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = if (result.length > 12) 34.sp else 48.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .semantics {
                    contentDescription = if (isError) "Error: $result" else "Result: $result"
                    liveRegion = LiveRegionMode.Polite
                }
        )
    }
}
