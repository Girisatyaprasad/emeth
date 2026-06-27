package com.emeth.kernel.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    interactive: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(if (interactive) 24.dp else 8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = if (interactive) 0.78f else 0.68f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.32f)),
        shadowElevation = if (interactive) 10.dp else 2.dp,
        tonalElevation = 0.dp
    ) {
        Box(content = content)
    }
}
