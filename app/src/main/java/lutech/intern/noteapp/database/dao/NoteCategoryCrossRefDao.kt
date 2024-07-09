package lutech.intern.noteapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import lutech.intern.noteapp.data.entity.relations.NoteCategoryCrossRef

@Dao
interface NoteCategoryCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(noteCategoryCrossRef: NoteCategoryCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(noteCategoryCrossRefs: List<NoteCategoryCrossRef>)

    @Query("DELETE FROM NoteCategoryCrossRef WHERE noteId = :noteId AND categoryId = :categoryId")
    suspend fun delete(noteId: Long, categoryId: Long)
}
