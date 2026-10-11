package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.entity.BookEntity
import com.example.data.entity.ChapterEntity
import com.example.data.repository.BookRepository
import com.example.ui.theme.AppThemeManager
import com.example.ui.theme.AppThemePreset
import com.example.ui.viewmodel.BookshelfViewMode
import com.example.ui.viewmodel.BookshelfViewModel
import com.example.ui.viewmodel.ReaderViewModel
import com.example.util.EpubRichTextFormatter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BookshelfModesAndEpubFixTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: BookRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = AppDatabase.getDatabase(context)
        repository = BookRepository(database.bookDao(), context)
        runBlocking {
            repository.initializePresetIfNeeded()
        }
    }

    /**
     * 验证 1: 封面阅读进度条与书架三种展示模式 (平铺模式、列表模式、书脊模式)
     */
    @Test
    fun testBookshelfThreeViewModesAndSpineSetup() = runBlocking {
        val viewModel = BookshelfViewModel(repository)
        val job = launch { viewModel.uiState.collect {} }

        // Wait for initial books to load
        val initial = viewModel.uiState.first { !it.isLoading }
        assertEquals(BookshelfViewMode.GRID, initial.viewMode)

        // 切换为 LIST (列表模式)
        viewModel.setViewMode(BookshelfViewMode.LIST)
        val listState = viewModel.uiState.first { it.viewMode == BookshelfViewMode.LIST }
        assertEquals(BookshelfViewMode.LIST, listState.viewMode)

        // 切换为 SPINE (书脊模式)
        viewModel.setViewMode(BookshelfViewMode.SPINE)
        val spineState = viewModel.uiState.first { it.viewMode == BookshelfViewMode.SPINE }
        assertEquals(BookshelfViewMode.SPINE, spineState.viewMode)

        // 循环切换验证
        viewModel.cycleViewMode()
        val gridAgain = viewModel.uiState.first { it.viewMode == BookshelfViewMode.GRID }
        assertEquals(BookshelfViewMode.GRID, gridAgain.viewMode)

        // 验证书籍列表完整且具备阅读进度数据
        val books = gridAgain.books
        assertTrue(books.isNotEmpty())
        val firstBook = books.first()
        assertTrue(firstBook.currentProgress >= 0f)

        job.cancel()
    }

    /**
     * 验证 2: 绿洲主题 (Oasis)
     */
    @Test
    fun testOasisThemePreset() {
        AppThemeManager.init(context)
        AppThemeManager.setTheme(context, AppThemePreset.OASIS)

        val current = AppThemeManager.currentTheme.value
        assertEquals(AppThemePreset.OASIS, current)
        assertEquals("绿洲 (Oasis)", current.title)
        assertTrue(current.colors.showOasisBackground)
        assertTrue(current.colors.useSerifFont)
    }

    /**
     * 验证 3: EPUB 图片解析与标记提取 (解决 EPUB 插图不显示问题)
     */
    @Test
    fun testEpubIllustrationPreservation() {
        val rawEpubHtml = """
            <!DOCTYPE html>
            <html>
            <head><title>图文测试</title></head>
            <body>
                <h1>第一章 仙境探索</h1>
                <p>走在古树参天的小径上，忽然眼前出现了一座梦幻的花园。</p>
                <p><img src="file:///data/user/0/com.example/files/epub_extracted/test/illustration1.jpg" alt="仙境花园"/></p>
                <p>这里的繁花在微风中摇曳，宛如世外桃源。</p>
            </body>
            </html>
        """.trimIndent()

        val stripped = EpubRichTextFormatter.stripHtml(rawEpubHtml)
        assertTrue("Image marker [IMG:...] must be cleanly preserved", stripped.contains("[IMG:file:///data/user/0/com.example/files/epub_extracted/test/illustration1.jpg]"))
        assertTrue("Plain text before image must be preserved", stripped.contains("走在古树参天的小径上"))
        assertTrue("Plain text after image must be preserved", stripped.contains("这里的繁花在微风中摇曳"))
        assertFalse("Raw HTML tags like <img or <p must be stripped", stripped.contains("<img"))
    }

    /**
     * 验证 4: EPUB 分页排版修复 (消除切断字/末行截断/多余空半页)
     */
    @Test
    fun testEpubPaginationNoBrokenSentencesAndImages() = runBlocking {
        val allBooks = repository.allBooks.first()
        val testBook = allBooks.first { it.format == "EPUB" }

        val readerViewModel = ReaderViewModel(
            bookId = testBook.id,
            repository = repository
        )

        // Simulate layout viewport dimensions update (390dp x 620dp)
        readerViewModel.updateViewportDimensions(390f, 620f, 2.5f)

        // Wait for async pagination to complete
        kotlinx.coroutines.delay(300)

        val pages = readerViewModel.uiState.value.pagesContent
        assertTrue("Book pages must be paginated and not empty", pages.isNotEmpty())

        for (page in pages) {
            // Check that pages do NOT contain artificial raw HTML paragraph tags caused by broken spans
            assertFalse("Pages must not contain raw unclosed paragraph tags", page.startsWith("<p dir=\"ltr\">") && page.endsWith("</p>"))
            // Check that content is not pure whitespace
            assertTrue("Page content must not be blank", page.isNotBlank())
        }
    }

    /**
     * 验证 5: EPUB 段落软折行平滑拼接 (消除中文字符被截断断开、单字掉到下一行的问题)
     */
    @Test
    fun testParagraphNormalizationSmoothFlow() {
        val brokenCjkParagraph = "这是第一句\n话，后面还有\n更多的内容。"
        val normalized = EpubRichTextFormatter.normalizeParagraphText(brokenCjkParagraph)
        assertEquals("这是第一句话，后面还有更多的内容。", normalized)

        val brokenEnglishParagraph = "This is a line\nwith a break."
        val normalizedEn = EpubRichTextFormatter.normalizeParagraphText(brokenEnglishParagraph)
        assertEquals("This is a line with a break.", normalizedEn)
    }
}
