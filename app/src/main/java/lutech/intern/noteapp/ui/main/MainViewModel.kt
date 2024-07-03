package lutech.intern.noteapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import lutech.intern.noteapp.common.NoteApplication
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.repository.CategoryRepository
import lutech.intern.noteapp.database.NoteDatabase

class MainViewModel : ViewModel() {
    private val categoryRepository by lazy {
        CategoryRepository(
            NoteDatabase.getDatabase(NoteApplication.context).categoryDao()
        )
    }

    val categories: LiveData<List<Category>> = categoryRepository.fetchAllCategories()
}