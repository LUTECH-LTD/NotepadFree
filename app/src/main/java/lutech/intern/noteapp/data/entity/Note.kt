package lutech.intern.noteapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey(true)
    @ColumnInfo(name = "noteId")
    val noteId: Long = 0L,
    @ColumnInfo(name = "title")
    val title: String? = null,
    @ColumnInfo(name = "content")
    val content: String? = null,
    @ColumnInfo(name = "lastUpdate")
    val lastUpdate: Long = System.currentTimeMillis()
)