package lutech.intern.noteapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "Note")
data class Note(
    @PrimaryKey(true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "title")
    val title: String? = null,
    @ColumnInfo(name = "content")
    val content: String? = null,
    @ColumnInfo(name = "lastUpdate")
    val lastUpdate: Long = System.currentTimeMillis()
)