package lutech.intern.noteapp.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.widget.Toast
import lutech.intern.noteapp.data.entity.Note
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class FileManager(private val context: Context) {
    fun exportFileToFolder(folderUri: Uri, fileName: String, fileContents: String) {
        val contentResolver: ContentResolver = context.contentResolver

        val documentUri = DocumentsContract.buildDocumentUriUsingTree(
            folderUri,
            DocumentsContract.getTreeDocumentId(folderUri)
        )

        val newFileUri = DocumentsContract.createDocument(
            contentResolver,
            documentUri,
            "text/plain",
            fileName
        )

        try {
            newFileUri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(fileContents.toByteArray())
                    outputStream.close()
                    Toast.makeText(context, "File saved successfully", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(context, "Failed to create file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
        }
    }

    fun importFileFromFolder(fileUri: Uri): Note? {
        val contentResolver: ContentResolver = context.contentResolver
        var note: Note? = null

        try {
            val displayName = getFileNameFromUri(fileUri)
            contentResolver.openInputStream(fileUri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val content = reader.readText()
                    note = Note(
                        title = displayName,
                        content = content
                    )
                }
            }
            Toast.makeText(context, "File imported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to import file", Toast.LENGTH_SHORT).show()
        }

        return note
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = "Untitled"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayName = cursor.getString(cursor.getColumnIndexOrThrow("_display_name"))
                if (!displayName.isNullOrEmpty()) {
                    fileName = displayName
                }
            }
        }
        return fileName
    }
}