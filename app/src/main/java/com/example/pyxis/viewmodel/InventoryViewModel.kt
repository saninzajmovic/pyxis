package com.example.pyxis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pyxis.data.repository.InventoryRepository
import com.example.pyxis.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ── UI State ───────────────────────────────────────────────────────────────────

data class LocationUiState(
    val locations: List<LocationEntity> = emptyList(),
    val isLoading: Boolean = true
)

data class RoomDetailUiState(
    val location: LocationEntity? = null,
    val containers: List<ContainerEntity> = emptyList(),
    val directItems: List<ItemEntity> = emptyList(),
    val isLoading: Boolean = true
)

data class ContainerDetailUiState(
    val container: ContainerEntity? = null,
    val childContainers: List<ContainerEntity> = emptyList(),
    val items: List<ItemEntity> = emptyList(),
    val isLoading: Boolean = true
)

data class SearchUiState(
    val query: String = "",
    val results: List<ItemSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val filterCategoryId: Long? = null,
    val isSearchBarActive: Boolean = false,
    val showAll: Boolean = false
)

/**
 * One step in the resolved location path for an item.
 * e.g. LocationEntity "Basement", or ContainerEntity "Plava Kutija (top shelf)"
 */
data class BreadcrumbStep(
    val name: String,
    val description: String = "",  // empty for the room itself
    val isRoom: Boolean = false
)

data class ItemDetailUiState(
    val item: ItemEntity? = null,
    val breadcrumb: String = "",
    val breadcrumbSteps: List<BreadcrumbStep> = emptyList(),
    val categoryName: String? = null,
    val isLoading: Boolean = true
)

data class CategoryUiState(
    val categories: List<CategoryEntity> = emptyList()
)

// ── ViewModel ──────────────────────────────────────────────────────────────────

class InventoryViewModel(private val repository: InventoryRepository) : ViewModel() {

    // ── Locations ──────────────────────────────────────────────────────────

    val locationUiState: StateFlow<LocationUiState> =
        repository.getAllLocations()
            .map { LocationUiState(it, false) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LocationUiState())

