package lutech.intern.noteapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import lutech.intern.noteapp.data.model.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category")
    fun getAllCategories(): List<Category>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category)
}