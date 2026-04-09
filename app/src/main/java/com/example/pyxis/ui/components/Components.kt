package com.example.pyxis.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pyxis.model.ContainerEntity
import com.example.pyxis.model.LocationEntity

// ── Empty state ────────────────────────────────────────────────────────────────

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Confirm delete dialog ──────────────────────────────────────────────────────

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Add / Edit Location dialog ─────────────────────────────────────────────────

@Composable
fun LocationDialog(
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Location name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Add / Edit Container dialog ────────────────────────────────────────────────

@Composable
fun ContainerDialog(
    title: String,
    initialName: String = "",
    initialDescription: String = "",
    onConfirm: (name: String, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Container name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (e.g. top shelf, left side)") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name, description) },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Add Item dialog (with location + container picker) ─────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    locations: List<LocationEntity>,
    getContainersForLocation: suspend (Long) -> List<ContainerEntity>,
    defaultLocationId: Long? = null,
    defaultContainerId: Long? = null,
    onConfirm: (name: String, description: String, locationId: Long, containerId: Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedLocationId by remember { mutableStateOf(defaultLocationId ?: locations.firstOrNull()?.id) }
    var selectedContainerId by remember { mutableStateOf(defaultContainerId) }
    var availableContainers by remember { mutableStateOf<List<ContainerEntity>>(emptyList()) }
    var locationExpanded by remember { mutableStateOf(false) }
    var containerExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Load containers whenever location changes
    LaunchedEffect(selectedLocationId) {
        availableContainers = selectedLocationId?.let { getContainersForLocation(it) } ?: emptyList()
        // Only reset the container selection if the location actually changed away from the default,
        // so that a pre-filled defaultContainerId is preserved on first load.
        if (selectedLocationId != defaultLocationId) {
            selectedContainerId = null
        }
    }

    val isDirectlyInRoom = selectedContainerId == null
    val selectedLocation = locations.firstOrNull { it.id == selectedLocationId }
    val selectedContainer = availableContainers.firstOrNull { it.id == selectedContainerId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Item name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                // Location picker
                item {
                    ExposedDropdownMenuBox(
                        expanded = locationExpanded,
                        onExpandedChange = { locationExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedLocation?.name ?: "Select location",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Location (room)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(locationExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = locationExpanded,
                            onDismissRequest = { locationExpanded = false }
                        ) {
                            locations.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text(loc.name) },
                                    onClick = {
                                        selectedLocationId = loc.id
                                        locationExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                // Container picker (optional)
                item {
                    ExposedDropdownMenuBox(
                        expanded = containerExpanded,
                        onExpandedChange = { if (availableContainers.isNotEmpty() || selectedContainerId != null) containerExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedContainer?.name ?: "No container (directly in room)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Container (optional)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(containerExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = containerExpanded,
                            onDismissRequest = { containerExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("No container (directly in room)") },
                                onClick = {
                                    selectedContainerId = null
                                    containerExpanded = false
                                }
                            )
                            availableContainers.forEach { container ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(container.name)
                                            if (container.description.isNotBlank()) {
                                                Text(
                                                    container.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedContainerId = container.id
                                        containerExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                // Hint when item is placed directly in room
                if (isDirectlyInRoom && selectedLocationId != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = "💡 Tip: Since this item isn't in a container, consider noting where in the room it is in the description (e.g. \"on the shelf above the door\").",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val locId = selectedLocationId
                    if (name.isNotBlank() && locId != null) {
                        onConfirm(name, description, locId, selectedContainerId)
                    }
                },
                enabled = name.isNotBlank() && selectedLocationId != null
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Swipe-to-action row (edit + delete icons on a list item) ───────────────────

@Composable
fun ItemRowActions(
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row {
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}