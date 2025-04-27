package com.mili.journalapp.util

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    private const val TAG = "FileUtils"

    fun saveNoteToFile(context: Context, content: String, timestamp: Long): File? {
        return try {
            // Check if external storage is available for writing
            if (!isExternalStorageWritable()) {
                Log.e(TAG, "External storage is not writable.")
                return null
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val date = sdf.format(Date(timestamp))
            val fileName = "Note_$date.txt"

            // Create directory if it doesn't exist
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "MyJournalNotes")
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e(TAG, "Failed to create directory.")
                    return null
                }
            }

            // Create file and write content
            val file = File(dir, fileName)
            FileOutputStream(file).use { it.write(content.toByteArray()) }

            Log.d(TAG, "Note saved successfully: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error saving note: ${e.message}", e)
            null
        }
    }

    // Check if external storage is available for writing
    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}
