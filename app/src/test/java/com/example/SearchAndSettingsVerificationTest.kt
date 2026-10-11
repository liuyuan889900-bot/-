package com.example

import com.example.data.preset.PresetBooks
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.SearchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchAndSettingsVerificationTest {

    @Test
    fun testPresetBooksFormatsAndPages() {
        val books = PresetBooks.defaultBooks
        // User requested 3+ books covering EPUB (x2), GBG (x1), WORD (x1), each with 10~20+ pages
        assertEquals(4, books.size)

        val book1 = books[0].book
        val book2 = books[1].book
        val book3 = books[2].book
        val book4 = books[3].book

        assertEquals("EPUB", book1.format)
        assertTrue(book1.totalPages in 10..30)

        assertEquals("EPUB", book2.format)
        assertTrue(book2.totalPages in 10..30)

        assertEquals("WORD", book3.format)
        assertTrue(book3.totalPages in 10..30)

        assertEquals("GBG", book4.format)
        assertTrue(book4.totalPages in 10..30)
    }

    @Test
    fun testFuzzyTypoSearch_ShengJing() {
        // User example: User searches "胜经" (typo for 圣经)
        val content = """
            查拉图斯特拉在下山之时，遇见林中隐士。隐士问他：
            你在孤独中领悟了神圣的圣经与天道吗？清晨的太阳升起，
            照耀在山巅的白雪上。
        """.trimIndent()

        // Search with typo "胜经"
        val results = SearchEngine.searchInChapterContent(
            bookId = 1L,
            bookTitle = "查拉图斯特拉如是说",
            bookAuthor = "尼采",
            chapterIndex = 0,
            chapterTitle = "第一章",
            content = content,
            rawQuery = "胜经"
        )

        assertTrue("Should find match for typo '胜经'", results.isNotEmpty())
        val firstMatch = results.first()
        assertTrue("Match should be fuzzy or typo tolerant", firstMatch.isFuzzyMatch)
        assertTrue("Snippet should contain the target text", firstMatch.snippetText.contains("圣经") || firstMatch.snippetText.contains("神圣"))
    }

    @Test
    fun testLongParagraphSearch() {
        val content = """
            查拉图斯特拉三十岁的时候，离开他的家乡和家乡的湖，走到高山里去。在那里，他享用他的精神与他的孤独，十年不厌。然而他的心境终于转变了——在一个晴朗的早晨，他迎着朝曦起身，走到太阳面前，对着它这般说道：你伟大的星球啊！如果你没有你所照耀的人们，你的幸福又在哪里呢！
        """.trimIndent()

        // Long continuous sentence query with slight whitespace and punctuation variations
        val longQuery = "离开他的家乡和家乡的湖，走到高山里去。在那里，他享用他的精神与他的孤独，十年不厌。"

        val results = SearchEngine.searchInChapterContent(
            bookId = 1L,
            bookTitle = "查拉图斯特拉如是说",
            bookAuthor = "尼采",
            chapterIndex = 0,
            chapterTitle = "第一章",
            content = content,
            rawQuery = longQuery
        )

        assertTrue("Long paragraph search must find matches", results.isNotEmpty())
        assertEquals(0, results.first().chapterIndex)
    }

    @Test
    fun testMultiLanguageStringTranslations() {
        // Test Chinese
        assertEquals("书架", AppStrings.get("tab_shelf", "zh_CN"))
        assertEquals("搜索", AppStrings.get("tab_search", "zh_CN"))
        assertEquals("应用文字大小", AppStrings.get("app_font_size_title", "zh_CN"))

        // Test Korean
        assertEquals("서가", AppStrings.get("tab_shelf", "ko"))
        assertEquals("검색", AppStrings.get("tab_search", "ko"))
        assertEquals("앱 전체 글꼴 크기", AppStrings.get("app_font_size_title", "ko"))

        // Test English
        assertEquals("Bookshelf", AppStrings.get("tab_shelf", "en"))
        assertEquals("Search", AppStrings.get("tab_search", "en"))
        assertEquals("Global App Font Size", AppStrings.get("app_font_size_title", "en"))
    }
}
