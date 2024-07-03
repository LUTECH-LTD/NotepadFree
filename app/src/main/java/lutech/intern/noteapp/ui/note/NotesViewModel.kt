package lutech.intern.noteapp.ui.note

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lutech.intern.noteapp.common.NoteApplication
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.repository.NoteRepository
import lutech.intern.noteapp.database.NoteDatabase

class NotesViewModel : ViewModel() {
    private val noteRepository by lazy {
        NoteRepository(
            NoteDatabase.getDatabase(NoteApplication.context).noteDao()
        )
    }
    val notes: LiveData<List<Note>> = noteRepository.fetchNotes()

    fun insert(note: Note) = viewModelScope.launch {
        noteRepository.insert(note)
    }
}