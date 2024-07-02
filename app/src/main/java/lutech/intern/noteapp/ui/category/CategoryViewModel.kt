package lutech.intern.noteapp.ui.category

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lutech.intern.noteapp.common.NoteApplication
import lutech.intern.noteapp.constant.Constants
import lutech.intern.noteapp.data.model.Category
import lutech.intern.noteapp.data.repository.CategoryRepository
import lutech.intern.noteapp.database.NoteDatabase

class CategoryViewModel : ViewModel() {
    private val categoryDao by lazy {
        NoteDatabase.getDatabase(NoteApplication.context).categoryDao()
    }

    private val categoryRepository by lazy {
        CategoryRepository(categoryDao)
    }

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    init {
        fetchCategories()
    }

    fun fetchCategories() {
        _categories.value = categoryRepository.fetchCategories()
        // Master
    }

    fun insert(category: Category) = viewModelScope.launch {
        categoryRepository.insert(category)
        fetchCategories()
    }

    fun update(category: Category) = viewModelScope.launch {
        categoryRepository.update(category)
        fetchCategories()
    }

    fun delete(category: Category) = viewModelScope.launch {
        categoryRepository.delete(category)
        fetchCategories()
    }
}