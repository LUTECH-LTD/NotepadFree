package lutech.intern.noteapp.data.repository

import lutech.intern.noteapp.data.model.Category
import lutech.intern.noteapp.database.dao.CategoryDao

class CategoryRepository(private val categoryDao: CategoryDao) {
    fun fetchCategories(): List<Category> {
        return categoryDao.getAllCategories()
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
}