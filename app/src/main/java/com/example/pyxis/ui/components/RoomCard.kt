package com.example.pyxis.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pyxis.R
import com.example.pyxis.model.LocationEntity
import com.example.pyxis.model.RoomIconType
import com.example.pyxis.util.GradientPresets

@Composable
fun RoomCard(
    location: LocationEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 160.dp
) {
    val preset = GradientPresets.fromKey(location.gradientPreset)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(iconSize)
        ) {
            // Layer 1 — gradient-tinted hexagon background (real SVG asset)
            RoomHexagonBackground(
                preset = preset,
                size = iconSize
            )
            // Layer 2 — foreground room scene SVG, scaled to sit inside the hex
            Image(
                painter = painterResource(id = iconResFor(location.iconType)),
                contentDescription = location.name,
                modifier = Modifier.fillMaxSize(0.78f)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = location.name,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

fun iconResFor(iconTypeString: String): Int =
    when (runCatching { RoomIconType.valueOf(iconTypeString) }.getOrDefault(RoomIconType.DEFAULT)) {
        RoomIconType.BEDROOM  -> R.drawable.ic_room_bedroom
        RoomIconType.STORAGE  -> R.drawable.ic_room_storage
        RoomIconType.BASEMENT -> R.drawable.ic_room_basement
        RoomIconType.OFFICE   -> R.drawable.ic_room_office
        RoomIconType.DEFAULT  -> R.drawable.ic_room_default
    }