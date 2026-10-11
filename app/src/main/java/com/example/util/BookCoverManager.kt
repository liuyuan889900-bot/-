package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.entity.BookEntity
import java.io.File

object BookCoverManager {

    private const val TAG = "COVER_CACHE"

    fun getCoverFile(context: Context, book: BookEntity): File? {
        // Pure black and white minimalist theme: cover images are suppressed in favor of pure typography
        return null
    }

    fun saveCoverImage(context: Context, bookId: Long, imageBytes: ByteArray): File? {
        return try {
            val coversDir = File(context.filesDir, "covers")
            if (!coversDir.exists()) coversDir.mkdirs()

            val coverFile = File(coversDir, "book_${bookId}.jpg")
            coverFile.writeBytes(imageBytes)
            Log.i(TAG, "[Cache Write] bookId=$bookId, key=book_${bookId}.jpg, written=${imageBytes.size}B, path=${coverFile.absolutePath}")
            coverFile
        } catch (e: Exception) {
            Log.e(TAG, "[Cache Write Error] bookId=$bookId", e)
            null
        }
    }

    private fun isImageFile(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
                lower.endsWith(".webp") || lower.endsWith(".gif")
    }
}

