package com.example.pyxis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pyxis.model.ContainerEntity
import com.example.pyxis.model.LocationEntity
import com.example.pyxis.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Long,
    viewModel: InventoryViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit
) {
    LaunchedEffect(itemId) { viewModel.loadItem(itemId) }

    val state by viewModel.itemDetailUiState.collectAsState()
    val allLocations by viewModel.locationUiState.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    // Edit state
    var editName by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editLocationId by remember { mutableStateOf<Long?>(null) }
    var editContainerId by remember { mutableStateOf<Long?>(null) }
    var availableContainers by remember { mutableStateOf<List<ContainerEntity>>(emptyList()) }

    // Sync edit fields when item loads
    LaunchedEffect(state.item) {
        state.item?.let {
            editName = it.name
            editDescription = it.description
            editLocationId = it.locationId
            editContainerId = it.containerId
        }
    }

    LaunchedEffect(editLocationId) {
        editContainerId = null
        availableContainers = editLocationId?.let { viewModel.getContainersForLocation(it) } ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.item?.name ?: "Item") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isEditing = !isEditing
                        // Reset on cancel
                        if (!isEditing) {
                            state.item?.let {
                                editName = it.name
                                editDescription = it.description
                                editLocationId = it.locationId
                                editContainerId = it.containerId
                            }
                        }
                    }) {
                        Icon(
                            if (isEditing) Icons.Default.ArrowBack else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Cancel edit" else "Edit"
                        )
                    }
                    IconButton(onClick = { showDelete = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.item == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Item not found.")
                }
            }
            isEditing -> {
                ItemEditContent(
                    editName = editName,
                    editDescription = editDescription,
                    editLocationId = editLocationId,
                    editContainerId = editContainerId,
                    locations = allLocations.locations,
                    availableContainers = availableContainers,
                    onNameChange = { editName = it },
                    onDescriptionChange = { editDescription = it },
                    onLocationChange = { editLocationId = it },
                    onContainerChange = { editContainerId = it },
                    onSave = {
                        val item = state.item!!
                        val locId = editLocationId ?: item.locationId
                        viewModel.updateItem(item, editName, editDescription)
                        viewModel.moveItem(item, locId, editContainerId)
                        isEditing = false
                    },
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                ItemViewContent(
                    item = state.item!!,
                    breadcrumb = state.breadcrumb,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    if (showDelete) {
        com.example.pyxis.ui.components.ConfirmDeleteDialog(
            title = "Delete \"${state.item?.name}\"?",
            message = "This item will be permanently removed.",
            onConfirm = {
                state.item?.let { viewModel.deleteItem(it) }
                showDelete = false
                onDeleted()
            },
            onDismiss = { showDelete = false }
        )
    }
}

@Composable
private fun ItemViewContent(
    item: com.example.pyxis.model.ItemEntity,
    breadcrumb: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Breadcrumb location badge
        if (breadcrumb.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Location",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        breadcrumb,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Description
        if (item.description.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Description",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(item.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            Text(
                "No description.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemEditContent(
    editName: String,
    editDescription: String,
    editLocationId: Long?,
    editContainerId: Long?,
    locations: List<LocationEntity>,
    availableContainers: List<ContainerEntity>,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onLocationChange: (Long) -> Unit,
    onContainerChange: (Long?) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var locationExpanded by remember { mutableStateOf(false) }
    var containerExpanded by remember { mutableStateOf(false) }
    val selectedLocation = locations.firstOrNull { it.id == editLocationId }
    val selectedContainer = availableContainers.firstOrNull { it.id == editContainerId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = editName,
            onValueChange = onNameChange,
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = editDescription,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        // Location picker
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
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = locationExpanded,
                onDismissRequest = { locationExpanded = false }
            ) {
                locations.forEach { loc ->
                    DropdownMenuItem(
                        text = { Text(loc.name) },
                        onClick = { onLocationChange(loc.id); locationExpanded = false }
                    )
                }
            }
        }

        // Container picker
        ExposedDropdownMenuBox(
            expanded = containerExpanded,
            onExpandedChange = { containerExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedContainer?.name ?: "No container (directly in room)",
                onValueChange = {},
                readOnly = true,
                label = { Text("Container (optional)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(containerExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = containerExpanded,
                onDismissRequest = { containerExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("No container (directly in room)") },
                    onClick = { onContainerChange(null); containerExpanded = false }
                )
                availableContainers.forEach { container ->
                    DropdownMenuItem(
                        text = { Text(container.name) },
                        onClick = { onContainerChange(container.id); containerExpanded = false }
                    )
                }
            }
        }

        if (editContainerId == null && editLocationId != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    "💡 Tip: Note where in the room this item is in the description.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Button(
            onClick = onSave,
            enabled = editName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save changes")
        }
    }
}