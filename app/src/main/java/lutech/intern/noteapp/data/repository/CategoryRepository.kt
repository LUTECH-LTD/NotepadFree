package lutech.intern.noteapp.data.repository

import androidx.lifecycle.LiveData
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.database.dao.CategoryDao

class CategoryRepository(private val categoryDao: CategoryDao) {
    fun fetchAllCategories(): LiveData<List<Category>> {
        return categoryDao.fetchAllCategories()
    }

    suspend fun insert(category: Category) {
        categoryDao.insert(category)
    }

    suspend fun update(category: Category) {
        categoryDao.update(category)
    }

    suspend fun delete(category: Category) {
        categoryDao.delete(category)
    }

    fun isCategoryNameExists(name: String): Boolean {
        return categoryDao.getCategoryByName(name) != null
    }
}