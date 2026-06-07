package com.example.pyxis.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pyxis.data.dao.InventoryDao
import com.example.pyxis.model.*

@Database(
    entities = [
        LocationEntity::class,
        ContainerEntity::class,
        CategoryEntity::class,
        ItemEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE locations ADD COLUMN iconType TEXT NOT NULL DEFAULT 'DEFAULT'")
                db.execSQL("ALTER TABLE locations ADD COLUMN gradientPreset TEXT NOT NULL DEFAULT 'PRESET_1'")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("ALTER TABLE items ADD COLUMN categoryId INTEGER REFERENCES categories(id) ON DELETE SET NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_items_categoryId ON items(categoryId)")
            }
        }

        /**
         * Migration 2 -> 3:
         * - containers: add categoryId (INTEGER nullable FK to categories)
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE containers ADD COLUMN categoryId INTEGER REFERENCES categories(id) ON DELETE SET NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_containers_categoryId ON containers(categoryId)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pyxis_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}