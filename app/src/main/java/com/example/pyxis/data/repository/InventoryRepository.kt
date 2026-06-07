package com.example.pyxis.data.repository

import com.example.pyxis.data.dao.InventoryDao
import com.example.pyxis.model.*
import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val dao: InventoryDao) {

    // ── Locations ──────────────────────────────────────────────────────────
    fun getAllLocations(): Flow<List<LocationEntity>> = dao.getAllLocations()
    suspend fun getLocationById(id: Long) = dao.getLocationById(id)
    suspend fun insertLocation(location: LocationEntity) = dao.insertLocation(location)
    suspend fun updateLocation(location: LocationEntity) = dao.updateLocation(location)
    suspend fun deleteLocation(location: LocationEntity) = dao.deleteLocation(location)

    // ── Containers ─────────────────────────────────────────────────────────
    fun getTopLevelContainers(locationId: Long) = dao.getTopLevelContainersForLocation(locationId)
    fun getChildContainers(parentId: Long) = dao.getChildContainers(parentId)
    suspend fun getContainerById(id: Long) = dao.getContainerById(id)
    suspend fun getAllContainersForLocation(locationId: Long) = dao.getAllContainersForLocation(locationId)
    suspend fun insertContainer(container: ContainerEntity) = dao.insertContainer(container)
    suspend fun updateContainer(container: ContainerEntity) = dao.updateContainer(container)
    suspend fun deleteContainer(container: ContainerEntity) = dao.deleteContainer(container)

    // ── Categories ─────────────────────────────────────────────────────────
    fun getAllCategories(): Flow<List<CategoryEntity>> = dao.getAllCategories()
    suspend fun insertCategory(category: CategoryEntity) = dao.insertCategory(category)
    suspend fun updateCategory(category: CategoryEntity) = dao.updateCategory(category)
    suspend fun deleteCategory(category: CategoryEntity) = dao.deleteCategory(category)

    // ── Items ──────────────────────────────────────────────────────────────
    fun getItemsDirectlyInLocation(locationId: Long) = dao.getItemsDirectlyInLocation(locationId)
    fun getItemsInContainer(containerId: Long) = dao.getItemsInContainer(containerId)
    suspend fun getItemById(id: Long) = dao.getItemById(id)
    suspend fun insertItem(item: ItemEntity) = dao.insertItem(item)
    suspend fun updateItem(item: ItemEntity) = dao.updateItem(item)
    suspend fun deleteItem(item: ItemEntity) = dao.deleteItem(item)

    // ── Search with breadcrumb + optional category filter ──────────────────
    suspend fun searchItems(
        query: String,
        categoryId: Long? = null,
        showAll: Boolean = false
    ): List<ItemSearchResult> {
        val items = when {
            showAll && query.isBlank() && categoryId == null -> dao.getAllItems()
            query.isBlank() && categoryId != null -> dao.getItemsByCategory(categoryId)
            categoryId != null -> dao.searchItemsFiltered(query, categoryId)
            else -> dao.searchItems(query)
        }
        if (items.isEmpty()) return emptyList()

        val locationIds = items.map { it.locationId }.distinct()
        val categoryIds = items.mapNotNull { it.categoryId }.distinct()

        val locationsById = dao.getLocationsByIds(locationIds).associateBy { it.id }
        val categoriesById = if (categoryIds.isNotEmpty()) {
            // Fetch categories inline (small set, fine to do per-search)
            categoryIds.mapNotNull { dao.getCategoryById(it) }.associateBy { it.id }
        } else emptyMap()

        // Walk container parent chain
        val containerIds = items.mapNotNull { it.containerId }.distinct().toMutableSet()
        var toResolve = containerIds.toList()
        while (toResolve.isNotEmpty()) {
            val parents = dao.getContainersByIds(toResolve)
                .mapNotNull { it.parentContainerId }
                .filter { it !in containerIds }
            containerIds.addAll(parents)
            toResolve = parents
        }
        val containersById = dao.getContainersByIds(containerIds.toList()).associateBy { it.id }

        return items.map { item ->
            val locationName = locationsById[item.locationId]?.name ?: "Unknown"
            val breadcrumb = if (item.containerId == null) {
                "$locationName (no container)"
            } else {
                val segments = mutableListOf<String>()
                var cur: Long? = item.containerId
                while (cur != null) {
                    val c = containersById[cur] ?: break
                    segments.add(0, c.name)
                    cur = c.parentContainerId
                }
                "$locationName > ${segments.joinToString(" > ")}"
            }
            ItemSearchResult(
                item = item,
                breadcrumb = breadcrumb,
                categoryName = item.categoryId?.let { categoriesById[it]?.name }
            )
        }
    }
}