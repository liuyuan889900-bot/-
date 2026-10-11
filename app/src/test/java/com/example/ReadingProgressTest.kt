package com.example

import com.example.data.entity.ReadingProgressEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ReadingProgressTest {

    @Test
    fun testReadingProgressEntityCreationAndValues() {
        val entity = ReadingProgressEntity(
            bookId = 42L,
            chapterIndex = 3,
            paragraphIndex = 15,
            progressPercent = 0.45f,
            charOffsetInParagraph = 120,
            totalParagraphsInChapter = 30,
            lastReadTime = 1690000000000L
        )

        assertEquals(42L, entity.bookId)
        assertEquals(3, entity.chapterIndex)
        assertEquals(15, entity.paragraphIndex)
        assertEquals(0.45f, entity.progressPercent, 0.001f)
        assertEquals(120, entity.charOffsetInParagraph)
        assertEquals(30, entity.totalParagraphsInChapter)
        assertEquals(1690000000000L, entity.lastReadTime)
    }

    @Test
    fun testReadingProgressDefaultValues() {
        val entity = ReadingProgressEntity(
            bookId = 1L
        )

        assertEquals(1L, entity.bookId)
        assertEquals(0, entity.chapterIndex)
        assertEquals(0, entity.paragraphIndex)
        assertEquals(0f, entity.progressPercent, 0.001f)
        assertEquals(0, entity.charOffsetInParagraph)
        assertEquals(1, entity.totalParagraphsInChapter)
        assertNotNull(entity.lastReadTime)
    }
}
