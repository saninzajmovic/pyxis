package com.example.pyxis.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

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
        )
    ],
    indices = [
        Index("locationId"),
        Index("parentContainerId")
    ]
)
data class ContainerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val locationId: Long,
    val parentContainerId: Long? = null, // null = top-level container in room
    val name: String,
    val description: String = ""
)

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
        )
    ],
    indices = [
        Index("locationId"),
        Index("containerId")
    ]
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val locationId: Long,
    val containerId: Long? = null, // null = item is directly in the room
    val name: String,
    val description: String = ""
)