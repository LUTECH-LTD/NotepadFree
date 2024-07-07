package lutech.intern.noteapp.data.repository

import androidx.lifecycle.LiveData
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.database.dao.CategoryDao

class CategoryRepository(private val categoryDao: CategoryDao) {
    fun fetchAllCategories(): LiveData<List<Category>> {
        return categoryDao.fetchAllCategories()
    }

    suspend fun insert(category: Category): Boolean {
        return categoryDao.getCategoryByName(category.name) == null && categoryDao.insert(category) != -1L
    }

    suspend fun update(category: Category): Boolean {
        return categoryDao.getCategoryByName(category.name) == null && categoryDao.update(category) > 0
    }

    suspend fun delete(category: Category) {
        categoryDao.delete(category)
    }
}