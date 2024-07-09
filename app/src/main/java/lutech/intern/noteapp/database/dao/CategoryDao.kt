package lutech.intern.noteapp.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.entity.CategoryWithNotes

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category): Int

    @Delete
    suspend fun delete(category: Category): Int

    @Query("SELECT * FROM category ORDER BY categoryId ASC")
    fun getCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM Category WHERE name =:name")
    fun getCategoryByName(name: String): Category?

    @Transaction
    @Query("SELECT * FROM Category")
    fun getCategoryWithNotes(): LiveData<List<CategoryWithNotes>>
}