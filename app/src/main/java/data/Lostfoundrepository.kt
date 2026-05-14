package com.example.lost_found_app.ui

import androidx.lifecycle.LiveData
import data.LostFoundDao

class Lostfoundrepository(private val dao: LostFoundDao) {
    val allItems: LiveData<List<LostFoundItem>> = dao.getAllItems()
    suspend fun insert(item: LostFoundItem){
        dao.insert(item)
    }

    suspend fun delete(item: LostFoundItem) {
        dao.delete(item)
    }

    fun searchAndFilter(query: String, category: String): LiveData<List<LostFoundItem>> {
        return dao.searchAndFilter(query, category)
    }
}

