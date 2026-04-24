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
fun ContainerDetailScreen(
    containerId: Long,
    viewModel: InventoryViewModel,
    onChildContainerClick: (containerId: Long) -> Unit,
    onItemClick: (itemId: Long) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(containerId) { viewModel.selectContainer(containerId) }

    val state by viewModel.containerDetailUiState.collectAsState()
    val allLocations by viewModel.locationUiState.collectAsState()

    var showAddContainer by remember { mutableStateOf(false) }
    var showAddItem by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.container?.name ?: "Container",
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
                        Text("+ Sub-container", modifier = Modifier.padding(horizontal = 8.dp))
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

        // Show container description if present
        val container = state.container
        val hasContent = state.childContainers.isNotEmpty() || state.items.isNotEmpty()

        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Description card if present
            if (container != null && container.description.isNotBlank()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = container.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            if (!hasContent) {
                item {
                    EmptyState(
                        "This container is empty.\nTap + to add a sub-container or item.",
                        modifier = Modifier.height(300.dp)
                    )
                }
            } else {
                // ── Child containers ───────────────────────────────────────
                if (state.childContainers.isNotEmpty()) {
                    item { SectionHeader("Sub-containers") }
                    items(state.childContainers, key = { "c-${it.id}" }) { child ->
                        var showEdit by remember { mutableStateOf(false) }
                        var showDelete by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChildContainerClick(child.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(child.name, style = MaterialTheme.typography.titleMedium)
                                    if (child.description.isNotBlank()) {
                                        Text(
                                            child.description,
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
                                title = "Edit sub-container",
                                initialName = child.name,
                                initialDescription = child.description,
                                onConfirm = { name, desc ->
                                    viewModel.updateContainer(child, name, desc)
                                    showEdit = false
                                },
                                onDismiss = { showEdit = false }
                            )
                        }
                        if (showDelete) {
                            ConfirmDeleteDialog(
                                title = "Delete \"${child.name}\"?",
                                message = "This will also delete all nested containers and items inside it.",
                                onConfirm = {
                                    viewModel.deleteContainer(child)
                                    showDelete = false
                                },
                                onDismiss = { showDelete = false }
                            )
                        }
                    }
                }

                // ── Items ──────────────────────────────────────────────────
                if (state.items.isNotEmpty()) {
                    item { SectionHeader("Items") }
                    items(state.items, key = { "i-${it.id}" }) { item ->
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
                                    Text(item.name, style = MaterialTheme.typography.titleMedium)
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
    val locationId = state.container?.locationId

    if (showAddContainer && locationId != null) {
        ContainerDialog(
            title = "Add sub-container",
            onConfirm = { name, desc ->
                viewModel.addContainer(locationId, name, desc, parentContainerId = containerId)
                showAddContainer = false
            },
            onDismiss = { showAddContainer = false }
        )
    }

    if (showAddItem && locationId != null) {
        AddItemDialog(
            locations = allLocations.locations,
            getContainersForLocation = viewModel::getContainersForLocation,
            defaultLocationId = locationId,
            defaultContainerId = containerId,
            onConfirm = { name, desc, locId, cId ->
                if (cId != null) {
                    viewModel.addItemToContainer(locId, cId, name, desc)
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