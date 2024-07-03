package lutech.intern.noteapp.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import lutech.intern.noteapp.data.entity.relations.NoteCategoryCrossRef

data class CategoryWithNotes(
    @Embedded
    val category: Category,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "noteId",
        associateBy = Junction(NoteCategoryCrossRef::class)
    )
    val notes: List<Note>,
)