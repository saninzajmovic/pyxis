package com.example.pyxis.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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

    var showAddContainer by remember { mutableStateOf(false) }
    var showAddItem by remember { mutableStateOf(false) }

    // FAB expands into two choices
    var fabExpanded by remember { mutableStateOf(false) }

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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (fabExpanded) {
                    SmallFloatingActionButton(
                        onClick = { showAddItem = true; fabExpanded = false },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text("+ Item", modifier = Modifier.padding(horizontal = 8.dp))
                    }
                    SmallFloatingActionButton(
                        onClick = { showAddContainer = true; fabExpanded = false },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text("+ Container", modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
                FloatingActionButton(onClick = { fabExpanded = !fabExpanded }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
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
                // ── Containers section ─────────────────────────────────────
                if (state.containers.isNotEmpty()) {
                    item {
                        SectionHeader("Containers")
                    }
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
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = container.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (container.description.isNotBlank()) {
                                        Text(
                                            text = container.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                ItemRowActions(
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
                                onConfirm = { name, desc ->
                                    viewModel.updateContainer(container, name, desc)
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

                // ── Direct items section ───────────────────────────────────
                if (state.directItems.isNotEmpty()) {
                    item {
                        SectionHeader("Items in this room (no container)")
                    }
                    items(state.directItems, key = { "i-${it.id}" }) { item ->
                        var showDelete by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(item.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (item.description.isNotBlank()) {
                                        Text(
                                            text = item.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                ItemRowActions(
                                    onEdit = { onItemClick(item.id) },
                                    onDelete = { showDelete = true }
                                )
                            }
                        }

                        if (showDelete) {
                            ConfirmDeleteDialog(
                                title = "Delete \"${item.name}\"?",
                                message = "This item will be permanently removed.",
                                onConfirm = {
                                    viewModel.deleteItem(item)
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

    // ── Dialogs ────────────────────────────────────────────────────────────
    if (showAddContainer) {
        ContainerDialog(
            title = "Add container",
            onConfirm = { name, desc ->
                viewModel.addContainer(locationId, name, desc)
                showAddContainer = false
            },
            onDismiss = { showAddContainer = false }
        )
    }

    if (showAddItem) {
        AddItemDialog(
            locations = allLocations.locations,
            getContainersForLocation = viewModel::getContainersForLocation,
            defaultLocationId = locationId,
            onConfirm = { name, desc, locId, containerId ->
                if (containerId != null) {
                    viewModel.addItemToContainer(locId, containerId, name, desc)
                } else {
                    viewModel.addItemToLocation(locId, name, desc)
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