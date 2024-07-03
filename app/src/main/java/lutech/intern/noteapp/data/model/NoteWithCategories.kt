package lutech.intern.noteapp.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class NoteWithCategories(
    @Embedded var note: Note,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(NoteCategoryJoin::class)
    )
    var categories: List<Category>
)
