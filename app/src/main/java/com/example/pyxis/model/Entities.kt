package com.example.pyxis.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ── Location (Room) ────────────────────────────────────────────────────────────

enum class RoomIconType {
    BEDROOM, STORAGE, BASEMENT, OFFICE, DEFAULT
}

/**
 * R2: Added gradientPreset (one of 8 preset string keys) and iconType.
 * Migration version 1->2 handles the new columns with default values.
 */
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconType: String = RoomIconType.DEFAULT.name,
    val gradientPreset: String = "PRESET_1"
)

// ── Container ──────────────────────────────────────────────────────────────────

@Entity(
    tableName = "containers",
    foreignKeys = [
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ContainerEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentContainerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("locationId"), Index("parentContainerId"), Index("categoryId")]
)
data class ContainerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val locationId: Long,
    val parentContainerId: Long? = null,
    val categoryId: Long? = null,
    val name: String,
    val description: String = ""
)

// ── Category ───────────────────────────────────────────────────────────────────

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

// ── Item ───────────────────────────────────────────────────────────────────────

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ContainerEntity::class,
            parentColumns = ["id"],
            childColumns = ["containerId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("locationId"), Index("containerId"), Index("categoryId")]
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val locationId: Long,
    val containerId: Long? = null,
    val categoryId: Long? = null,
    val name: String,
    val description: String = ""
)