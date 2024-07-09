package lutech.intern.noteapp.ui.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lutech.intern.noteapp.common.NoteApplication
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.entity.CategoryWithNotes
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.NoteWithCategories
import lutech.intern.noteapp.data.entity.relations.NoteCategoryCrossRef
import lutech.intern.noteapp.data.repository.CategoryRepository
import lutech.intern.noteapp.data.repository.NoteCategoryCrossRefRepository
import lutech.intern.noteapp.data.repository.NoteRepository
import lutech.intern.noteapp.database.NoteDatabase

class NoteEditorViewModel : ViewModel() {
    private val noteRepository by lazy {
        NoteRepository(
            NoteDatabase.getDatabase(NoteApplication.context).noteDao()
        )
    }
    private val categoryRepository by lazy {
        CategoryRepository(
            NoteDatabase.getDatabase(NoteApplication.context).categoryDao()
        )
    }
    private val noteCategoryCrossRepository by lazy {
        NoteCategoryCrossRefRepository(
            NoteDatabase.getDatabase(NoteApplication.context).noteCategoryCrossRefDao()
        )
    }
    val categoryWithNotes: LiveData<List<CategoryWithNotes>> = categoryRepository.getCategoryWithNotes()

    val categories: LiveData<List<Category>> = categoryRepository.getAllCategories()

    fun update(note: Note) = viewModelScope.launch {
        noteRepository.update(note)
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
}