package com.example.pyxis.util

import androidx.compose.ui.graphics.Color

data class GradientPreset(
    val key: String,
    val label: String,
    val topColor: Color,
    val bottomColor: Color
)

/**
 * 8 gradient presets. topColor is the lighter/top face,
 * bottomColor is the darker/bottom face — matching the 3-face SVG background approach.
 * These are stored by key string in the DB so adding more later is non-breaking.
 */
object GradientPresets {

    val all = listOf(
        GradientPreset("PRESET_1", "Ember",     Color(0xFFEF745C), Color(0xFF34073D)),
        GradientPreset("PRESET_2", "Ocean",     Color(0xFF4FC3F7), Color(0xFF0D1B6E)),
        GradientPreset("PRESET_3", "Forest",    Color(0xFF66BB6A), Color(0xFF1B3A1F)),
        GradientPreset("PRESET_4", "Violet",    Color(0xFFAB7BFF), Color(0xFF1A0040)),
        GradientPreset("PRESET_5", "Copper",    Color(0xFFFFB347), Color(0xFF4A1500)),
        GradientPreset("PRESET_6", "Arctic",    Color(0xFFB2EBF2), Color(0xFF1A3A4A)),
        GradientPreset("PRESET_7", "Rose",      Color(0xFFF48FB1), Color(0xFF3E0020)),
        GradientPreset("PRESET_8", "Graphite",  Color(0xFF9E9E9E), Color(0xFF1C1C1C)),
    )

    private val byKey = all.associateBy { it.key }

    fun fromKey(key: String): GradientPreset = byKey[key] ?: all.first()
}