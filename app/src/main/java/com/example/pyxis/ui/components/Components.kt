package com.example.pyxis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pyxis.model.CategoryEntity
import com.example.pyxis.model.ContainerEntity
import com.example.pyxis.model.LocationEntity
import com.example.pyxis.model.RoomIconType
import com.example.pyxis.util.GradientPreset
import com.example.pyxis.util.GradientPresets

// ── Empty state ────────────────────────────────────────────────────────────────

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Confirm delete dialog ──────────────────────────────────────────────────────

@Composable
fun ConfirmDeleteDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ── 3-dot context menu ─────────────────────────────────────────────────────────

@Composable
fun ThreeDotsMenu(onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = { onEdit(); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                onClick = { onDelete(); expanded = false }
            )
        }
    }
}

// ── Add / Edit Location dialog ─────────────────────────────────────────────────

@Composable
fun LocationDialog(
    title: String,
    initialName: String = "",
    initialIconType: RoomIconType = RoomIconType.DEFAULT,
    initialGradientPreset: String = "PRESET_1",
    onConfirm: (name: String, iconType: RoomIconType, gradientPreset: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedIcon by remember { mutableStateOf(initialIconType) }
    var selectedGradient by remember { mutableStateOf(initialGradientPreset) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Room name") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Icon type picker — two rows of chips so nothing overflows
                item {
                    Text("Room type", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    // Row 1: Bedroom, Storage, Basement
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(RoomIconType.BEDROOM, RoomIconType.STORAGE, RoomIconType.BASEMENT)
                            .forEach { iconType ->
                                FilterChip(
                                    selected = selectedIcon == iconType,
                                    onClick = { selectedIcon = iconType },
                                    label = {
                                        Text(
                                            iconType.name.lowercase().replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                            }
                    }
                    Spacer(Modifier.height(4.dp))
                    // Row 2: Office, Default
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(RoomIconType.OFFICE, RoomIconType.DEFAULT)
                            .forEach { iconType ->
                                FilterChip(
                                    selected = selectedIcon == iconType,
                                    onClick = { selectedIcon = iconType },
                                    label = {
                                        Text(
                                            iconType.name.lowercase().replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                            }
                    }
                }

                // Gradient picker
                item {
                    Text("Colour", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(GradientPresets.all) { preset ->
                            GradientSwatch(
                                preset = preset,
                                selected = selectedGradient == preset.key,
                                onClick = { selectedGradient = preset.key }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name, selectedIcon, selectedGradient) },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun GradientSwatch(preset: GradientPreset, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(listOf(preset.topColor, preset.bottomColor))
            )
            .then(
                if (selected) Modifier.border(3.dp, Color.White, CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick)
    )
}

// ── Add / Edit Container dialog ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerDialog(
    title: String,
    initialName: String = "",
    initialDescription: String = "",
    initialLocationId: Long? = null,
    initialCategoryId: Long? = null,
    locations: List<LocationEntity> = emptyList(),
    categories: List<CategoryEntity> = emptyList(),
    onConfirm: (name: String, description: String, locationId: Long?, categoryId: Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    var selectedLocationId by remember { mutableStateOf(initialLocationId) }
    var selectedCategoryId by remember { mutableStateOf(initialCategoryId) }
    var locationExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val selectedLocation = locations.firstOrNull { it.id == selectedLocationId }
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Container name") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = description, onValueChange = { description = it },
                        label = { Text("Description (e.g. top shelf, left side)") },
                        minLines = 2, maxLines = 3, modifier = Modifier.fillMaxWidth()
                    )
                }
                // Location picker — only shown when locations are provided (edit mode)
                if (locations.isNotEmpty()) {
                    item {
                        ExposedDropdownMenuBox(
                            expanded = locationExpanded,
                            onExpandedChange = { locationExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedLocation?.name ?: "Select room",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Room") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(locationExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = locationExpanded,
                                onDismissRequest = { locationExpanded = false }
                            ) {
                                locations.forEach { loc ->
                                    DropdownMenuItem(
                                        text = { Text(loc.name) },
                                        onClick = { selectedLocationId = loc.id; locationExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
                // Category picker — only shown when categories exist
                if (categories.isNotEmpty()) {
                    item {
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory?.name ?: "No category",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category (optional)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("No category") },
                                    onClick = { selectedCategoryId = null; categoryExpanded = false }
                                )
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = { selectedCategoryId = cat.id; categoryExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name, description, selectedLocationId, selectedCategoryId) },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ── Category dialog ────────────────────────────────────────────────────────────

@Composable
fun CategoryDialog(
    title: String,
    initialName: String = "",
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it },
                label = { Text("Category name") }, singleLine = true,
                modifier = Modifier.fillMaxWidth())
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ── Add Item dialog ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    locations: List<LocationEntity>,
    categories: List<CategoryEntity>,
    getContainersForLocation: suspend (Long) -> List<ContainerEntity>,
    defaultLocationId: Long? = null,
    defaultContainerId: Long? = null,
    onConfirm: (name: String, description: String, locationId: Long, containerId: Long?, categoryId: Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedLocationId by remember { mutableStateOf(defaultLocationId ?: locations.firstOrNull()?.id) }
    var selectedContainerId by remember { mutableStateOf(defaultContainerId) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var availableContainers by remember { mutableStateOf<List<ContainerEntity>>(emptyList()) }
    var locationExpanded by remember { mutableStateOf(false) }
    var containerExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedLocationId) {
        availableContainers = selectedLocationId?.let { getContainersForLocation(it) } ?: emptyList()
        if (selectedLocationId != defaultLocationId) selectedContainerId = null
    }

    val isDirectlyInRoom = selectedContainerId == null
    val selectedLocation = locations.firstOrNull { it.id == selectedLocationId }
    val selectedContainer = availableContainers.firstOrNull { it.id == selectedContainerId }
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                item {
                    OutlinedTextField(value = name, onValueChange = { name = it },
                        label = { Text("Item name") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = description, onValueChange = { description = it },
                        label = { Text("Description") }, minLines = 2, maxLines = 3,
                        modifier = Modifier.fillMaxWidth())
                }
                // Location picker
                item {
                    ExposedDropdownMenuBox(expanded = locationExpanded, onExpandedChange = { locationExpanded = it }) {
                        OutlinedTextField(
                            value = selectedLocation?.name ?: "Select location", onValueChange = {},
                            readOnly = true, label = { Text("Location (room)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(locationExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor())
                        ExposedDropdownMenu(expanded = locationExpanded, onDismissRequest = { locationExpanded = false }) {
                            locations.forEach { loc ->
                                DropdownMenuItem(text = { Text(loc.name) },
                                    onClick = { selectedLocationId = loc.id; locationExpanded = false })
                            }
                        }
                    }
                }
                // Container picker
                item {
                    ExposedDropdownMenuBox(expanded = containerExpanded, onExpandedChange = { containerExpanded = it }) {
                        OutlinedTextField(
                            value = selectedContainer?.name ?: "No container (directly in room)", onValueChange = {},
                            readOnly = true, label = { Text("Container (optional)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(containerExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor())
                        ExposedDropdownMenu(expanded = containerExpanded, onDismissRequest = { containerExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("No container (directly in room)") },
                                onClick = { selectedContainerId = null; containerExpanded = false })
                            availableContainers.forEach { container ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(container.name)
                                            if (container.description.isNotBlank()) {
                                                Text(container.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    },
                                    onClick = { selectedContainerId = container.id; containerExpanded = false })
                            }
                        }
                    }
                }
                // Category picker
                if (categories.isNotEmpty()) {
                    item {
                        ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                            OutlinedTextField(
                                value = selectedCategory?.name ?: "No category", onValueChange = {},
                                readOnly = true, label = { Text("Category (optional)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                                DropdownMenuItem(text = { Text("No category") },
                                    onClick = { selectedCategoryId = null; categoryExpanded = false })
                                categories.forEach { cat ->
                                    DropdownMenuItem(text = { Text(cat.name) },
                                        onClick = { selectedCategoryId = cat.id; categoryExpanded = false })
                                }
                            }
                        }
                    }
                }
                // Hint when directly in room
                if (isDirectlyInRoom && selectedLocationId != null) {
                    item {
                        Card(colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                            Text("💡 Tip: Since this item isn't in a container, consider noting " +
                                    "where in the room it is in the description.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val locId = selectedLocationId
                    if (name.isNotBlank() && locId != null)
                        onConfirm(name, description, locId, selectedContainerId, selectedCategoryId)
                },
                enabled = name.isNotBlank() && selectedLocationId != null
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ── Category pill ──────────────────────────────────────────────────────────────

@Composable
fun CategoryPill(name: String, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}