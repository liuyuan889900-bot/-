package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.BookmarkEntity
import com.example.data.entity.BookEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.ReadingNoteEntity
import com.example.data.entity.ReadingProgressEntity
import com.example.data.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY lastReadTime DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: Long): BookEntity?

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    fun observeBookById(id: Long): Flow<BookEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: Long)

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' ORDER BY lastReadTime DESC")
    suspend fun searchBooksByTitleOrAuthor(query: String): List<BookEntity>

    // Chapters
    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterIndex ASC")
    fun getChaptersForBook(bookId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterIndex ASC")
    suspend fun getChaptersListForBook(bookId: Long): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId AND chapterIndex = :index LIMIT 1")
    suspend fun getChapter(bookId: Long, index: Int): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteChaptersForBook(bookId: Long)

    @Query("""
        SELECT c.*, b.title AS bookTitle, b.author AS bookAuthor 
        FROM chapters c 
        INNER JOIN books b ON c.bookId = b.id 
        WHERE c.title LIKE '%' || :query || '%' 
        ORDER BY c.bookId ASC, c.chapterIndex ASC
    """)
    suspend fun searchChaptersByTitle(query: String): List<ChapterWithBookInfo>

    @Query("""
        SELECT c.*, b.title AS bookTitle, b.author AS bookAuthor 
        FROM chapters c 
        INNER JOIN books b ON c.bookId = b.id 
        WHERE c.content LIKE '%' || :query || '%' 
        LIMIT 100
    """)
    suspend fun searchChaptersByContent(query: String): List<ChapterWithBookInfo>

    // Bookmarks
    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId ORDER BY createdAt DESC")
    fun getBookmarksForBook(bookId: Long): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Long)

    // Notes
    @Query("SELECT * FROM reading_notes ORDER BY createdAt DESC")
    fun getAllReadingNotes(): Flow<List<ReadingNoteEntity>>

    @Query("SELECT * FROM reading_notes WHERE bookId = :bookId ORDER BY createdAt DESC")
    fun getNotesForBook(bookId: Long): Flow<List<ReadingNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingNote(note: ReadingNoteEntity): Long

    @Query("DELETE FROM reading_notes WHERE id = :id")
    suspend fun deleteReadingNote(id: Long)

    // Search History
    @Query("SELECT * FROM search_history ORDER BY searchTime DESC LIMIT 20")
    fun getRecentSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchHistory(item: SearchHistoryEntity): Long

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteSearchHistoryItem(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBookCount(): Int

    @Query("""
        SELECT c.*, b.title AS bookTitle, b.author AS bookAuthor 
        FROM chapters c 
        INNER JOIN books b ON c.bookId = b.id 
        ORDER BY c.bookId ASC, c.chapterIndex ASC
    """)
    suspend fun getAllChaptersWithBookInfo(): List<ChapterWithBookInfo>

    // Reading Progress (Paragraph-Level Tracking)
    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId LIMIT 1")
    fun getReadingProgressFlow(bookId: Long): Flow<ReadingProgressEntity?>

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId LIMIT 1")
    suspend fun getReadingProgress(bookId: Long): ReadingProgressEntity?

    @Query("SELECT * FROM reading_progress ORDER BY lastReadTime DESC")
    fun getAllReadingProgress(): Flow<List<ReadingProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReadingProgress(progress: ReadingProgressEntity)

    @Query("DELETE FROM reading_progress WHERE bookId = :bookId")
    suspend fun deleteReadingProgress(bookId: Long)
}

data class ChapterWithBookInfo(
    val id: Long,
    val bookId: Long,
    val chapterIndex: Int,
    val title: String,
    val content: String,
    val wordCount: Int,
    val bookTitle: String,
    val bookAuthor: String
)
