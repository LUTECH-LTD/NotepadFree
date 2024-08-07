package lutech.intern.noteapp.data.entity

import androidx.core.content.ContextCompat
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import lutech.intern.noteapp.R
import lutech.intern.noteapp.common.NoteApplication
import java.io.Serializable

@Entity
data class Note(
    @PrimaryKey(true)
    @ColumnInfo(name = "noteId")
    val noteId: Long = 0L,
    @ColumnInfo(name = "title")
    val title: String = "",
    @ColumnInfo(name = "content")
    val content: String = "",
    @ColumnInfo(name = "color")
    val color: String = String.format("#%06X", 0xFFFFFF and ContextCompat.getColor(NoteApplication.context, R.color.color_beige)),
    @ColumnInfo(name = "dateCreate")
    val dateCreate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "lastUpdate")
    val lastUpdate: Long = System.currentTimeMillis(),
) : Serializable