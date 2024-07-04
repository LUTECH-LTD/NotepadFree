package lutech.intern.noteapp.data.repository

import lutech.intern.noteapp.data.entity.relations.NoteCategoryCrossRef
import lutech.intern.noteapp.database.dao.NoteCategoryCrossRefDao

class NoteCategoryCrossRefRepository(private val noteCategoryCrossRefDao: NoteCategoryCrossRefDao) {
    suspend fun insert(noteCategoryCrossRef: NoteCategoryCrossRef) {
        noteCategoryCrossRefDao.insert(noteCategoryCrossRef)
    }
}