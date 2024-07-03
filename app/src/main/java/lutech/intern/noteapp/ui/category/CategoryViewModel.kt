package lutech.intern.noteapp.ui.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lutech.intern.noteapp.common.NoteApplication
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.repository.CategoryRepository
import lutech.intern.noteapp.database.NoteDatabase

class CategoryViewModel : ViewModel() {
    private val categoryRepository by lazy {
        CategoryRepository(
            NoteDatabase.getDatabase(NoteApplication.context).categoryDao()
        )
    }

    val categories: LiveData<List<Category>> = categoryRepository.fetchAllCategories()

    private val _insertResult = MutableLiveData<Boolean>()
    val insertResult = _insertResult

    private val _updateResult = MutableLiveData<Boolean?>()
    val updateResult = _updateResult

    fun insert(category: Category) = viewModelScope.launch {
        if (categoryRepository.isCategoryNameExists(category.name)) {
            _insertResult.value = false
        } else {
            categoryRepository.insert(category)
            _insertResult.value = true
        }
    }

    fun update(category: Category) = viewModelScope.launch {
        if (categoryRepository.isCategoryNameExists(category.name)) {
            _updateResult.value = false
        } else {
            categoryRepository.update(category)
            _updateResult.value = true
        }
    }

    fun delete(category: Category) = viewModelScope.launch {
        categoryRepository.delete(category)
    }

    fun resetUpdateResult() {
        _updateResult.value = null
    }
}