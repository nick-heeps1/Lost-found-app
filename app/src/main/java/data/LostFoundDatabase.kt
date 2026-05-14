package com.example.lost_found_app.ui
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import data.LostFoundDao

@Database(entities = [LostFoundItem::class], version = 2, exportSchema = false)
abstract class LostFoundDatabase : RoomDatabase() {
    abstract fun lostFoundDao(): LostFoundDao
    companion object {
        @Volatile
        private var INSTANCE: LostFoundDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE lost_found_items ADD COLUMN latitude REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE lost_found_items ADD COLUMN longitude REAL NOT NULL DEFAULT 0.0")
            }
        }

        fun getDatabase(context: Context): LostFoundDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LostFoundDatabase::class.java,
                    "lost_found_dataabse"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}