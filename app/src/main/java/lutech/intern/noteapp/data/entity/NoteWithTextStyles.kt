package lutech.intern.noteapp.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import lutech.intern.noteapp.data.entity.relations.NoteCategoryCrossRef

data class NoteWithTextStyles(
    @Embedded var note: Note,
    @Relation(
        parentColumn = "noteId",
        entityColumn = "noteId",
    )
    var textStyles: List<TextStyles>
)
