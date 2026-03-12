package com.example.pyxis.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * A container with all its direct child items.
 */
data class ContainerWithItems(
    @Embedded val container: ContainerEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "containerId"
    )
    val items: List<ItemEntity>
)

/**
 * A location with all its top-level containers and direct items.
 */
data class LocationWithContainers(
    @Embedded val location: LocationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "locationId"
    )
    val containers: List<ContainerEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "locationId"
    )
    val items: List<ItemEntity>
)

/**
 * Flat search result carrying the resolved breadcrumb path for an item.
 * Built manually in the repository — not a Room @Relation.
 */
data class ItemSearchResult(
    val item: ItemEntity,
    val breadcrumb: String  // e.g. "Basement > Box 1 > Small Tin" or "Basement (no container)"
)