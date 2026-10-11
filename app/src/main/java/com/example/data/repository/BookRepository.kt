package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.dao.BookDao
import com.example.data.dao.ChapterWithBookInfo
import com.example.data.entity.BookmarkEntity
import com.example.data.entity.BookEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.ReadingNoteEntity
import com.example.data.entity.ReadingProgressEntity
import com.example.data.entity.SearchHistoryEntity
import com.example.data.preset.PresetBooks
import com.example.util.SearchEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class SearchResultSnippet(
    val bookId: Long,
    val bookTitle: String,
    val bookAuthor: String,
    val chapterIndex: Int,
    val chapterTitle: String,
    val snippetText: String,
    val matchCount: Int = 1,
    val matchIndexInChapter: Int = 0,
    val charOffset: Int = 0,
    val isFuzzyMatch: Boolean = false,
    val similarityScore: Float = 1.0f,
    val matchLabel: String = "精确匹配",
    val estimatedPage: Int = 1
)

class BookRepository(
    private val bookDao: BookDao,
    val context: Context
) {
    val allBooks: Flow<List<BookEntity>> = bookDao.getAllBooks()
    val searchHistory: Flow<List<SearchHistoryEntity>> = bookDao.getRecentSearchHistory()
    val allNotes: Flow<List<ReadingNoteEntity>> = bookDao.getAllReadingNotes()

    // In-memory cache for ultra-fast title matching
    private var cachedBooks: List<BookEntity> = emptyList()

    suspend fun initializePresetIfNeeded() = withContext(Dispatchers.IO) {
        val existing = bookDao.getAllBooks().first()
        
        // Clean out legacy covers directory on disk
        try {
            val coversDir = java.io.File(context.filesDir, "covers")
            if (coversDir.exists()) {
                coversDir.deleteRecursively()
            }
        } catch (_: Exception) {}

        val currentPresetTitles = PresetBooks.defaultBooks.map { it.book.title }.toSet()
        // Remove old preset books that are no longer part of defaultBooks
        existing.filter { it.isPreset && !currentPresetTitles.contains(it.title) }.forEach { oldBook ->
            bookDao.deleteBook(oldBook)
        }

        PresetBooks.defaultBooks.forEach { preset ->
            val existingBook = existing.find { it.title == preset.book.title }
            if (existingBook == null || existingBook.wordCount < preset.book.wordCount || existingBook.format != preset.book.format) {
                if (existingBook != null) {
                    bookDao.deleteBook(existingBook)
                }
                val bookToInsert = preset.book.copy(id = 0, filePath = "")
                val bookId = bookDao.insertBook(bookToInsert)

                val chaptersWithId = preset.chapters.map { chapter ->
                    chapter.copy(id = 0, bookId = bookId)
                }
                bookDao.insertChapters(chaptersWithId)
            }
        }
        // Warm up cache
        cachedBooks = bookDao.getAllBooks().first()
    }

    suspend fun getBookById(id: Long): BookEntity? = withContext(Dispatchers.IO) {
        bookDao.getBookById(id)
    }

    fun observeBookById(id: Long): Flow<BookEntity?> = bookDao.observeBookById(id)

    fun getChaptersForBook(bookId: Long): Flow<List<ChapterEntity>> = bookDao.getChaptersForBook(bookId)

    suspend fun getChaptersListForBook(bookId: Long): List<ChapterEntity> = withContext(Dispatchers.IO) {
        bookDao.getChaptersListForBook(bookId)
    }

    suspend fun getChapter(bookId: Long, index: Int): ChapterEntity? = withContext(Dispatchers.IO) {
        bookDao.getChapter(bookId, index)
    }

    suspend fun replaceChaptersForBook(bookId: Long, newChapters: List<ChapterEntity>) = withContext(Dispatchers.IO) {
        bookDao.deleteChaptersForBook(bookId)
        bookDao.insertChapters(newChapters)
        val book = bookDao.getBookById(bookId)
        if (book != null) {
            val totalWords = newChapters.sumOf { it.wordCount.toLong() }
            val updated = book.copy(
                totalChapters = newChapters.size.coerceAtLeast(1),
                wordCount = totalWords,
                totalPages = (totalWords / 400).toInt().coerceAtLeast(5)
            )
            bookDao.updateBook(updated)
            cachedBooks = bookDao.getAllBooks().first()
        }
    }

    suspend fun updateBookProgress(
        bookId: Long,
        chapterIndex: Int,
        progress: Float,
        currentPage: Int = 1,
        totalPages: Int = 10
    ) = withContext(Dispatchers.IO) {
        val book = bookDao.getBookById(bookId) ?: return@withContext
        val updated = book.copy(
            currentChapterIndex = chapterIndex,
            currentProgress = progress.coerceIn(0f, 1f),
            currentPage = currentPage,
            totalPages = totalPages,
            lastReadTime = System.currentTimeMillis()
        )
        bookDao.updateBook(updated)
    }

    suspend fun updateBookCategory(bookId: Long, category: String) = withContext(Dispatchers.IO) {
        val book = bookDao.getBookById(bookId) ?: return@withContext
        bookDao.updateBook(book.copy(category = category))
    }

    suspend fun toggleFavorite(bookId: Long) = withContext(Dispatchers.IO) {
        val book = bookDao.getBookById(bookId) ?: return@withContext
        bookDao.updateBook(book.copy(isFavorite = !book.isFavorite))
    }

    suspend fun deleteBook(bookId: Long) = withContext(Dispatchers.IO) {
        bookDao.deleteBookById(bookId)
    }

    // ================= READING PROGRESS TRACKING (PARAGRAPH LEVEL) =================

    val allReadingProgress: Flow<List<ReadingProgressEntity>> = bookDao.getAllReadingProgress()

    fun getReadingProgress(bookId: Long): Flow<ReadingProgressEntity?> =
        bookDao.getReadingProgressFlow(bookId)

    suspend fun getReadingProgressOnce(bookId: Long): ReadingProgressEntity? = withContext(Dispatchers.IO) {
        bookDao.getReadingProgress(bookId)
    }

    suspend fun saveReadingProgress(
        bookId: Long,
        chapterIndex: Int,
        paragraphIndex: Int,
        progressPercent: Float = 0f,
        charOffsetInParagraph: Int = 0,
        totalParagraphsInChapter: Int = 1
    ) = withContext(Dispatchers.IO) {
        val entity = ReadingProgressEntity(
            bookId = bookId,
            chapterIndex = chapterIndex,
            paragraphIndex = paragraphIndex,
            progressPercent = progressPercent.coerceIn(0f, 1f),
            charOffsetInParagraph = charOffsetInParagraph,
            totalParagraphsInChapter = totalParagraphsInChapter,
            lastReadTime = System.currentTimeMillis()
        )
        bookDao.saveReadingProgress(entity)
        // Also keep book's current chapter and last read timestamp in sync
        val book = bookDao.getBookById(bookId)
        if (book != null) {
            bookDao.updateBook(
                book.copy(
                    currentChapterIndex = chapterIndex,
                    currentProgress = progressPercent.coerceIn(0f, 1f),
                    lastReadTime = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deleteReadingProgress(bookId: Long) = withContext(Dispatchers.IO) {
        bookDao.deleteReadingProgress(bookId)
    }

    // ================= REAL-TIME SEARCH METHODS =================

    // 1. Ultra-fast instantaneous Title/Author filter (memory first, fallback to DB)
    suspend fun searchBooksByTitleInstant(query: String): List<BookEntity> = withContext(Dispatchers.Default) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return@withContext emptyList()
        
        // Fast memory filter
        if (cachedBooks.isNotEmpty()) {
            val matched = cachedBooks.filter { 
                it.title.lowercase().contains(q) || it.author.lowercase().contains(q)
            }
            if (matched.isNotEmpty()) return@withContext matched
        }
        // DB fallback
        withContext(Dispatchers.IO) {
            bookDao.searchBooksByTitleOrAuthor(query.trim())
        }
    }

    // 2. Fast TOC (Table of Contents) Chapter Title Search with Fuzzy & Subsequence Matching
    suspend fun searchChaptersByTitle(query: String): List<ChapterWithBookInfo> = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isEmpty()) return@withContext emptyList()
        
        // Exact DB query first
        val directResults = bookDao.searchChaptersByTitle(q).toMutableList()
        val directIds = directResults.map { it.id }.toSet()

        // Supplement with fuzzy and subsequence scoring over all chapters
        val allChapters = bookDao.getAllChaptersWithBookInfo()
        val scoredOtherChapters = mutableListOf<Pair<ChapterWithBookInfo, Float>>()

        for (chap in allChapters) {
            if (chap.id in directIds) continue
            val score = SearchEngine.matchChapterTitleScore(chap.title, q)
            if (score >= 0.55f) {
                scoredOtherChapters.add(chap to score)
            }
        }

        scoredOtherChapters.sortByDescending { it.second }
        directResults.addAll(scoredOtherChapters.map { it.first })
        directResults
    }

    // 3. Full-text Content Search with Paragraph Matching & Fuzzy Tolerant Snippets
    suspend fun searchContentWithSnippets(query: String): List<SearchResultSnippet> = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isEmpty()) return@withContext emptyList()

        val results = mutableListOf<SearchResultSnippet>()
        val maxTotalResults = 120

        // If query is very short or simple, try DB query first for speed
        val initialMatchedChapters = if (q.length <= 15) {
            bookDao.searchChaptersByContent(q)
        } else {
            emptyList()
        }

        val processedChapterIds = mutableSetOf<Long>()

        for (chapter in initialMatchedChapters) {
            if (results.size >= maxTotalResults) break
            processedChapterIds.add(chapter.id)
            val hits = SearchEngine.searchInChapterContent(
                bookId = chapter.bookId,
                bookTitle = chapter.bookTitle,
                bookAuthor = chapter.bookAuthor,
                chapterIndex = chapter.chapterIndex,
                chapterTitle = chapter.title,
                content = chapter.content,
                rawQuery = q,
                maxMatchesPerChapter = 5
            )
            results.addAll(hits)
        }

        // If query is long (> 10 chars) or we found few matches, scan all chapters for
        // paragraph matching, multi-clause proximity, and fuzzy matching
        if (q.length > 10 || results.size < 5) {
            val allChapters = bookDao.getAllChaptersWithBookInfo()
            for (chapter in allChapters) {
                if (results.size >= maxTotalResults) break
                if (chapter.id in processedChapterIds) continue

                val hits = SearchEngine.searchInChapterContent(
                    bookId = chapter.bookId,
                    bookTitle = chapter.bookTitle,
                    bookAuthor = chapter.bookAuthor,
                    chapterIndex = chapter.chapterIndex,
                    chapterTitle = chapter.title,
                    content = chapter.content,
                    rawQuery = q,
                    maxMatchesPerChapter = 5
                )
                if (hits.isNotEmpty()) {
                    results.addAll(hits)
                }
            }
        }

        // Sort: Exact matches first, then highest similarity score
        results.sortedWith(
            compareByDescending<SearchResultSnippet> { if (!it.isFuzzyMatch) 1 else 0 }
                .thenByDescending { it.similarityScore }
        )
    }

    // In-Book search: search within a specific book's chapters, extracting all distinct matches
    suspend fun searchInBook(bookId: Long, query: String): List<SearchResultSnippet> = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isEmpty()) return@withContext emptyList()
        val book = bookDao.getBookById(bookId) ?: return@withContext emptyList()
        val chapters = bookDao.getChaptersListForBook(bookId)
        val results = mutableListOf<SearchResultSnippet>()
        val maxTotalResults = 200

        for (chapter in chapters) {
            if (results.size >= maxTotalResults) break
            val hits = SearchEngine.searchInChapterContent(
                bookId = bookId,
                bookTitle = book.title,
                bookAuthor = book.author,
                chapterIndex = chapter.chapterIndex,
                chapterTitle = chapter.title,
                content = chapter.content,
                rawQuery = q,
                maxMatchesPerChapter = 25
            )
            results.addAll(hits)
        }

        results.sortedWith(
            compareByDescending<SearchResultSnippet> { if (!it.isFuzzyMatch) 1 else 0 }
                .thenByDescending { it.similarityScore }
        )
    }

    // Bookmarks & Notes
    fun getBookmarksForBook(bookId: Long): Flow<List<BookmarkEntity>> = bookDao.getBookmarksForBook(bookId)

    suspend fun addBookmark(
        bookId: Long,
        chapterIndex: Int,
        chapterTitle: String,
        progress: Float,
        excerpt: String
    ) = withContext(Dispatchers.IO) {
        bookDao.insertBookmark(
            BookmarkEntity(
                bookId = bookId,
                chapterIndex = chapterIndex,
                chapterTitle = chapterTitle,
                progress = progress,
                excerpt = excerpt
            )
        )
    }

    suspend fun deleteBookmark(id: Long) = withContext(Dispatchers.IO) {
        bookDao.deleteBookmark(id)
    }

    suspend fun addNote(
        bookId: Long,
        bookTitle: String,
        chapterTitle: String,
        quoteText: String,
        noteContent: String = "",
        colorHex: String = "#FFFF5A00"
    ) = withContext(Dispatchers.IO) {
        bookDao.insertReadingNote(
            ReadingNoteEntity(
                bookId = bookId,
                bookTitle = bookTitle,
                chapterTitle = chapterTitle,
                quoteText = quoteText,
                noteContent = noteContent,
                colorHex = colorHex
            )
        )
    }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        bookDao.deleteReadingNote(id)
    }

    // Search History
    suspend fun saveSearchHistory(query: String, type: String = "ALL") = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext
        bookDao.deleteSearchHistoryItem(query.trim())
        bookDao.insertSearchHistory(
            SearchHistoryEntity(
                query = query.trim(),
                searchType = type
            )
        )
    }

    suspend fun deleteSearchHistoryItem(query: String) = withContext(Dispatchers.IO) {
        bookDao.deleteSearchHistoryItem(query)
    }

    suspend fun clearSearchHistory() = withContext(Dispatchers.IO) {
        bookDao.clearSearchHistory()
    }

    // Import Book from Local File (Uri or Text, supporting TXT, DOCX, EPUB)
    suspend fun importLocalTextBook(
        title: String,
        author: String,
        content: String,
        category: String = "导入文档"
    ): Long = withContext(Dispatchers.IO) {
        val chapters = parseTextIntoChapters(content, title)
        val wordCount = content.length.toLong()
        val book = BookEntity(
            title = title,
            author = if (author.isBlank()) "佚名" else author,
            coverColorHex = "#C25B38",
            format = "TXT",
            category = category,
            totalChapters = chapters.size.coerceAtLeast(1),
            currentChapterIndex = 0,
            currentProgress = 0f,
            currentPage = 1,
            totalPages = (wordCount / 400).toInt().coerceAtLeast(5),
            wordCount = wordCount,
            intro = "导入的本地书籍《$title》",
            isFavorite = false,
            isPreset = false
        )
        val bookId = bookDao.insertBook(book)
        val chapterEntities = chapters.mapIndexed { idx, (chapTitle, chapContent) ->
            val sanitized = com.example.util.CharsetDetector.healAndSanitize(chapContent)
            ChapterEntity(
                bookId = bookId,
                chapterIndex = idx,
                title = com.example.util.normalizeChapterTitle(chapTitle, idx + 1),
                content = sanitized,
                wordCount = sanitized.length
            )
        }
        bookDao.insertChapters(chapterEntities)
        // Refresh cache
        cachedBooks = bookDao.getAllBooks().first()
        bookId
    }

    suspend fun importFileUri(uri: Uri, fileName: String): Long = withContext(Dispatchers.IO) {
        val actualFileName = com.example.util.BookParser.getFileName(context, uri).ifBlank { fileName }
        val parsed = com.example.util.BookParser.parse(context, uri, actualFileName)
        
        val book = BookEntity(
            title = parsed.title,
            author = parsed.author,
            coverColorHex = when (parsed.format) {
                "EPUB" -> "#2E6B8C"
                "DOCX" -> "#2B579A"
                else -> "#C25B38"
            },
            filePath = parsed.coverFilePath ?: "",
            format = parsed.format,
            category = "导入文档",
            totalChapters = parsed.chapters.size.coerceAtLeast(1),
            currentChapterIndex = 0,
            currentProgress = 0f,
            currentPage = 1,
            totalPages = (parsed.totalWordCount / 400).toInt().coerceAtLeast(5),
            wordCount = parsed.totalWordCount,
            intro = parsed.intro,
            isFavorite = false,
            isPreset = false
        )
        val bookId = bookDao.insertBook(book)
        var finalCoverPath = ""
        if (!parsed.coverFilePath.isNullOrBlank()) {
            try {
                val srcFile = java.io.File(parsed.coverFilePath)
                if (srcFile.exists() && srcFile.length() > 0) {
                    val coverFile = java.io.File(context.filesDir, "covers/book_${bookId}.jpg")
                    coverFile.parentFile?.mkdirs()
                    srcFile.copyTo(coverFile, overwrite = true)
                    finalCoverPath = coverFile.absolutePath
                    if (srcFile.absolutePath != coverFile.absolutePath) {
                        srcFile.delete()
                    }
                    android.util.Log.i("EPUB_COVER", "[Import Cover] bookId=$bookId, title='${book.title}', destination=${coverFile.absolutePath}, size=${coverFile.length()}B")
                }
            } catch (e: Exception) {
                android.util.Log.e("EPUB_COVER", "[Import Cover Error] bookId=$bookId, title='${book.title}'", e)
            }
        }
        // If format is TXT, save raw bytes into books_txt directory so user can change encoding anytime!
        var savedRawFilePath = ""
        if (parsed.format.equals("TXT", ignoreCase = true)) {
            try {
                val txtDir = java.io.File(context.filesDir, "books_txt")
                txtDir.mkdirs()
                val destTxtFile = java.io.File(txtDir, "book_${bookId}.txt")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destTxtFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                if (destTxtFile.exists() && destTxtFile.length() > 0) {
                    savedRawFilePath = destTxtFile.absolutePath
                }
            } catch (e: Exception) {
                android.util.Log.e("TXT_SAVE", "Failed to preserve raw TXT file for bookId=$bookId", e)
            }
        }

        val effectiveFilePath = when {
            savedRawFilePath.isNotBlank() -> savedRawFilePath
            finalCoverPath.isNotBlank() -> finalCoverPath
            else -> ""
        }

        if (effectiveFilePath.isNotBlank()) {
            val updatedBook = book.copy(id = bookId, filePath = effectiveFilePath)
            bookDao.updateBook(updatedBook)
        }
        val chapterEntities = parsed.chapters.mapIndexed { idx, chap ->
            val sanitized = com.example.util.CharsetDetector.healAndSanitize(chap.content)
            ChapterEntity(
                bookId = bookId,
                chapterIndex = idx,
                title = com.example.util.normalizeChapterTitle(chap.title, idx + 1),
                content = sanitized,
                wordCount = sanitized.length
            )
        }
        bookDao.insertChapters(chapterEntities)
        cachedBooks = bookDao.getAllBooks().first()
        bookId
    }

    suspend fun importMultipleUris(
        files: List<Pair<Uri, String>>,
        onProgress: (current: Int, total: Int, name: String) -> Unit
    ): List<Long> = withContext(Dispatchers.IO) {
        val importedIds = mutableListOf<Long>()
        val total = files.size
        for ((index, item) in files.withIndex()) {
            val (uri, fileName) = item
            try {
                onProgress(index + 1, total, fileName)
                val newId = importFileUri(uri, fileName)
                importedIds.add(newId)
            } catch (e: Exception) {
                android.util.Log.e("BatchImport", "Failed to import file: $fileName", e)
            }
        }
        if (importedIds.isNotEmpty()) {
            cachedBooks = bookDao.getAllBooks().first()
        }
        importedIds
    }

    private fun parseTextIntoChapters(rawText: String, bookTitle: String): List<Pair<String, String>> {
        val lines = rawText.lines()
        val result = mutableListOf<Pair<String, String>>()
        val chapterRegex = Regex("""^(第[0-9零一二三四五六七八九十百千]+[章节卷回篇集部].*|Chapter\s+[0-9]+.*|序[言章]|前言|后记|引子|楔子).*""")
        
        var currentTitle = "第一章 $bookTitle"
        val currentBuffer = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.length in 2..35 && chapterRegex.matches(trimmed)) {
                if (currentBuffer.isNotBlank()) {
                    result.add(Pair(currentTitle, currentBuffer.toString().trim()))
                    currentBuffer.clear()
                }
                currentTitle = trimmed
            } else {
                currentBuffer.append(line).append("\n")
            }
        }

        if (currentBuffer.isNotBlank() || result.isEmpty()) {
            result.add(Pair(currentTitle, currentBuffer.toString().trim()))
        }
        return result
    }
}
