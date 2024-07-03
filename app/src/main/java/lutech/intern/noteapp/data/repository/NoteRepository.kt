package lutech.intern.noteapp.data.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lutech.intern.noteapp.data.model.Note
import lutech.intern.noteapp.database.dao.NoteDao

class NoteRepository(private val noteDao: NoteDao) {
    fun fetchNotes(): LiveData<List<Note>> {
        return noteDao.fetchAllNotes()
    }

    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    suspend fun update(note: Note) {
        noteDao.update(note)
    }

    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }
}