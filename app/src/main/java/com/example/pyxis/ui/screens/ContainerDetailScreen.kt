package com.example.pyxis.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
    val categoryState by viewModel.categoryUiState.collectAsState()

    var showRoomModal by remember { mutableStateOf(false) }
    var showAddContainer by remember { mutableStateOf(false) }
    var showAddItem by remember { mutableStateOf(false) }

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
            // Description card
            if (container != null && container.description.isNotBlank()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            container.description,
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
                    item { SectionLabel("Sub-containers") }
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
                                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(child.name, style = MaterialTheme.typography.titleMedium)
                                        child.categoryId?.let { cid ->
                                            categoryState.categories.firstOrNull { it.id == cid }?.let {
                                                CategoryPill(name = it.name)
                                            }
                                        }
                                    }
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
                                ThreeDotsMenu(
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
                                initialLocationId = child.locationId,
                                initialCategoryId = child.categoryId,
                                locations = allLocations.locations,
                                categories = categoryState.categories,
                                onConfirm = { name, desc, locId, catId ->
                                    viewModel.updateContainer(child, name, desc, locId, catId)
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

                // ── Items — tap to open detail, no inline edit/delete ──────
                if (state.items.isNotEmpty()) {
                    item { SectionLabel("Items") }
                    items(state.items, key = { "i-${it.id}" }) { item ->
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

    // ── Modals & dialogs ───────────────────────────────────────────────────
    val locationId = state.container?.locationId

    if (showRoomModal) {
        RoomAddModal(
            onItem      = { showRoomModal = false; showAddItem = true },
            onContainer = { showRoomModal = false; showAddContainer = true },
            onDismiss   = { showRoomModal = false }
        )
    }

    if (showAddContainer && locationId != null) {
        ContainerDialog(
            title = "Add sub-container",
            initialLocationId = locationId,
            categories = categoryState.categories,
            onConfirm = { name, desc, _, catId ->
                viewModel.addContainer(locationId, name, desc, parentContainerId = containerId, categoryId = catId)
                showAddContainer = false
            },
            onDismiss = { showAddContainer = false }
        )
    }

    if (showAddItem && locationId != null) {
        AddItemDialog(
            locations = allLocations.locations,
            categories = categoryState.categories,
            getContainersForLocation = viewModel::getContainersForLocation,
            defaultLocationId = locationId,
            defaultContainerId = containerId,
            onConfirm = { name, desc, locId, cId, categoryId ->
                if (cId != null) {
                    viewModel.addItemToContainer(locId, cId, name, desc, categoryId)
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
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}