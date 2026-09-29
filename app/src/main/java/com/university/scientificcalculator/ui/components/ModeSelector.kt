package com.university.scientificcalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.university.scientificcalculator.model.AngleMode

/**
 * A compact two-way switch clearly showing whether trig functions are
 * currently evaluated in degrees or radians. Uses both color AND a filled
 * background (not color alone) to mark the active mode, for accessibility.
 */
@Composable
fun ModeSelector(
    angleMode: AngleMode,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
            .padding(4.dp)
    ) {
        androidx.compose.foundation.layout.Row {
            ModeChip(
                label = "DEG",
                selected = angleMode == AngleMode.DEG,
                onClick = { if (angleMode != AngleMode.DEG) onToggle() }
            )
            ModeChip(
                label = "RAD",
                selected = angleMode == AngleMode.RAD,
                onClick = { if (angleMode != AngleMode.RAD) onToggle() }
            )
        }
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .selectable(selected = selected, onClick = onClick)
            .background(background, RoundedCornerShape(50))
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .semantics { contentDescription = "$label angle mode${if (selected) ", selected" else ""}" }
    ) {
        Text(
            text = label,
            color = content,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
