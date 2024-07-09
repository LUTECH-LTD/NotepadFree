package lutech.intern.noteapp.data.repository

import androidx.lifecycle.LiveData
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.entity.CategoryWithNotes
import lutech.intern.noteapp.database.dao.CategoryDao

class CategoryRepository(private val dao: CategoryDao) {
    suspend fun insert(category: Category): Boolean {
        return dao.getCategoryByName(category.name) == null && dao.insert(category) != -1L
    }

    suspend fun update(category: Category): Boolean {
        return dao.getCategoryByName(category.name) == null && dao.update(category) > 0
    }

    suspend fun delete(category: Category) {
        dao.delete(category)
    }

    fun getCategories(): LiveData<List<Category>> {
        return dao.getCategories()
    }

    fun getCategoryWithNotes(): LiveData<List<CategoryWithNotes>> {
        return dao.getCategoryWithNotes()
    }
}