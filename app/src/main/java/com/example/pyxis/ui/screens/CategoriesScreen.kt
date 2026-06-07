package com.example.pyxis.ui.screens

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
import androidx.compose.ui.unit.dp
import com.example.pyxis.ui.components.*
import com.example.pyxis.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: InventoryViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.categoryUiState.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add category")
            }
        }
    ) { padding ->
        if (state.categories.isEmpty()) {
            EmptyState(
                "No categories yet.\nTap + to add one.",
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
                items(state.categories, key = { it.id }) { category ->
                    var showEdit by remember { mutableStateOf(false) }
                    var showDelete by remember { mutableStateOf(false) }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                category.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            ThreeDotsMenu(
                                onEdit = { showEdit = true },
                                onDelete = { showDelete = true }
                            )
                        }
                    }

                    if (showEdit) {
                        CategoryDialog(
                            title = "Edit category",
                            initialName = category.name,
                            onConfirm = { name ->
                                viewModel.updateCategory(category, name)
                                showEdit = false
                            },
                            onDismiss = { showEdit = false }
                        )
                    }
                    if (showDelete) {
                        ConfirmDeleteDialog(
                            title = "Delete \"${category.name}\"?",
                            message = "Items assigned to this category will become uncategorised.",
                            onConfirm = {
                                viewModel.deleteCategory(category)
                                showDelete = false
                            },
                            onDismiss = { showDelete = false }
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        CategoryDialog(
            title = "Add category",
            onConfirm = { name ->
                viewModel.addCategory(name)
                showAdd = false
            },
            onDismiss = { showAdd = false }
        )
    }
}