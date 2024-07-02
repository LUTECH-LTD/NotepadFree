package lutech.intern.noteapp.common

import android.app.Application
import lutech.intern.noteapp.database.NoteDatabase

class NoteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NoteDatabase.getDatabase(this).openHelper.readableDatabase
    }
}