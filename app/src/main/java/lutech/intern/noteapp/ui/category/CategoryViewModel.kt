package lutech.intern.noteapp.ui.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lutech.intern.noteapp.common.NoteApplication
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.repository.CategoryRepository
import lutech.intern.noteapp.database.NoteDatabase

class CategoryViewModel : ViewModel() {
    private var categoryRepository: CategoryRepository

    init {
        val categoryDao = NoteDatabase.getDatabase(NoteApplication.context).categoryDao()
        categoryRepository = CategoryRepository(categoryDao)
    }

    val categories: LiveData<List<Category>> = categoryRepository.getAllCategories()

    fun insertCategory(category: Category, callback: (Boolean) -> Unit) = viewModelScope.launch {
        callback(categoryRepository.insert(category))
    }

    fun updateCategory(category: Category, callback: (Boolean) -> Unit) = viewModelScope.launch {
        callback(categoryRepository.update(category))
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        categoryRepository.delete(category)
    }
}