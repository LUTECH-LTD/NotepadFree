package lutech.intern.noteapp.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    fun getFormattedDateTime(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}