package lutech.intern.noteapp.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lutech.intern.noteapp.common.NoteApplication
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.repository.NoteRepository
import lutech.intern.noteapp.database.NoteDatabase

class NoteEditorViewModel : ViewModel() {
    private val noteRepository by lazy {
        NoteRepository(
            NoteDatabase.getDatabase(NoteApplication.context).noteDao()
        )
    }

    fun update(note: Note) = viewModelScope.launch {
        noteRepository.update(note)
    }
}