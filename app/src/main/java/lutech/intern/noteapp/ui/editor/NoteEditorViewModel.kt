package lutech.intern.noteapp.ui.editor

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lutech.intern.noteapp.common.NoteApplication
import lutech.intern.noteapp.constant.Constants
import lutech.intern.noteapp.data.IndexRange
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.entity.CategoryWithNotes
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.NoteWithTextStyles
import lutech.intern.noteapp.data.entity.TextStyles
import lutech.intern.noteapp.data.entity.relations.NoteCategoryCrossRef
import lutech.intern.noteapp.data.repository.CategoryRepository
import lutech.intern.noteapp.data.repository.NoteCategoryCrossRefRepository
import lutech.intern.noteapp.data.repository.NoteRepository
import lutech.intern.noteapp.data.repository.TextStylesRepository
import lutech.intern.noteapp.database.NoteDatabase
import lutech.intern.noteapp.database.dao.TextStylesDao

class NoteEditorViewModel : ViewModel() {
    private var noteRepository: NoteRepository
    private var categoryRepository: CategoryRepository
    private var noteCategoryCrossRepository: NoteCategoryCrossRefRepository
    private var textStylesRepository: TextStylesRepository

    init {
        val noteDatabase = NoteDatabase.getDatabase(NoteApplication.context)
        val noteDao = noteDatabase.noteDao()
        val categoryDao = noteDatabase.categoryDao()
        val noteCategoryCrossRefDao = noteDatabase.noteCategoryCrossRefDao()
        val textStylesDao = noteDatabase.textStylesDao()

        noteRepository = NoteRepository(noteDao)
        categoryRepository = CategoryRepository(categoryDao)
        noteCategoryCrossRepository = NoteCategoryCrossRefRepository(noteCategoryCrossRefDao)
        textStylesRepository = TextStylesRepository(textStylesDao)
    }

    val categoryWithNotes: LiveData<List<CategoryWithNotes>> =
        categoryRepository.getCategoryWithNotes()
    val categories: LiveData<List<Category>> = categoryRepository.getCategories()
    private val _note = MutableLiveData<Note>()
    val note: LiveData<Note> = _note
    private val _indexRanges = MutableLiveData<List<IndexRange>>(emptyList())
    val indexRanges: LiveData<List<IndexRange>> = _indexRanges
    private val _isFormattingBarShow = MutableLiveData(false)
    val isFormattingBarShow: LiveData<Boolean> = _isFormattingBarShow

    fun deleteNote(note: Note) = viewModelScope.launch {
        noteRepository.delete(note)
    }

    fun insertNoteCategoryCrossRef(noteCategoryCrossRef: NoteCategoryCrossRef) =
        viewModelScope.launch {
            noteCategoryCrossRepository.insert(noteCategoryCrossRef)
        }

    fun deleteNoteCategoryCrossRef(noteId: Long, categoryId: Long) = viewModelScope.launch {
        noteCategoryCrossRepository.delete(noteId, categoryId)
    }

    fun getNoteById(noteId: Long) {
        _note.value = noteRepository.getNoteById(noteId)
    }

    fun searchContentNote(content: String, query: String) {
        var indexRanges = mutableListOf<IndexRange>()
        if (query.isNotEmpty()) {
            var start = content.indexOf(query, ignoreCase = true)
            while (start != -1) {
                val end = start + query.length
                indexRanges.add(IndexRange(start, end))
                start = content.indexOf(query, end)
            }
        }
        _indexRanges.value = indexRanges
    }

    fun showFormattingBar() {
        _isFormattingBarShow.value = true
    }

    fun hideFormattingBar() {
        _isFormattingBarShow.value = false
    }

    // new fun
    private val _noteWithTextStyles = MutableLiveData<NoteWithTextStyles>()
    val noteWithTextStyles: LiveData<NoteWithTextStyles> = _noteWithTextStyles
    fun getNoteWithTextStylesById(noteId: Long) {
        val noteWithTextStyles = noteRepository.getNoteWithTextStylesById(noteId)
        noteWithTextStyles?.let {
            _noteWithTextStyles.value = it
        }
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        noteRepository.update(note)
    }

    fun updateNoteColor(noteId: Long, color: String) = viewModelScope.launch {
        noteRepository.updateNoteColor(noteId, color)
        getNoteWithTextStylesById(noteId)
    }

    fun deleteAllTextStyles() = viewModelScope.launch {
        textStylesRepository.deleteAll()
    }

    fun insertTextStyles(textStyles: TextStyles) = viewModelScope.launch {
        textStylesRepository.insert(textStyles)
    }

    fun deleteTextStylesByNoteId(noteId: Long) = viewModelScope.launch {
        textStylesRepository.deleteByNoteId(noteId)
    }
}