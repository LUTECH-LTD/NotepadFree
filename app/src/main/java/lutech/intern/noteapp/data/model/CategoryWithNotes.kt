package lutech.intern.noteapp.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CategoryWithNotes(
    @Embedded
    val category: Category,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(NoteCategoryJoin::class)
    )
    val notes: List<Note>,
)