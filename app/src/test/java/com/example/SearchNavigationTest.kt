package com.example

import com.example.data.repository.SearchResultSnippet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchNavigationTest {

    @Test
    fun testMultiMatchResolutionInChapter() {
        // Mock paginated book pages:
        // Chapter 0: pages 0..2
        // Chapter 1: pages 3..5
        // Chapter 2: pages 6..8
        val pagesContent = listOf(
            "第一篇 学而 子曰：学而时习之，不亦说乎？有朋自远方来，不亦乐乎？", // page 0 (Chap 0)
            "为人谋而不忠乎？与朋友交而不信乎？传不习乎？", // page 1 (Chap 0)
            "吾十有五而志于学，三十而立，四十而不惑。", // page 2 (Chap 0)
            "第二篇 为政 子曰：为政以德，譬如北辰，居其所而众星共之。", // page 3 (Chap 1, match 0)
            "道之以政，齐之以刑，民免而无耻；道之以德，齐之以礼，有耻且格。", // page 4 (Chap 1)
            "子曰：诗三百，一言以蔽之，曰：思无邪。", // page 5 (Chap 1, match 1)
            "第三篇 八佾 孔子谓季氏：八佾舞于庭，是可忍也，孰不可忍也？", // page 6 (Chap 2)
            "子曰：人而不仁，如礼何？人而不仁，如乐何？", // page 7 (Chap 2, match 0)
            "林放问礼之本。子曰：大哉问！礼，与其奢也，宁俭。" // page 8 (Chap 2, match 1)
        )

        val chapterStartPageMap = mapOf(
            0 to 0,
            1 to 3,
            2 to 6
        )

        fun findExactPage(
            chapterIndex: Int,
            keyword: String,
            snippetText: String,
            matchIndexInChapter: Int
        ): Int {
            val startPage = chapterStartPageMap[chapterIndex] ?: 0
            val nextChapterStart = chapterStartPageMap[chapterIndex + 1] ?: pagesContent.size
            val chapterPageRange = startPage until nextChapterStart.coerceAtMost(pagesContent.size)

            val cleanKeyword = keyword.trim()
            val cleanSnippet = snippetText.replace("...", "").trim()

            // Level 1: Long anchor match
            if (cleanSnippet.isNotBlank() && cleanKeyword.isNotBlank()) {
                val kwPos = cleanSnippet.indexOf(cleanKeyword, ignoreCase = true)
                if (kwPos >= 0) {
                    val aStart = (kwPos - 8).coerceAtLeast(0)
                    val aEnd = (kwPos + cleanKeyword.length + 10).coerceAtMost(cleanSnippet.length)
                    val longAnchor = cleanSnippet.substring(aStart, aEnd).trim()

                    if (longAnchor.length >= cleanKeyword.length + 3) {
                        for (p in chapterPageRange) {
                            val pageContent = pagesContent.getOrNull(p) ?: continue
                            if (pageContent.contains(longAnchor, ignoreCase = true)) {
                                return p
                            }
                        }
                    }
                }
            }

            // Level 3: Occurrence count matching in this chapter
            if (cleanKeyword.isNotBlank() && matchIndexInChapter >= 0) {
                var accumulatedMatches = 0
                for (p in chapterPageRange) {
                    val pageContent = pagesContent.getOrNull(p) ?: continue
                    var pos = pageContent.indexOf(cleanKeyword, 0, ignoreCase = true)
                    var countOnPage = 0
                    while (pos >= 0) {
                        countOnPage++
                        pos = pageContent.indexOf(cleanKeyword, pos + cleanKeyword.length, ignoreCase = true)
                    }

                    if (accumulatedMatches + countOnPage > matchIndexInChapter) {
                        return p
                    }
                    accumulatedMatches += countOnPage
                }
            }

            return startPage
        }

        // Test 1: Clicking Chapter 1, match 0 ("为政以德") -> should jump to Page 3
        val pageChap1Match0 = findExactPage(
            chapterIndex = 1,
            keyword = "子曰",
            snippetText = "...子曰：为政以德，譬如北辰...",
            matchIndexInChapter = 0
        )
        assertEquals(3, pageChap1Match0)

        // Test 2: Clicking Chapter 1, match 1 ("诗三百") -> should jump to Page 5, NOT page 3 or page 0!
        val pageChap1Match1 = findExactPage(
            chapterIndex = 1,
            keyword = "子曰",
            snippetText = "...子曰：诗三百，一言以蔽之...",
            matchIndexInChapter = 1
        )
        assertEquals(5, pageChap1Match1)

        // Test 3: Clicking Chapter 2, match 1 ("大哉问") -> should jump to Page 8, NOT page 0 or page 6!
        val pageChap2Match1 = findExactPage(
            chapterIndex = 2,
            keyword = "子曰",
            snippetText = "...林放问礼之本。子曰：大哉问！礼...",
            matchIndexInChapter = 1
        )
        assertEquals(8, pageChap2Match1)

        // Test 4: Verify search in Chapter 2 NEVER resolves to Chapter 0 even if keyword is on Page 0
        assertTrue("Must be in Chapter 2 range (6..8)", pageChap2Match1 in 6..8)
    }

    @Test
    fun testLongParagraphAndWhitespaceToleranceSearch() {
        val chapterContent = """
            子曰：“学而时习之，不亦说乎？
            有朋自远方来，不亦乐乎？
            人不知而不愠，不亦君子乎？”
            有子曰：“其为人也孝弟，而好犯上者，鲜矣；不好犯上，而好作乱者，未之有也。君子务本，本立而道生。”
        """.trimIndent()

        // Test long paragraph query with differing newlines, spaces, and quote symbols
        val longQueryWithDifferentFormatting = "学而时习之, 不亦说乎? 有朋自远方来, 不亦乐乎? 人不知而不愠, 不亦君子乎?"
        val results = com.example.util.SearchEngine.searchInChapterContent(
            bookId = 1L,
            bookTitle = "论语",
            bookAuthor = "孔子",
            chapterIndex = 0,
            chapterTitle = "学而篇 第一",
            content = chapterContent,
            rawQuery = longQueryWithDifferentFormatting
        )

        assertTrue("Long paragraph search should find match despite punctuation/line breaks", results.isNotEmpty())
        val hit = results.first()
        assertEquals("论语", hit.bookTitle)
        assertEquals(0, hit.chapterIndex)
        assertTrue("Offset should locate beginning of paragraph", hit.charOffset in 0..15)
        assertTrue("Snippet should contain text", hit.snippetText.contains("学而时习之"))
    }

    @Test
    fun testFuzzyHomophoneAndTypoSearch() {
        val chapterContent = "天覆地载，物数号万，而生人以为贵者，政食之主也。神农作耒耜以教天下，自此五谷咸备。"

        // User typed homophone/typo: "生人以为贵这" instead of "生人以为贵者"
        val fuzzyQuery = "生人以为贵这"
        val results = com.example.util.SearchEngine.searchInChapterContent(
            bookId = 4L,
            bookTitle = "天工开物",
            bookAuthor = "宋应星",
            chapterIndex = 0,
            chapterTitle = "第一卷 乃粒",
            content = chapterContent,
            rawQuery = fuzzyQuery
        )

        assertTrue("Fuzzy search should tolerate single typo/homophone", results.isNotEmpty())
        val hit = results.first()
        assertTrue("Should be marked as fuzzy match", hit.isFuzzyMatch)
        assertTrue("Snippet should contain context", hit.snippetText.contains("生人以为贵者"))
    }

    @Test
    fun testTocTitleFuzzyMatching() {
        val score1 = com.example.util.SearchEngine.matchChapterTitleScore(
            title = "第一卷 乃粒·稼穑之本",
            query = "乃粒 稼穑"
        )
        assertTrue("Subsequence keywords in TOC title should match with high score", score1 >= 0.80f)

        val score2 = com.example.util.SearchEngine.matchChapterTitleScore(
            title = "学而篇 第一",
            query = "学而"
        )
        assertTrue("Prefix substring in TOC title should match", score2 >= 0.90f)
    }
}

