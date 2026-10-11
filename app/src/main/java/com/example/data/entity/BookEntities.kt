package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val coverColorHex: String = "#FF5A00",
    val coverImageRes: Int? = null,
    val format: String = "TXT",
    val category: String = "文学经典",
    val totalChapters: Int = 1,
    val currentChapterIndex: Int = 0,
    val currentProgress: Float = 0f,
    val currentPage: Int = 1,
    val totalPages: Int = 10,
    val lastReadTime: Long = System.currentTimeMillis(),
    val wordCount: Long = 0,
    val intro: String = "",
    val isFavorite: Boolean = false,
    val isPreset: Boolean = false,
    val filePath: String = ""
)

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId"), Index("bookId", "chapterIndex")]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val chapterIndex: Int,
    val title: String,
    val content: String,
    val wordCount: Int = 0
)

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId")]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val chapterIndex: Int,
    val chapterTitle: String,
    val progress: Float,
    val excerpt: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reading_notes",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId")]
)
data class ReadingNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val bookTitle: String,
    val chapterTitle: String,
    val quoteText: String,
    val noteContent: String = "",
    val colorHex: String = "#FFFF5A00",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val searchType: String = "ALL",
    val searchTime: Long = System.currentTimeMillis()
)

/**
 * Reading Progress Entity
 * Stores fine-grained book reading progress including:
 * - Last read paragraph index within the chapter
 * - Chapter index
 * - Character offset within the paragraph
 * - Overall progress percentage
 * - Total paragraphs in chapter
 * - Timestamp of last read activity
 */
@Entity(
    tableName = "reading_progress",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId", unique = true)]
)
data class ReadingProgressEntity(
    @PrimaryKey
    val bookId: Long,
    val chapterIndex: Int = 0,
    val paragraphIndex: Int = 0,
    val progressPercent: Float = 0f,
    val charOffsetInParagraph: Int = 0,
    val totalParagraphsInChapter: Int = 1,
    val lastReadTime: Long = System.currentTimeMillis()
)
