package lutech.intern.noteapp.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.NoteWithCategories
import lutech.intern.noteapp.data.entity.NoteWithTextStyles

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM Note ORDER BY lastUpdate DESC")
    fun fetchAllNotes(): LiveData<List<Note>>

    @Transaction
    @Query("SELECT * FROM Note ORDER BY lastUpdate DESC")
    fun getNoteWithCategories(): LiveData<List<NoteWithCategories>>

    @Query("SELECT * FROM Note WHERE noteId = :noteId")
    fun getNoteById(noteId: Long): Note?

    @Query("SELECT * FROM Note WHERE noteId = :noteId")
    fun getNoteWithTextStylesById(noteId: Long): NoteWithTextStyles?
}