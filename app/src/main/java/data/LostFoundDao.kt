package data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lost_found_app.ui.LostFoundItem

@Dao
interface LostFoundDao {
    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insert(item: LostFoundItem)

    @Delete
    suspend fun delete(item: LostFoundItem)

    @Query("Select * FROM lost_found_items ORDER BY postedAt DESC")
    fun getAllItems(): LiveData<List<LostFoundItem>>

    @Query("""
    SELECT * FROM lost_found_items 
    WHERE (description LIKE :searchQuery OR name LIKE :searchQuery)
    AND (:category = 'All' OR category = :category)
    ORDER BY postedAt DESC
""")
    fun searchAndFilter(searchQuery: String, category: String): LiveData<List<LostFoundItem>>
}