package lutech.intern.noteapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["noteId"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["noteId"])]
)
data class TextStyles(
    @PrimaryKey(true)
    val textStylesId: Int? = null,
    val start: Int,
    val end: Int,
    val isBold: Boolean,
    val isItalic: Boolean,
    val isUnderline: Boolean,
    val isStrikethrough: Boolean,
    val highlight: String? = null,
    val color: String? = null,
    val noteId: Int
)