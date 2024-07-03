package lutech.intern.noteapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import lutech.intern.noteapp.constant.Constants
import lutech.intern.noteapp.data.model.Category
import lutech.intern.noteapp.data.model.Note
import lutech.intern.noteapp.database.dao.CategoryDao
import lutech.intern.noteapp.database.dao.NoteDao

@Database(entities = [Category::class, Note::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun noteDao(): NoteDao

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