    fun addLocation(name: String, iconType: RoomIconType, gradientPreset: String) =
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.insertLocation(
                    LocationEntity(
                        name = name.trim(),
                        iconType = iconType.name,
                        gradientPreset = gradientPreset
                    )
                )
            }
        }

    fun updateLocation(location: LocationEntity, name: String, iconType: RoomIconType, gradientPreset: String) =
        viewModelScope.launch {
            repository.updateLocation(
                location.copy(name = name.trim(), iconType = iconType.name, gradientPreset = gradientPreset)
            )
        }

    fun deleteLocation(location: LocationEntity) = viewModelScope.launch {
        repository.deleteLocation(location)
    }

    // ── Categories ─────────────────────────────────────────────────────────

    val categoryUiState: StateFlow<CategoryUiState> =
        repository.getAllCategories()
            .map { CategoryUiState(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoryUiState())

    fun addCategory(name: String) = viewModelScope.launch {
        if (name.isNotBlank()) repository.insertCategory(CategoryEntity(name = name.trim()))
    }

    fun updateCategory(category: CategoryEntity, name: String) = viewModelScope.launch {
        if (name.isNotBlank()) repository.updateCategory(category.copy(name = name.trim()))
    }

    fun deleteCategory(category: CategoryEntity) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

    // ── Room Detail ────────────────────────────────────────────────────────

    private val _currentLocationId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val roomDetailUiState: StateFlow<RoomDetailUiState> =
        _currentLocationId.flatMapLatest { id ->
            if (id == null) return@flatMapLatest flowOf(RoomDetailUiState())
            combine(
                repository.getTopLevelContainers(id),
                repository.getItemsDirectlyInLocation(id)
            ) { containers, items ->
                RoomDetailUiState(
                    location = repository.getLocationById(id),
                    containers = containers,
                    directItems = items,
                    isLoading = false
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoomDetailUiState())

    fun selectLocation(id: Long) { _currentLocationId.value = id }

    fun addContainer(
        locationId: Long,
        name: String,
        description: String,
        parentContainerId: Long? = null,
        categoryId: Long? = null
    ) = viewModelScope.launch {
        if (name.isNotBlank()) {
            repository.insertContainer(
                ContainerEntity(
                    locationId = locationId,
                    parentContainerId = parentContainerId,
                    categoryId = categoryId,
                    name = name.trim(),
                    description = description.trim()
                )
            )
        }
    }

    fun addItemToLocation(locationId: Long, name: String, description: String, categoryId: Long?) =
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.insertItem(
                    ItemEntity(locationId = locationId, containerId = null,
                        categoryId = categoryId, name = name.trim(), description = description.trim())
                )
            }
        }

    // ── Container Detail ───────────────────────────────────────────────────

    private val _currentContainerId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val containerDetailUiState: StateFlow<ContainerDetailUiState> =
        _currentContainerId.flatMapLatest { id ->
            if (id == null) return@flatMapLatest flowOf(ContainerDetailUiState())
            combine(
                repository.getChildContainers(id),
                repository.getItemsInContainer(id)
            ) { children, items ->
                ContainerDetailUiState(
                    container = repository.getContainerById(id),
                    childContainers = children,
                    items = items,
                    isLoading = false
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ContainerDetailUiState())

    fun selectContainer(id: Long) { _currentContainerId.value = id }

    fun addItemToContainer(locationId: Long, containerId: Long, name: String, description: String, categoryId: Long?) =
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.insertItem(
                    ItemEntity(locationId = locationId, containerId = containerId,
                        categoryId = categoryId, name = name.trim(), description = description.trim())
                )
            }
        }

    fun updateContainer(
        container: ContainerEntity,
        name: String,
        description: String,
        locationId: Long? = null,
        categoryId: Long? = container.categoryId
    ) = viewModelScope.launch {
        repository.updateContainer(
            container.copy(
                name = name.trim(),
                description = description.trim(),
                locationId = locationId ?: container.locationId,
                categoryId = categoryId
            )
        )
    }

    fun deleteContainer(container: ContainerEntity) = viewModelScope.launch {
        repository.deleteContainer(container)
    }

    // ── Item Detail ────────────────────────────────────────────────────────

    private val _itemDetailState = MutableStateFlow(ItemDetailUiState())
    val itemDetailUiState: StateFlow<ItemDetailUiState> = _itemDetailState.asStateFlow()

    fun loadItem(itemId: Long) = viewModelScope.launch {
        _itemDetailState.value = ItemDetailUiState(isLoading = true)
        val item = repository.getItemById(itemId) ?: run {
            _itemDetailState.value = ItemDetailUiState(isLoading = false)
            return@launch
        }
        val result = repository.searchItems(item.name).firstOrNull { it.item.id == itemId }

        // Build the detailed step list: Room → Container → … → innermost container
        val steps = mutableListOf<BreadcrumbStep>()
        val location = repository.getLocationById(item.locationId)
        if (location != null) {
            steps.add(BreadcrumbStep(name = location.name, isRoom = true))
        }
        if (item.containerId != null) {
            // Walk the container chain from innermost outward, then reverse
            val containerChain = mutableListOf<ContainerEntity>()
            var currentId: Long? = item.containerId
            while (currentId != null) {
                val container = repository.getContainerById(currentId) ?: break
                containerChain.add(0, container)
                currentId = container.parentContainerId
            }
            containerChain.forEach { container ->
                steps.add(
                    BreadcrumbStep(
                        name = container.name,
                        description = container.description,
                        isRoom = false
                    )
                )
            }
        }

        _itemDetailState.value = ItemDetailUiState(
            item = item,
            breadcrumb = result?.breadcrumb ?: "",
            breadcrumbSteps = steps,
            isLoading = false
        )
    }

    fun saveItem(
        item: ItemEntity,
        name: String,
        description: String,
        categoryId: Long?,
        newLocationId: Long,
        newContainerId: Long?
    ) = viewModelScope.launch {
        val updated = item.copy(
            name = name.trim(),
            description = description.trim(),
            categoryId = categoryId,
            locationId = newLocationId,
            containerId = newContainerId
        )
        repository.updateItem(updated)
        _itemDetailState.value = _itemDetailState.value.copy(item = updated)
    }

    fun deleteItem(item: ItemEntity) = viewModelScope.launch { repository.deleteItem(item) }

    // ── Search ─────────────────────────────────────────────────────────────

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            combine(
                searchQueryFlow.debounce(300),
                _searchState.map { it.filterCategoryId }.distinctUntilChanged(),
                _searchState.map { it.showAll }.distinctUntilChanged()
            ) { query, categoryId, showAll -> Triple(query, categoryId, showAll) }
                .collect { (query, categoryId, showAll) ->
                    when {
                        // Show all items — "All" chip pressed with no query
                        showAll && query.isBlank() && categoryId == null -> {
                            _searchState.value = _searchState.value.copy(isSearching = true)
                            val results = repository.searchItems("", null, showAll = true)
                            _searchState.value = _searchState.value.copy(results = results, isSearching = false)
                        }
                        // Nothing active — clear results
                        query.isBlank() && categoryId == null -> {
                            _searchState.value = _searchState.value.copy(results = emptyList(), isSearching = false)
                        }
                        // Normal search
                        else -> {
                            _searchState.value = _searchState.value.copy(isSearching = true)
                            val results = repository.searchItems(query, categoryId)
                            _searchState.value = _searchState.value.copy(results = results, isSearching = false)
                        }
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchState.value = _searchState.value.copy(query = query)
        searchQueryFlow.value = query
    }

    fun setSearchCategoryFilter(categoryId: Long?) {
        // null means "All" was pressed — show all items
        val showAll = categoryId == null
        _searchState.value = _searchState.value.copy(filterCategoryId = categoryId, showAll = showAll)
        searchQueryFlow.value = _searchState.value.query
    }

    fun setSearchBarActive(active: Boolean) {
        _searchState.value = _searchState.value.copy(isSearchBarActive = active)
        // When deactivating with no query and no filter, reset fully
        if (!active && _searchState.value.query.isBlank() && _searchState.value.filterCategoryId == null) {
            _searchState.value = SearchUiState()
        }
    }

    fun clearSearch() {
        _searchState.value = SearchUiState()
        searchQueryFlow.value = ""
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    suspend fun getContainersForLocation(locationId: Long): List<ContainerEntity> =
        repository.getAllContainersForLocation(locationId)

    // ── Factory ────────────────────────────────────────────────────────────

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InventoryViewModel::class.java))
                return InventoryViewModel(repository) as T
            throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}