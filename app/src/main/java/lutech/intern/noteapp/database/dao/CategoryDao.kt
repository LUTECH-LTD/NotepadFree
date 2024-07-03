package lutech.intern.noteapp.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import lutech.intern.noteapp.data.model.Category

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM category")
    fun fetchAllCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM category WHERE name =:name")
    fun getCategoryByName(name: String): Category?
}