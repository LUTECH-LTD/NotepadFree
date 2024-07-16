package lutech.intern.noteapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import lutech.intern.noteapp.data.entity.TextStyles

@Dao
interface TextStylesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(textStyle: TextStyles)

    @Query("DELETE FROM TextStyles")
    suspend fun deleteAll()

    @Query("DELETE FROM TextStyles WHERE noteId = :noteId")
    suspend fun deleteByNoteId(noteId: Long)
}