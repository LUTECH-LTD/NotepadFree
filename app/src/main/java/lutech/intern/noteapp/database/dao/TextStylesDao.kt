package lutech.intern.noteapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import lutech.intern.noteapp.data.entity.TextStyles

@Dao
interface TextStylesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(textStyle: TextStyles)
}