package lutech.intern.noteapp.ui.note

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lutech.intern.noteapp.common.NoteApplication
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.NoteWithCategories
import lutech.intern.noteapp.data.entity.relations.NoteCategoryCrossRef
import lutech.intern.noteapp.data.repository.NoteCategoryCrossRefRepository
import lutech.intern.noteapp.data.repository.NoteRepository
import lutech.intern.noteapp.database.NoteDatabase

class NotesViewModel : ViewModel() {
    private val noteRepository by lazy {
        NoteRepository(
            NoteDatabase.getDatabase(NoteApplication.context).noteDao()
        )
    }
    private val noteCategoryCrossRepository by lazy {
        NoteCategoryCrossRefRepository(
            NoteDatabase.getDatabase(NoteApplication.context).noteCategoryCrossRefDao()
        )
    }

    val noteWithCategories: LiveData<List<NoteWithCategories>> = noteRepository.fetchNoteWithCategories()
    private val _lastInsertedNote = MutableLiveData<Note>()
    val lastInsertedNote = _lastInsertedNote

    fun insert(note: Note) = viewModelScope.launch {
        val noteId = noteRepository.insert(note)
        val noteWithNoteId = noteRepository.fetchNoteById(noteId)
        noteWithNoteId?.let {
            _lastInsertedNote.value = it
        }
    }

    fun insertNoteCategoryCrossRef(noteCategoryCrossRef: NoteCategoryCrossRef) = viewModelScope.launch {
        noteCategoryCrossRepository.insert(noteCategoryCrossRef)
    }
}