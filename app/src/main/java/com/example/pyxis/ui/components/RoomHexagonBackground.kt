package com.example.pyxis.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.example.pyxis.R
import com.example.pyxis.util.GradientPreset

/**
 * Draws the custom faceted hexagon background with a true linear gradient.
 *
 * Strategy — two-layer compositing with BlendMode.SrcIn:
 *
 * Layer 1 (bottom): the ic_hex_background vector drawn normally (its own dark fill
 *   colours become the base — but we only care about its alpha/shape mask).
 * Layer 2 (top): a full-rect linear gradient painted over it using BlendMode.SrcIn,
 *   which keeps gradient pixels ONLY where the layer below is opaque.
 *
 * This requires an offscreen compositing layer (graphicsLayer with compositingStrategy
 * = ModulateAlpha ensures the BlendMode operates on the local layer, not the screen).
 */
@Composable
fun RoomHexagonBackground(
    preset: GradientPreset,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                // Force an offscreen layer so BlendMode.SrcIn works correctly
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithContent {
                // Draw the hex shape (creates the alpha mask)
                drawContent()

                // Paint gradient over it — SrcIn clips to the shape's opaque pixels
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(preset.topColor, preset.bottomColor),
                        start = Offset(size.toPx() / 2f, 0f),
                        end = Offset(size.toPx() / 2f, size.toPx())
                    ),
                    blendMode = BlendMode.SrcIn
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // The hex SVG drawn here becomes the mask for the gradient above
        Image(
            painter = painterResource(R.drawable.ic_hex_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}