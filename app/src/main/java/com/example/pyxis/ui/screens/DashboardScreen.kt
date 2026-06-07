package com.example.pyxis.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.example.pyxis.R
import com.example.pyxis.model.RoomIconType
import com.example.pyxis.ui.components.*
import com.example.pyxis.viewmodel.InventoryViewModel
import com.example.pyxis.viewmodel.SearchUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: InventoryViewModel,
    onLocationClick: (locationId: Long) -> Unit,
    onItemClick: (itemId: Long) -> Unit,
    onCategoriesClick: () -> Unit
) {
    val locationState by viewModel.locationUiState.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val categoryState by viewModel.categoryUiState.collectAsState()

    // Modal visibility
    var showDashboardModal by remember { mutableStateOf(false) }

    // Dialog visibility
    var showAddLocation by remember { mutableStateOf(false) }
    var showAddItem by remember { mutableStateOf(false) }
    var showAddContainer by remember { mutableStateOf(false) }
    var showAddCategory by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PYXIS",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            PyxisFab(onClick = { showDashboardModal = true })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Search bar ─────────────────────────────────────────────────
            OutlinedTextField(
                value = searchState.query,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Find an item…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    AnimatedVisibility(visible = searchState.query.isNotBlank() || searchState.isSearchBarActive) {
                        IconButton(onClick = viewModel::clearSearch) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .onFocusChanged { focusState ->
                        viewModel.setSearchBarActive(focusState.isFocused)
                    }
            )

            // ── Category chips — shown as soon as search bar is active ─────
            val showChips = searchState.isSearchBarActive || searchState.query.isNotBlank()
                    || searchState.filterCategoryId != null
            if (showChips && categoryState.categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = searchState.filterCategoryId == null,
                            onClick = { viewModel.setSearchCategoryFilter(null) },
                            label = { Text("All") }
                        )
                    }
                    items(categoryState.categories) { cat ->
                        FilterChip(
                            selected = searchState.filterCategoryId == cat.id,
                            onClick = {
                                viewModel.setSearchCategoryFilter(
                                    if (searchState.filterCategoryId == cat.id) null else cat.id
                                )
                            },
                            label = { Text(cat.name) }
                        )
                    }
                }
            }

            val isSearchActive = searchState.query.isNotBlank() || searchState.filterCategoryId != null || searchState.showAll

            if (isSearchActive) {
                // ── Search results ─────────────────────────────────────────
                SearchResultsList(
                    state = searchState,
                    onItemClick = onItemClick
                )
            } else {
                // ── Room grid ──────────────────────────────────────────────
                if (locationState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (locationState.locations.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Your house is empty!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        Image(
                            painter = painterResource(R.drawable.ic_pyxis_logo),
                            contentDescription = "Pyxis logo",
                            modifier = Modifier
                                .size(120.dp)
                                .alpha(0.6f)
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = "Tap + to add your first room.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(locationState.locations, key = { it.id }) { location ->
                            var showEdit by remember { mutableStateOf(false) }
                            var showDelete by remember { mutableStateOf(false) }

                            Box {
                                RoomCard(
                                    location = location,
                                    onClick = { onLocationClick(location.id) }
                                )
                                // 3-dot menu anchored to top-end of card
                                Box(
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    ThreeDotsMenu(
                                        onEdit = { showEdit = true },
                                        onDelete = { showDelete = true }
                                    )
                                }
                            }

                            if (showEdit) {
                                LocationDialog(
                                    title = "Edit room",
                                    initialName = location.name,
                                    initialIconType = runCatching {
                                        RoomIconType.valueOf(location.iconType)
                                    }.getOrDefault(RoomIconType.DEFAULT),
                                    initialGradientPreset = location.gradientPreset,
                                    onConfirm = { name, icon, gradient ->
                                        viewModel.updateLocation(location, name, icon, gradient)
                                        showEdit = false
                                    },
                                    onDismiss = { showEdit = false }
                                )
                            }
                            if (showDelete) {
                                ConfirmDeleteDialog(
                                    title = "Delete \"${location.name}\"?",
                                    message = "This will also delete all containers and items inside it.",
                                    onConfirm = {
                                        viewModel.deleteLocation(location)
                                        showDelete = false
                                    },
                                    onDismiss = { showDelete = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Dashboard add modal ────────────────────────────────────────────────
    if (showDashboardModal) {
        DashboardAddModal(
            onCategory  = { showDashboardModal = false; showAddCategory = true },
            onItem      = { showDashboardModal = false; showAddItem = true },
            onContainer = { showDashboardModal = false; showAddContainer = true },
            onRoom      = { showDashboardModal = false; showAddLocation = true },
            onDismiss   = { showDashboardModal = false }
        )
    }

    // ── Add dialogs ────────────────────────────────────────────────────────
    if (showAddLocation) {
        LocationDialog(
            title = "Add room",
            onConfirm = { name, icon, gradient ->
                viewModel.addLocation(name, icon, gradient)
                showAddLocation = false
            },
            onDismiss = { showAddLocation = false }
        )
    }

    if (showAddCategory) {
        CategoryDialog(
            title = "Add category",
            onConfirm = { name ->
                viewModel.addCategory(name)
                showAddCategory = false
            },
            onDismiss = { showAddCategory = false }
        )
    }

    if (showAddItem) {
        AddItemDialog(
            locations = locationState.locations,
            categories = categoryState.categories,
            getContainersForLocation = viewModel::getContainersForLocation,
            onConfirm = { name, desc, locId, containerId, categoryId ->
                if (containerId != null) {
                    viewModel.addItemToContainer(locId, containerId, name, desc, categoryId)
                } else {
                    viewModel.addItemToLocation(locId, name, desc, categoryId)
                }
                showAddItem = false
            },
            onDismiss = { showAddItem = false }
        )
    }

    if (showAddContainer) {
        DashboardAddContainerDialog(
            locations = locationState.locations,
            categories = categoryState.categories,
            getContainersForLocation = viewModel::getContainersForLocation,
            onConfirm = { name, desc, locationId, parentContainerId, catId ->
                viewModel.addContainer(locationId, name, desc, parentContainerId, catId)
                showAddContainer = false
            },
            onDismiss = { showAddContainer = false }
        )
    }
}

// ── Dashboard-level add container dialog (needs location picker) ───────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardAddContainerDialog(
    locations: List<com.example.pyxis.model.LocationEntity>,
    categories: List<com.example.pyxis.model.CategoryEntity>,
    getContainersForLocation: suspend (Long) -> List<com.example.pyxis.model.ContainerEntity>,
    onConfirm: (name: String, description: String, locationId: Long, parentContainerId: Long?, categoryId: Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedLocationId by remember { mutableStateOf(locations.firstOrNull()?.id) }
    var selectedParentId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var availableContainers by remember { mutableStateOf<List<com.example.pyxis.model.ContainerEntity>>(emptyList()) }
    var locationExpanded by remember { mutableStateOf(false) }
    var parentExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedLocationId) {
        selectedParentId = null
        availableContainers = selectedLocationId?.let { getContainersForLocation(it) } ?: emptyList()
    }

    val selectedLocation = locations.firstOrNull { it.id == selectedLocationId }
    val selectedParent = availableContainers.firstOrNull { it.id == selectedParentId }
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add container") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(value = name, onValueChange = { name = it },
                        label = { Text("Container name") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = description, onValueChange = { description = it },
                        label = { Text("Description (e.g. top shelf)") },
                        minLines = 2, maxLines = 3, modifier = Modifier.fillMaxWidth())
                }
                item {
                    ExposedDropdownMenuBox(expanded = locationExpanded, onExpandedChange = { locationExpanded = it }) {
                        OutlinedTextField(
                            value = selectedLocation?.name ?: "Select room", onValueChange = {},
                            readOnly = true, label = { Text("Room") },
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
                if (availableContainers.isNotEmpty()) {
                    item {
                        ExposedDropdownMenuBox(expanded = parentExpanded, onExpandedChange = { parentExpanded = it }) {
                            OutlinedTextField(
                                value = selectedParent?.name ?: "Top-level (no parent container)", onValueChange = {},
                                readOnly = true, label = { Text("Parent container (optional)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(parentExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = parentExpanded, onDismissRequest = { parentExpanded = false }) {
                                DropdownMenuItem(text = { Text("Top-level (no parent)") },
                                    onClick = { selectedParentId = null; parentExpanded = false })
                                availableContainers.forEach { c ->
                                    DropdownMenuItem(text = { Text(c.name) },
                                        onClick = { selectedParentId = c.id; parentExpanded = false })
                                }
                            }
                        }
                    }
                }
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
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val locId = selectedLocationId
                    if (name.isNotBlank() && locId != null)
                        onConfirm(name, description, locId, selectedParentId, selectedCategoryId)
                },
                enabled = name.isNotBlank() && selectedLocationId != null
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ── Search results list ────────────────────────────────────────────────────────

@Composable
private fun SearchResultsList(
    state: SearchUiState,
    onItemClick: (Long) -> Unit
) {
    when {
        state.isSearching -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.results.isEmpty() -> {
            EmptyState("No items found for \"${state.query}\"")
        }
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.results, key = { it.item.id }) { result ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(result.item.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    result.item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (result.categoryName != null) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = {
                                            Text(
                                                result.categoryName,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                            Text(
                                result.breadcrumb,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (result.item.description.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    result.item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}