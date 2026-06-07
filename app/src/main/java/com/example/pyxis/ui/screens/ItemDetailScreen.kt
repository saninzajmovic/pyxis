package com.example.pyxis.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pyxis.model.ContainerEntity
import com.example.pyxis.model.LocationEntity
import com.example.pyxis.ui.components.ConfirmDeleteDialog
import com.example.pyxis.viewmodel.BreadcrumbStep
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
    val categoryState by viewModel.categoryUiState.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    // Edit fields
    var editName by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editLocationId by remember { mutableStateOf<Long?>(null) }
    var editContainerId by remember { mutableStateOf<Long?>(null) }
    var editCategoryId by remember { mutableStateOf<Long?>(null) }
    var availableContainers by remember { mutableStateOf<List<ContainerEntity>>(emptyList()) }
    // Tracks the location at the time the item loaded — so we only reset
    // containerId when the user actively picks a DIFFERENT location, not on first load
    var initialLocationId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(state.item) {
        state.item?.let {
            editName = it.name
            editDescription = it.description
            editLocationId = it.locationId
            editContainerId = it.containerId
            editCategoryId = it.categoryId
            initialLocationId = it.locationId
            availableContainers = viewModel.getContainersForLocation(it.locationId)
        }
    }

    LaunchedEffect(editLocationId) {
        val locId = editLocationId ?: return@LaunchedEffect
        availableContainers = viewModel.getContainersForLocation(locId)
        // Only clear the container if the user changed to a different location
        if (locId != initialLocationId) {
            editContainerId = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.item?.name ?: "Item") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditing) {
                            // Cancel edit — restore original values
                            state.item?.let {
                                editName = it.name
                                editDescription = it.description
                                editLocationId = it.locationId
                                editContainerId = it.containerId
                                editCategoryId = it.categoryId
                            }
                            isEditing = false
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            if (isEditing) Icons.AutoMirrored.Filled.ArrowBack
                            else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isEditing) "Cancel" else "Back"
                        )
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                    IconButton(onClick = { showDelete = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error)
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
                    editCategoryId = editCategoryId,
                    locations = allLocations.locations,
                    availableContainers = availableContainers,
                    categories = categoryState.categories,
                    onNameChange = { editName = it },
                    onDescriptionChange = { editDescription = it },
                    onLocationChange = { editLocationId = it },
                    onContainerChange = { editContainerId = it },
                    onCategoryChange = { editCategoryId = it },
                    onSave = {
                        val item = state.item!!
                        val locId = editLocationId ?: item.locationId
                        viewModel.saveItem(
                            item = item,
                            name = editName,
                            description = editDescription,
                            categoryId = editCategoryId,
                            newLocationId = locId,
                            newContainerId = editContainerId
                        )
                        isEditing = false
                    },
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                // Resolve category name directly from categoryState — avoids
                // depending on the async searchItems breadcrumb resolution
                val resolvedCategoryName = state.item?.categoryId?.let { cid ->
                    categoryState.categories.firstOrNull { it.id == cid }?.name
                }
                ItemViewContent(
                    item = state.item!!,
                    breadcrumb = state.breadcrumb,
                    breadcrumbSteps = state.breadcrumbSteps,
                    categoryName = resolvedCategoryName,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    if (showDelete) {
        ConfirmDeleteDialog(
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

// ── View mode ──────────────────────────────────────────────────────────────────

@Composable
private fun ItemViewContent(
    item: com.example.pyxis.model.ItemEntity,
    breadcrumb: String,
    breadcrumbSteps: List<com.example.pyxis.viewmodel.BreadcrumbStep>,
    categoryName: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Expandable location breadcrumb card ────────────────────────────
        if (breadcrumb.isNotBlank()) {
            ExpandableBreadcrumbCard(
                breadcrumb = breadcrumb,
                steps = breadcrumbSteps
            )
        }

        // Category pill
        if (categoryName != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Category: ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                com.example.pyxis.ui.components.CategoryPill(name = categoryName)
            }
        }

        // Description
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Description",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (item.description.isNotBlank()) item.description else "No description.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// ── Expandable breadcrumb card ─────────────────────────────────────────────────

@Composable
private fun ExpandableBreadcrumbCard(
    breadcrumb: String,
    steps: List<com.example.pyxis.viewmodel.BreadcrumbStep>
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                Icon(
                    imageVector = if (expanded)
                        androidx.compose.material.icons.Icons.Default.KeyboardArrowUp
                    else
                        androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Expanded detail — each step with its description
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    Spacer(Modifier.height(4.dp))
                    steps.forEachIndexed { index, step ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Indent each level
                            if (index > 0) {
                                Spacer(Modifier.width((index * 12).dp))
                                Text(
                                    "↳ ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                )
                            }
                            Column {
                                Text(
                                    text = if (step.isRoom) "📍 ${step.name}" else step.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = if (step.isRoom)
                                        androidx.compose.ui.text.font.FontWeight.SemiBold
                                    else
                                        androidx.compose.ui.text.font.FontWeight.Normal
                                )
                                if (step.description.isNotBlank()) {
                                    Text(
                                        text = step.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Edit mode ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemEditContent(
    editName: String,
    editDescription: String,
    editLocationId: Long?,
    editContainerId: Long?,
    editCategoryId: Long?,
    locations: List<LocationEntity>,
    availableContainers: List<ContainerEntity>,
    categories: List<com.example.pyxis.model.CategoryEntity>,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onLocationChange: (Long) -> Unit,
    onContainerChange: (Long?) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var locationExpanded by remember { mutableStateOf(false) }
    var containerExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val selectedLocation = locations.firstOrNull { it.id == editLocationId }
    val selectedContainer = availableContainers.firstOrNull { it.id == editContainerId }
    val selectedCategory = categories.firstOrNull { it.id == editCategoryId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(value = editName, onValueChange = onNameChange,
            label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = editDescription, onValueChange = onDescriptionChange,
            label = { Text("Description") }, minLines = 3, modifier = Modifier.fillMaxWidth())

        // Location picker
        ExposedDropdownMenuBox(expanded = locationExpanded, onExpandedChange = { locationExpanded = it }) {
            OutlinedTextField(
                value = selectedLocation?.name ?: "Select location", onValueChange = {},
                readOnly = true, label = { Text("Location (room)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(locationExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor())
            ExposedDropdownMenu(expanded = locationExpanded, onDismissRequest = { locationExpanded = false }) {
                locations.forEach { loc ->
                    DropdownMenuItem(text = { Text(loc.name) },
                        onClick = { onLocationChange(loc.id); locationExpanded = false })
                }
            }
        }

        // Container picker
        ExposedDropdownMenuBox(expanded = containerExpanded, onExpandedChange = { containerExpanded = it }) {
            OutlinedTextField(
                value = selectedContainer?.name ?: "No container (directly in room)", onValueChange = {},
                readOnly = true, label = { Text("Container (optional)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(containerExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor())
            ExposedDropdownMenu(expanded = containerExpanded, onDismissRequest = { containerExpanded = false }) {
                DropdownMenuItem(text = { Text("No container (directly in room)") },
                    onClick = { onContainerChange(null); containerExpanded = false })
                availableContainers.forEach { c ->
                    DropdownMenuItem(text = { Text(c.name) },
                        onClick = { onContainerChange(c.id); containerExpanded = false })
                }
            }
        }

        // Category picker
        if (categories.isNotEmpty()) {
            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "No category", onValueChange = {},
                    readOnly = true, label = { Text("Category (optional)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    DropdownMenuItem(text = { Text("No category") },
                        onClick = { onCategoryChange(null); categoryExpanded = false })
                    categories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat.name) },
                            onClick = { onCategoryChange(cat.id); categoryExpanded = false })
                    }
                }
            }
        }

        if (editContainerId == null && editLocationId != null) {
            Card(colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Text("💡 Tip: Note where in the room this item is in the description.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp))
            }
        }

        Button(onClick = onSave, enabled = editName.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text("Save changes")
        }
    }
}