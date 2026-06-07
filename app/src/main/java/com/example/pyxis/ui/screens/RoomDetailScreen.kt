package com.example.pyxis.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pyxis.ui.components.*
import com.example.pyxis.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    locationId: Long,
    viewModel: InventoryViewModel,
    onContainerClick: (containerId: Long) -> Unit,
    onItemClick: (itemId: Long) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(locationId) { viewModel.selectLocation(locationId) }

    val state by viewModel.roomDetailUiState.collectAsState()
    val allLocations by viewModel.locationUiState.collectAsState()
    val categoryState by viewModel.categoryUiState.collectAsState()

    var showRoomModal by remember { mutableStateOf(false) }
    var showAddContainer by remember { mutableStateOf(false) }
    var showAddItem by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.location?.name ?: "Room",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            PyxisFab(onClick = { showRoomModal = true })
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val hasContent = state.containers.isNotEmpty() || state.directItems.isNotEmpty()

        if (!hasContent) {
            EmptyState(
                "This room is empty.\nTap + to add a container or item.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ── Containers ─────────────────────────────────────────────
                if (state.containers.isNotEmpty()) {
                    item { SectionHeader("Containers") }
                    items(state.containers, key = { "c-${it.id}" }) { container ->
                        var showEdit by remember { mutableStateOf(false) }
                        var showDelete by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onContainerClick(container.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(container.name, style = MaterialTheme.typography.titleMedium)
                                        container.categoryId?.let { cid ->
                                            categoryState.categories.firstOrNull { it.id == cid }?.let {
                                                CategoryPill(name = it.name)
                                            }
                                        }
                                    }
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
                                ThreeDotsMenu(
                                    onEdit = { showEdit = true },
                                    onDelete = { showDelete = true }
                                )
                            }
                        }

                        if (showEdit) {
                            ContainerDialog(
                                title = "Edit container",
                                initialName = container.name,
                                initialDescription = container.description,
                                initialLocationId = container.locationId,
                                initialCategoryId = container.categoryId,
                                locations = allLocations.locations,
                                categories = categoryState.categories,
                                onConfirm = { name, desc, locId, catId ->
                                    viewModel.updateContainer(container, name, desc, locId, catId)
                                    showEdit = false
                                },
                                onDismiss = { showEdit = false }
                            )
                        }
                        if (showDelete) {
                            ConfirmDeleteDialog(
                                title = "Delete \"${container.name}\"?",
                                message = "This will also delete all nested containers and items inside it.",
                                onConfirm = {
                                    viewModel.deleteContainer(container)
                                    showDelete = false
                                },
                                onDismiss = { showDelete = false }
                            )
                        }
                    }
                }

                // ── Direct items ───────────────────────────────────────────
                if (state.directItems.isNotEmpty()) {
                    item { SectionHeader("Items in this room (no container)") }
                    items(state.directItems, key = { "i-${it.id}" }) { item ->
                        val categoryName = item.categoryId?.let { cid ->
                            categoryState.categories.firstOrNull { it.id == cid }?.name
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(item.id) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (categoryName != null) {
                                        CategoryPill(
                                            name = categoryName,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                                if (item.description.isNotBlank()) {
                                    Text(
                                        item.description,
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

    // ── Room add modal ─────────────────────────────────────────────────────
    if (showRoomModal) {
        RoomAddModal(
            onItem      = { showRoomModal = false; showAddItem = true },
            onContainer = { showRoomModal = false; showAddContainer = true },
            onDismiss   = { showRoomModal = false }
        )
    }

    if (showAddContainer) {
        ContainerDialog(
            title = "Add container",
            initialLocationId = locationId,
            categories = categoryState.categories,
            onConfirm = { name, desc, _, catId ->
                viewModel.addContainer(locationId, name, desc, categoryId = catId)
                showAddContainer = false
            },
            onDismiss = { showAddContainer = false }
        )
    }

    if (showAddItem) {
        AddItemDialog(
            locations = allLocations.locations,
            categories = categoryState.categories,
            getContainersForLocation = viewModel::getContainersForLocation,
            defaultLocationId = locationId,
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
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}