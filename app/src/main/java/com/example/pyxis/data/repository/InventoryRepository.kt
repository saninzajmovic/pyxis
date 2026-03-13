package com.example.pyxis.data.repository

import com.example.pyxis.data.dao.InventoryDao
import com.example.pyxis.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository pattern: the single source of truth for all inventory data.
 * The ViewModel never talks directly to the DAO — it always goes through here.
 * This decouples the data source from the UI layer and makes future data-source
 * swaps (e.g. adding a remote API) a one-file change.
 */
class InventoryRepository(private val dao: InventoryDao) {

    // ── Locations ──────────────────────────────────────────────────────────

    fun getAllLocations(): Flow<List<LocationEntity>> = dao.getAllLocations()

    suspend fun getLocationById(id: Long): LocationEntity? = dao.getLocationById(id)

    suspend fun insertLocation(location: LocationEntity): Long = dao.insertLocation(location)

    suspend fun updateLocation(location: LocationEntity) = dao.updateLocation(location)

    suspend fun deleteLocation(location: LocationEntity) = dao.deleteLocation(location)

    // ── Containers ─────────────────────────────────────────────────────────

    fun getTopLevelContainers(locationId: Long): Flow<List<ContainerEntity>> =
        dao.getTopLevelContainersForLocation(locationId)

    fun getChildContainers(parentContainerId: Long): Flow<List<ContainerEntity>> =
        dao.getChildContainers(parentContainerId)

    suspend fun getContainerById(id: Long): ContainerEntity? = dao.getContainerById(id)

    suspend fun getAllContainersForLocation(locationId: Long): List<ContainerEntity> =
        dao.getAllContainersForLocation(locationId)

    suspend fun insertContainer(container: ContainerEntity): Long = dao.insertContainer(container)

    suspend fun updateContainer(container: ContainerEntity) = dao.updateContainer(container)

    suspend fun deleteContainer(container: ContainerEntity) = dao.deleteContainer(container)

    // ── Items ──────────────────────────────────────────────────────────────

    fun getItemsDirectlyInLocation(locationId: Long): Flow<List<ItemEntity>> =
        dao.getItemsDirectlyInLocation(locationId)

    fun getItemsInContainer(containerId: Long): Flow<List<ItemEntity>> =
        dao.getItemsInContainer(containerId)

    suspend fun getItemById(id: Long): ItemEntity? = dao.getItemById(id)

    suspend fun insertItem(item: ItemEntity): Long = dao.insertItem(item)

    suspend fun updateItem(item: ItemEntity) = dao.updateItem(item)

    suspend fun deleteItem(item: ItemEntity) = dao.deleteItem(item)

    // ── Search with breadcrumb resolution ─────────────────────────────────

    /**
     * Searches items by name and resolves the full breadcrumb for each result.
     * Breadcrumb walks up the container tree to produce e.g.:
     *   "Basement > Shelf A > Small Box"
     * If the item is directly in a room (no container):
     *   "Basement (no container)"
     */
    suspend fun searchItems(query: String): List<ItemSearchResult> {
        val items = dao.searchItems(query)
        if (items.isEmpty()) return emptyList()

        // Bulk-fetch all unique location and container IDs to minimise DB round-trips
        val locationIds = items.map { it.locationId }.distinct()
        val containerIds = items.mapNotNull { it.containerId }.distinct()

        val locationsById = dao.getLocationsByIds(locationIds).associateBy { it.id }
        // We may need to walk up multiple levels of containers, so we fetch all
        // containers for the relevant locations in one go.
        val allContainerIds = containerIds.toMutableSet()
        // Iteratively resolve parent chain
        var toResolve = containerIds
        while (toResolve.isNotEmpty()) {
            val parents = dao.getContainersByIds(toResolve)
                .mapNotNull { it.parentContainerId }
                .filter { it !in allContainerIds }
            allContainerIds.addAll(parents)
            toResolve = parents
        }
        val containersById = dao.getContainersByIds(allContainerIds.toList()).associateBy { it.id }

        return items.map { item ->
            val location = locationsById[item.locationId]
            val locationName = location?.name ?: "Unknown location"

            val breadcrumb = if (item.containerId == null) {
                "$locationName (no container)"
            } else {
                // Walk up the container chain to build the path segments
                val segments = mutableListOf<String>()
                var currentId: Long? = item.containerId
                while (currentId != null) {
                    val container = containersById[currentId] ?: break
                    segments.add(0, container.name)
                    currentId = container.parentContainerId
                }
                "$locationName > ${segments.joinToString(" > ")}"
            }

            ItemSearchResult(item = item, breadcrumb = breadcrumb)
        }
    }
}