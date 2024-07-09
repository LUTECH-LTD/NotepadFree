package lutech.intern.noteapp.ui.note

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lutech.intern.noteapp.common.NoteApplication
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.NoteWithCategories
import lutech.intern.noteapp.data.entity.relations.NoteCategoryCrossRef
import lutech.intern.noteapp.data.repository.CategoryRepository
import lutech.intern.noteapp.data.repository.NoteCategoryCrossRefRepository
import lutech.intern.noteapp.data.repository.NoteRepository
import lutech.intern.noteapp.database.NoteDatabase

class NotesViewModel : ViewModel() {
    private var noteRepository: NoteRepository
    private var categoryRepository: CategoryRepository
    private var noteCategoryCrossRepository: NoteCategoryCrossRefRepository

    init {
        val noteDatabase = NoteDatabase.getDatabase(NoteApplication.context)
        val noteDao = noteDatabase.noteDao()
        val categoryDao = noteDatabase.categoryDao()
        val noteCategoryCrossRefDao = noteDatabase.noteCategoryCrossRefDao()

        noteRepository = NoteRepository(noteDao)
        categoryRepository = CategoryRepository(categoryDao)
        noteCategoryCrossRepository = NoteCategoryCrossRefRepository(noteCategoryCrossRefDao)
    }

    val noteWithCategories: LiveData<List<NoteWithCategories>> =  noteRepository.getNoteWithCategories()
    val categories: LiveData<List<Category>> = categoryRepository.getCategories()

    fun insertNote(note: Note, callback: (Note) -> Unit) = viewModelScope.launch {
        val noteId = noteRepository.insert(note)
        val noteWithNoteId = noteRepository.getNoteById(noteId)
        noteWithNoteId?.let {
            callback(it)
        }
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        noteRepository.update(note)
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        noteRepository.delete(note)
    }

    fun insertNoteCategoryCrossRef(noteCategoryCrossRef: NoteCategoryCrossRef) = viewModelScope.launch {
        noteCategoryCrossRepository.insert(noteCategoryCrossRef)
    }
}