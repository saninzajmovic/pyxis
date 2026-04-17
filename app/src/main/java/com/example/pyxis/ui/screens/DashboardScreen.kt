package com.example.pyxis.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pyxis.model.ItemSearchResult
import com.example.pyxis.ui.components.*
import com.example.pyxis.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: InventoryViewModel,
    onLocationClick: (locationId: Long) -> Unit,
    onItemClick: (itemId: Long) -> Unit
) {
    val locationState by viewModel.locationUiState.collectAsState()
    val searchState by viewModel.searchState.collectAsState()

    var showAddLocation by remember { mutableStateOf(false) }
    val isSearchActive = searchState.query.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PYXIS") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (!isSearchActive) {
                FloatingActionButton(onClick = { showAddLocation = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add location")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Search bar ─────────────────────────────────────────────────
            SearchBar(
                query = searchState.query,
                onQueryChange = viewModel::onSearchQueryChanged,
                onClear = viewModel::clearSearch
            )

            // ── Search results ─────────────────────────────────────────────
            if (isSearchActive) {
                SearchResults(
                    state = searchState,
                    onItemClick = onItemClick
                )
            } else {
                // ── Location list ──────────────────────────────────────────
                if (locationState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (locationState.locations.isEmpty()) {
                    EmptyState("No rooms yet.\nTap + to add your first room.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(locationState.locations, key = { it.id }) { location ->
                            var showEdit by remember { mutableStateOf(false) }
                            var showDelete by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onLocationClick(location.id) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = location.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ItemRowActions(
                                        onEdit = { showEdit = true },
                                        onDelete = { showDelete = true }
                                    )
                                }
                            }

                            if (showEdit) {
                                LocationDialog(
                                    title = "Edit room",
                                    initialName = location.name,
                                    onConfirm = { newName ->
                                        viewModel.updateLocation(location, newName)
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

    if (showAddLocation) {
        LocationDialog(
            title = "Add room",
            onConfirm = { name ->
                viewModel.addLocation(name)
                showAddLocation = false
            },
            onDismiss = { showAddLocation = false }
        )
    }
}

// ── Search bar component ───────────────────────────────────────────────────────

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Find an item…") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            AnimatedVisibility(visible = query.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

// ── Search results list ────────────────────────────────────────────────────────

@Composable
private fun SearchResults(
    state: com.example.pyxis.viewmodel.SearchUiState,
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
                    SearchResultCard(result = result, onClick = { onItemClick(result.item.id) })
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(result: ItemSearchResult, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = result.item.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = result.breadcrumb,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (result.item.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = result.item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}