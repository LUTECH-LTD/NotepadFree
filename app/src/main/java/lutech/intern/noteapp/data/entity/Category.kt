package lutech.intern.noteapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Category(
    @PrimaryKey(true)
    @ColumnInfo(name = "categoryId")
    val categoryId: Long = 0L,
    @ColumnInfo(name = "name")
    val name: String,
)
