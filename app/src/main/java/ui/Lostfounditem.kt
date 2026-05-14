package com.example.lost_found_app.ui
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lost_found_items")
data class LostFoundItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val postType: String,
    val name: String,
    val phone: String,
    val description: String,
    val date: String,
    val location: String,
    val category: String,
    val imagePath: String? = null,
    val postedAt: Long = System.currentTimeMillis(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
