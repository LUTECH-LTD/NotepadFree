package lutech.intern.noteapp.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import lutech.intern.noteapp.data.model.CategoryWithNotes
import lutech.intern.noteapp.data.model.NoteCategoryJoin

@Dao
interface NoteCategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteCategoryCrossRef(join: NoteCategoryJoin)

//    @Query("SELECT * FROM Category")
//    @Transaction
//
//    fun getCategoryWithNotes(): LiveData<List<CategoryWithNotes>>
}
