package lutech.intern.noteapp.ui.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lutech.intern.noteapp.common.NoteApplication
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.entity.CategoryWithNotes
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.relations.NoteCategoryCrossRef
import lutech.intern.noteapp.data.repository.CategoryRepository
import lutech.intern.noteapp.data.repository.NoteCategoryCrossRefRepository
import lutech.intern.noteapp.data.repository.NoteRepository
import lutech.intern.noteapp.database.NoteDatabase

class NoteEditorViewModel : ViewModel() {
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

    val categoryWithNotes: LiveData<List<CategoryWithNotes>> = categoryRepository.getCategoryWithNotes()
    val categories: LiveData<List<Category>> = categoryRepository.getCategories()
    private val _note = MutableLiveData<Note>()
    val note : LiveData<Note> = _note

    fun updateNote(note: Note) = viewModelScope.launch {
        noteRepository.update(note)
        getNoteById(note.noteId)
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        noteRepository.delete(note)
    }

    fun insertNoteCategoryCrossRef(noteCategoryCrossRef: NoteCategoryCrossRef) = viewModelScope.launch {
        noteCategoryCrossRepository.insert(noteCategoryCrossRef)
    }

    fun deleteNoteCategoryCrossRef(noteId: Long, categoryId: Long) = viewModelScope.launch {
        noteCategoryCrossRepository.delete(noteId, categoryId)
    }

    fun getNoteById(noteId: Long) {
        _note.value = noteRepository.getNoteById(noteId)
    }
}