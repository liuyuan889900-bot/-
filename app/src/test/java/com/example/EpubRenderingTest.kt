package com.example

import com.example.data.preset.PresetBooks
import org.junit.Assert.*
import org.junit.Test

class EpubRenderingTest {

    @Test
    fun testPresetCreation() {
        val preset = PresetBooks.defaultBooks.first { it.book.title == "查拉图斯特拉如是说" }
        assertEquals("查拉图斯特拉如是说", preset.book.title)
        assertEquals("弗里德里希·尼采", preset.book.author)
        assertEquals("EPUB", preset.book.format)
        assertTrue(preset.chapters.isNotEmpty())
        assertTrue(preset.chapters.first().title.contains("序言"))
    }

    @Test
    fun testEpubMetaExtractionRegex() {
        val testContent = """<!-- EPUB_META: relPath=OEBPS/Text/chapter1.xhtml;hash=garden_art -->
            |<!DOCTYPE html><html><head><title>Chapter 1</title></head><body><h1>Title</h1></body></html>
        """.trimMargin()

        val metaMatch = Regex("""<!--\s*EPUB_META:\s*relPath=([^;]+);hash=([^\s]+)\s*-->""").find(testContent)
        assertNotNull(metaMatch)
        assertEquals("OEBPS/Text/chapter1.xhtml", metaMatch!!.groupValues[1])
        assertEquals("garden_art", metaMatch.groupValues[2])

        val cleaned = testContent.replace(metaMatch.value, "").trim()
        assertFalse(cleaned.contains("EPUB_META"))
        assertTrue(cleaned.startsWith("<!DOCTYPE html>"))
    }

    @Test
    fun testPaginationProgressCalculation() {
        val chapIdx = 0
        val curPageInChap = 2
        val totalPagesInChap = 4
        val totalChapters = 3

        val chapProg = if (totalPagesInChap <= 1) 1f else (curPageInChap - 1).toFloat() / (totalPagesInChap - 1)
        assertEquals(1f / 3f, chapProg, 0.001f)

        val overallProg = ((chapIdx.toFloat() + (curPageInChap - 1f) / totalPagesInChap.coerceAtLeast(1)) / totalChapters).coerceIn(0f, 1f)
        // (0 + 1/4) / 3 = 1/12 ≈ 0.0833
        assertEquals(0.0833f, overallProg, 0.001f)
    }
}
