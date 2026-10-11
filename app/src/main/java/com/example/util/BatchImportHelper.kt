package com.example.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

object BatchImportHelper {

    /**
     * Recursively scan a folder tree Uri for supported ebook files (.txt, .epub, .docx, .doc).
     * Returns a list of (Uri, fileName) pairs.
     */
    fun scanFolderForBooks(context: Context, treeUri: Uri): List<Pair<Uri, String>> {
        val results = mutableListOf<Pair<Uri, String>>()
        val rootDoc = try {
            DocumentFile.fromTreeUri(context, treeUri)
        } catch (e: Exception) {
            null
        } ?: return emptyList()

        fun scanDir(dir: DocumentFile, depth: Int) {
            if (depth > 5) return // Prevent excessive recursion
            val files = try {
                dir.listFiles()
            } catch (e: Exception) {
                emptyArray()
            }
            for (file in files) {
                if (file.isDirectory) {
                    scanDir(file, depth + 1)
                } else if (file.isFile) {
                    val name = file.name ?: ""
                    val lower = name.lowercase()
                    if (lower.endsWith(".txt") || lower.endsWith(".epub") ||
                        lower.endsWith(".docx") || lower.endsWith(".doc")) {
                        results.add(file.uri to name)
                    }
                }
            }
        }

        scanDir(rootDoc, 0)
        return results
    }
}
