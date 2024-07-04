package lutech.intern.noteapp.data.entity.relations

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.entity.Note

@Entity(
    primaryKeys = ["noteId", "categoryId"],
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["noteId"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["noteId"]), Index(value = ["categoryId"])]
)
data class NoteCategoryCrossRef(
    @ColumnInfo(name = "noteId")
    val noteId: Long,
    @ColumnInfo(name = "categoryId")
    val categoryId: Long
)

