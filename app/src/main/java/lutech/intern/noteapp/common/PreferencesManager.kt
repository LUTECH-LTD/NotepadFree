package lutech.intern.noteapp.common

import android.content.Context
import android.content.SharedPreferences
import lutech.intern.noteapp.constant.SortNoteMode

object PreferencesManager {
    private const val PREF_NAME = "SortPreferences"
    private const val KEY_SORT_MODE = "SortMode"
    private val sharedPreferences by lazy {
        NoteApplication.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setSortMode(value: SortNoteMode) {
        sharedPreferences.edit().putString(KEY_SORT_MODE, value.name).apply()
    }

    fun getSortMode(): String {
        return sharedPreferences.getString(KEY_SORT_MODE, SortNoteMode.EDIT_DATE_NEWEST.name) ?: SortNoteMode.CREATION_DATE_NEWEST.name
    }
}