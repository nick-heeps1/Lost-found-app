package com.example.lost_found_app.ui

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class LostFoundViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: Lostfoundrepository
    val allItems: LiveData<List<LostFoundItem>>
    private val searchQuery = MutableLiveData("")
    private val selectedCategory = MutableLiveData("All")
    val filteredItems = MediatorLiveData<List<LostFoundItem>>()
    init {
        val dao = LostFoundDatabase.getDatabase(application).lostFoundDao()
        repository = Lostfoundrepository(dao)
        allItems = repository.allItems

        var currentSource: LiveData<List<LostFoundItem>>? = null

        fun refresh() {
            val q = "%${searchQuery.value ?: ""}%"
            val cat = selectedCategory.value ?: "All"
            currentSource?.let { filteredItems.removeSource(it) }
            val newSource = repository.searchAndFilter(q, cat)
            currentSource = newSource
            filteredItems.addSource(newSource) { filteredItems.value = it }
        }
        filteredItems.addSource(searchQuery) { refresh() }
        filteredItems.addSource(selectedCategory) { refresh() }
    }
    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }
    fun setCategory(category: String) {
        selectedCategory.value = category
    }
    fun insert(item: LostFoundItem) = viewModelScope.launch {
        repository.insert(item)
    }

    fun delete(item: LostFoundItem) = viewModelScope.launch {
        repository.delete(item)
    }
}