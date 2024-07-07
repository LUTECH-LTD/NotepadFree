package lutech.intern.noteapp.common

import android.app.Application
import android.content.Context
import lutech.intern.noteapp.constant.SortNoteMode
import lutech.intern.noteapp.database.NoteDatabase

class NoteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NoteDatabase.getDatabase(this).openHelper.readableDatabase
        context = this
    }

    companion object {
        lateinit var context: Context
    }
}