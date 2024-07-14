package lutech.intern.noteapp.data.repository

import lutech.intern.noteapp.data.entity.TextStyles
import lutech.intern.noteapp.database.dao.TextStylesDao

class TextStylesRepository(private val dao: TextStylesDao) {
    suspend fun insert(textStyle: TextStyles) {
        dao.insert(textStyle)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    suspend fun deleteByNoteId(noteId: Long) {
        dao.deleteByNoteId(noteId)
    }
}