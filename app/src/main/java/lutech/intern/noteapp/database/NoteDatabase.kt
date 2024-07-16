package lutech.intern.noteapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import lutech.intern.noteapp.constant.Constants
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.TextStyles
import lutech.intern.noteapp.data.entity.relations.NoteCategoryCrossRef
import lutech.intern.noteapp.database.dao.CategoryDao
import lutech.intern.noteapp.database.dao.NoteCategoryCrossRefDao
import lutech.intern.noteapp.database.dao.NoteDao
import lutech.intern.noteapp.database.dao.TextStylesDao

@Database(entities = [Category::class, Note::class, NoteCategoryCrossRef::class, TextStyles::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun noteDao(): NoteDao
    abstract fun noteCategoryCrossRefDao(): NoteCategoryCrossRefDao
    abstract fun textStylesDao(): TextStylesDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    Constants.DB_NAME
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
