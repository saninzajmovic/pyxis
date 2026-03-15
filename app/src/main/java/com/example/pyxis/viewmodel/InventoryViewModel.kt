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

// ── UI state models ────────────────────────────────────────────────────────────

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
    val isSearching: Boolean = false
)

data class ItemDetailUiState(
    val item: ItemEntity? = null,
    val breadcrumb: String = "",
    val isLoading: Boolean = true
)

// ── ViewModel ──────────────────────────────────────────────────────────────────

class InventoryViewModel(private val repository: InventoryRepository) : ViewModel() {

    // ── Dashboard / Locations ──────────────────────────────────────────────

    /**
     * Observer pattern: StateFlow observed by Compose via collectAsState().
     * Room emits a new list whenever the DB changes — the UI reacts automatically.
     */
    val locationUiState: StateFlow<LocationUiState> =
        repository.getAllLocations()
            .map { LocationUiState(locations = it, isLoading = false) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = LocationUiState()
            )

    fun addLocation(name: String) = viewModelScope.launch {
        if (name.isNotBlank()) {
            repository.insertLocation(LocationEntity(name = name.trim()))
        }
    }

    fun updateLocation(location: LocationEntity, newName: String) = viewModelScope.launch {
        if (newName.isNotBlank()) {
            repository.updateLocation(location.copy(name = newName.trim()))
        }
    }

    fun deleteLocation(location: LocationEntity) = viewModelScope.launch {
        repository.deleteLocation(location)
    }

    // ── Room Detail ────────────────────────────────────────────────────────

    private val _currentLocationId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val roomDetailUiState: StateFlow<RoomDetailUiState> =
        _currentLocationId.flatMapLatest { locationId ->
            if (locationId == null) return@flatMapLatest flowOf(RoomDetailUiState())
            combine(
                repository.getTopLevelContainers(locationId),
                repository.getItemsDirectlyInLocation(locationId)
            ) { containers, directItems ->
                val location = repository.getLocationById(locationId)
                RoomDetailUiState(
                    location = location,
                    containers = containers,
                    directItems = directItems,
                    isLoading = false
                )
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = RoomDetailUiState()
            )

    fun selectLocation(locationId: Long) {
        _currentLocationId.value = locationId
    }

    fun addContainer(
        locationId: Long,
        name: String,
        description: String,
        parentContainerId: Long? = null
    ) = viewModelScope.launch {
        if (name.isNotBlank()) {
            repository.insertContainer(
                ContainerEntity(
                    locationId = locationId,
                    parentContainerId = parentContainerId,
                    name = name.trim(),
                    description = description.trim()
                )
            )
        }
    }

    fun addItemToLocation(locationId: Long, name: String, description: String) =
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.insertItem(
                    ItemEntity(
                        locationId = locationId,
                        containerId = null,
                        name = name.trim(),
                        description = description.trim()
                    )
                )
            }
        }

    // ── Container Detail ───────────────────────────────────────────────────

    private val _currentContainerId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val containerDetailUiState: StateFlow<ContainerDetailUiState> =
        _currentContainerId.flatMapLatest { containerId ->
            if (containerId == null) return@flatMapLatest flowOf(ContainerDetailUiState())
            combine(
                repository.getChildContainers(containerId),
                repository.getItemsInContainer(containerId)
            ) { children, items ->
                val container = repository.getContainerById(containerId)
                ContainerDetailUiState(
                    container = container,
                    childContainers = children,
                    items = items,
                    isLoading = false
                )
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ContainerDetailUiState()
            )

    fun selectContainer(containerId: Long) {
        _currentContainerId.value = containerId
    }

    fun addItemToContainer(
        locationId: Long,
        containerId: Long,
        name: String,
        description: String
    ) = viewModelScope.launch {
        if (name.isNotBlank()) {
            repository.insertItem(
                ItemEntity(
                    locationId = locationId,
                    containerId = containerId,
                    name = name.trim(),
                    description = description.trim()
                )
            )
        }
    }

    fun updateContainer(container: ContainerEntity, name: String, description: String) =
        viewModelScope.launch {
            repository.updateContainer(
                container.copy(name = name.trim(), description = description.trim())
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
        val results = repository.searchItems("") // We use search to get breadcrumb
        // More direct: resolve breadcrumb for a single item
        val item = repository.getItemById(itemId)
        if (item == null) {
            _itemDetailState.value = ItemDetailUiState(isLoading = false)
            return@launch
        }
        val searchResult = repository.searchItems(item.name)
            .firstOrNull { it.item.id == itemId }
        _itemDetailState.value = ItemDetailUiState(
            item = item,
            breadcrumb = searchResult?.breadcrumb ?: "",
            isLoading = false
        )
    }

    fun updateItem(item: ItemEntity, name: String, description: String) = viewModelScope.launch {
        val updated = item.copy(name = name.trim(), description = description.trim())
        repository.updateItem(updated)
        _itemDetailState.value = _itemDetailState.value.copy(item = updated)
    }

    fun deleteItem(item: ItemEntity) = viewModelScope.launch {
        repository.deleteItem(item)
    }

    fun moveItem(item: ItemEntity, newLocationId: Long, newContainerId: Long?) =
        viewModelScope.launch {
            val updated = item.copy(locationId = newLocationId, containerId = newContainerId)
            repository.updateItem(updated)
            _itemDetailState.value = _itemDetailState.value.copy(item = updated)
        }

    // ── Search ─────────────────────────────────────────────────────────────

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    @OptIn(FlowPreview::class)
    private val searchQueryFlow = MutableStateFlow("")

    init {
        // Debounce search so we don't hit the DB on every keystroke
        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300)
                .collect { query ->
                    if (query.isBlank()) {
                        _searchState.value = SearchUiState(query = query)
                    } else {
                        _searchState.value = _searchState.value.copy(isSearching = true)
                        val results = repository.searchItems(query)
                        _searchState.value = SearchUiState(
                            query = query,
                            results = results,
                            isSearching = false
                        )
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchState.value = _searchState.value.copy(query = query)
        searchQueryFlow.value = query
    }

    fun clearSearch() {
        _searchState.value = SearchUiState()
        searchQueryFlow.value = ""
    }

    // ── Helpers for add-item dialog (location/container picker) ────────────

    suspend fun getContainersForLocation(locationId: Long): List<ContainerEntity> =
        repository.getAllContainersForLocation(locationId)

    // ── ViewModelFactory ───────────────────────────────────────────────────

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
                return InventoryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}