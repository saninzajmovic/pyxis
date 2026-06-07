package com.example.pyxis.data.dao

import androidx.room.*
import com.example.pyxis.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // ── Locations ──────────────────────────────────────────────────────────
    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations WHERE id = :id")
    suspend fun getLocationById(id: Long): LocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity): Long

    @Update
    suspend fun updateLocation(location: LocationEntity)

    @Delete
    suspend fun deleteLocation(location: LocationEntity)

    // ── Containers ─────────────────────────────────────────────────────────
    @Query("SELECT * FROM containers WHERE locationId = :locationId AND parentContainerId IS NULL ORDER BY name ASC")
    fun getTopLevelContainersForLocation(locationId: Long): Flow<List<ContainerEntity>>

    @Query("SELECT * FROM containers WHERE parentContainerId = :parentId ORDER BY name ASC")
    fun getChildContainers(parentId: Long): Flow<List<ContainerEntity>>

    @Query("SELECT * FROM containers WHERE id = :id")
    suspend fun getContainerById(id: Long): ContainerEntity?

    @Query("SELECT * FROM containers WHERE locationId = :locationId ORDER BY name ASC")
    suspend fun getAllContainersForLocation(locationId: Long): List<ContainerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContainer(container: ContainerEntity): Long

    @Update
    suspend fun updateContainer(container: ContainerEntity)

    @Delete
    suspend fun deleteContainer(container: ContainerEntity)

    // ── Categories ─────────────────────────────────────────────────────────
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    // ── Items ──────────────────────────────────────────────────────────────
    @Query("SELECT * FROM items WHERE locationId = :locationId AND containerId IS NULL ORDER BY name ASC")
    fun getItemsDirectlyInLocation(locationId: Long): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE containerId = :containerId ORDER BY name ASC")
    fun getItemsInContainer(containerId: Long): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Long): ItemEntity?

    @Query("SELECT * FROM items WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchItems(query: String): List<ItemEntity>

    @Query("SELECT * FROM items WHERE name LIKE '%' || :query || '%' AND (:categoryId IS NULL OR categoryId = :categoryId) ORDER BY name ASC")
    suspend fun searchItemsFiltered(query: String, categoryId: Long?): List<ItemEntity>

    @Query("SELECT * FROM items WHERE categoryId = :categoryId ORDER BY name ASC")
    suspend fun getItemsByCategory(categoryId: Long): List<ItemEntity>

    @Query("SELECT * FROM items ORDER BY name ASC")
    suspend fun getAllItems(): List<ItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    // ── Bulk helpers ───────────────────────────────────────────────────────
    @Query("SELECT * FROM containers WHERE id IN (:ids)")
    suspend fun getContainersByIds(ids: List<Long>): List<ContainerEntity>

    @Query("SELECT * FROM locations WHERE id IN (:ids)")
    suspend fun getLocationsByIds(ids: List<Long>): List<LocationEntity>
}