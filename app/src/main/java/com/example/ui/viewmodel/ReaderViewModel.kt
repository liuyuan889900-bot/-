package com.example.ui.viewmodel

import android.graphics.Paint
import android.os.Build
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.BookmarkEntity
import com.example.data.entity.BookEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.ReadingNoteEntity
import com.example.data.repository.BookRepository
import com.example.data.repository.SearchResultSnippet
import com.example.ui.model.PageTurnMode
import com.example.ui.model.ReaderSettings
import com.example.ui.model.ReaderThemeType
import com.example.ui.theme.AppThemeManager
import com.example.util.EpubRichTextFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ReaderUiState(
    val book: BookEntity? = null,
    val chapters: List<ChapterEntity> = emptyList(),
    val currentChapter: ChapterEntity? = null,
    val currentChapterIndex: Int = 0,
    val currentPageIndex: Int = 0,
    val totalPagesInChapter: Int = 1,
    val currentPageInChapter: Int = 1,
    val chapterProgress: Float = 0f,
    val pagesContent: List<String> = emptyList(),
    val highlightKeyword: String = "",
    val showToolbars: Boolean = false,
    val showTocDrawer: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val showSearchDialog: Boolean = false,
    val showInBookSearchBar: Boolean = false,
    val showNoteDialog: Boolean = false,
    val selectedTextForNote: String = "",
    val inBookSearchQuery: String = "",
    val inBookSearchResults: List<SearchResultSnippet> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val notes: List<ReadingNoteEntity> = emptyList(),
    val settings: ReaderSettings = ReaderSettings(),
    val overallProgress: Float = 0f,
    val effectiveLineHeightSp: Float = 0f,
    val isUsingWebView: Boolean = false,
    val bookshelfBooks: List<BookEntity> = emptyList()
)

class ReaderViewModel(
    var bookId: Long,
    val repository: BookRepository,
    private val initialChapterIndex: Int = -1,
    private val initialHighlightKeyword: String = "",
    private val initialSnippetText: String = "",
    private val initialMatchIndexInChapter: Int = 0
) : ViewModel() {

    private var currentTargetChapterIndex: Int = initialChapterIndex
    private var isExternalSearchHighlight = initialHighlightKeyword.isNotBlank()

    private val _book = MutableStateFlow<BookEntity?>(null)
    private val _chapters = MutableStateFlow<List<ChapterEntity>>(emptyList())
    private val _currentChapterIndex = MutableStateFlow(0)
    private val _currentPageIndex = MutableStateFlow(0)
    private val _pagesContent = MutableStateFlow<List<String>>(emptyList())
    private val _highlightKeyword = MutableStateFlow(initialHighlightKeyword)
    private val _showToolbars = MutableStateFlow(false)
    private val _showTocDrawer = MutableStateFlow(false)
    private val _showSettingsSheet = MutableStateFlow(false)
    private val _showSearchDialog = MutableStateFlow(false)
    private val _showInBookSearchBar = MutableStateFlow(false)
    private val _showNoteDialog = MutableStateFlow(false)
    private val _selectedTextForNote = MutableStateFlow("")
    private val _inBookSearchQuery = MutableStateFlow("")
    private val _inBookSearchResults = MutableStateFlow<List<SearchResultSnippet>>(emptyList())
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    private val _bookmarks = MutableStateFlow<List<BookmarkEntity>>(emptyList())
    private val _notes = MutableStateFlow<List<ReadingNoteEntity>>(emptyList())
    private val _settings = MutableStateFlow(
        ReaderSettings.defaultForTheme(AppThemeManager.currentTheme.value.colors.isDark)
    )
    private val _effectiveLineHeightSp = MutableStateFlow(0f)
    private val _chapterStartPageMap = MutableStateFlow<Map<Int, Int>>(emptyMap())
    private val chapterStartPageMap: Map<Int, Int> get() = _chapterStartPageMap.value

    private val _epubChapterPageCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    private val _epubCurrentPageInChapter = MutableStateFlow(1)
    private val _bookshelfBooks = MutableStateFlow<List<BookEntity>>(emptyList())
    private var isJumpingToLastPage = false

    private var autoHideToolbarJob: Job? = null

    // Real dynamic viewport dimensions measured by Compose container (in dp)
    private var hasMeasuredViewport = false
    private var measuredViewportWidthDp = 390f
    private var measuredViewportHeightDp = 820f

    val uiState: StateFlow<ReaderUiState> = combine(
        _book,
        _chapters,
        _currentChapterIndex,
        _currentPageIndex,
        _pagesContent,
        _highlightKeyword,
        _showToolbars,
        _showTocDrawer,
        _showSettingsSheet,
        _showSearchDialog,
        _showInBookSearchBar,
        _showNoteDialog,
        _selectedTextForNote,
        _inBookSearchQuery,
        _inBookSearchResults,
        _searchHistory,
        _bookmarks,
        _notes,
        _settings,
        _effectiveLineHeightSp,
        _chapterStartPageMap,
        _epubChapterPageCounts,
        _epubCurrentPageInChapter,
        _bookshelfBooks
    ) { params: Array<Any?> ->
        val book = params[0] as BookEntity?
        @Suppress("UNCHECKED_CAST")
        val chapters = params[1] as List<ChapterEntity>
        val chapIdx = params[2] as Int
        val pageIdx = params[3] as Int
        @Suppress("UNCHECKED_CAST")
        val pages = params[4] as List<String>
        val highlight = params[5] as String
        val toolbars = params[6] as Boolean
        val toc = params[7] as Boolean
        val settingsSheet = params[8] as Boolean
        val searchDiag = params[9] as Boolean
        val inBookSearchBar = params[10] as Boolean
        val noteDiag = params[11] as Boolean
        val selText = params[12] as String
        val searchQ = params[13] as String
        @Suppress("UNCHECKED_CAST")
        val searchRes = params[14] as List<SearchResultSnippet>
        @Suppress("UNCHECKED_CAST")
        val history = params[15] as List<String>
        @Suppress("UNCHECKED_CAST")
        val bms = params[16] as List<BookmarkEntity>
        @Suppress("UNCHECKED_CAST")
        val nts = params[17] as List<ReadingNoteEntity>
        val currentSettings = params[18] as ReaderSettings
        val effectiveLineHeight = params[19] as Float
        @Suppress("UNCHECKED_CAST")
        val startMap = params[20] as Map<Int, Int>
        @Suppress("UNCHECKED_CAST")
        val epubCounts = params[21] as Map<Int, Int>
        val epubCurPage = params[22] as Int
        @Suppress("UNCHECKED_CAST")
        val allBooks = params[23] as List<BookEntity>

        val curChapter = chapters.getOrNull(chapIdx)
        val startPage = startMap[chapIdx] ?: 0
        val nextStart = startMap[chapIdx + 1] ?: pages.size
        val totalPagesInChap = (nextStart - startPage).coerceAtLeast(1)
        val curPageInChap = (pageIdx - startPage + 1).coerceIn(1, totalPagesInChap)

        val chapProg = if (totalPagesInChap <= 1) 1f else (curPageInChap - 1).toFloat() / (totalPagesInChap - 1)
        val overallProg = if (pages.isNotEmpty()) {
            (pageIdx + 1).toFloat() / pages.size
        } else 0f

        ReaderUiState(
            book = book,
            chapters = chapters,
            currentChapter = curChapter,
            currentChapterIndex = chapIdx,
            currentPageIndex = pageIdx,
            totalPagesInChapter = totalPagesInChap,
            currentPageInChapter = curPageInChap,
            chapterProgress = chapProg.coerceIn(0f, 1f),
            pagesContent = pages,
            highlightKeyword = highlight,
            showToolbars = toolbars,
            showTocDrawer = toc,
            showSettingsSheet = settingsSheet,
            showSearchDialog = searchDiag,
            showInBookSearchBar = inBookSearchBar,
            showNoteDialog = noteDiag,
            selectedTextForNote = selText,
            inBookSearchQuery = searchQ,
            inBookSearchResults = searchRes,
            searchHistory = history,
            bookmarks = bms,
            notes = nts,
            settings = currentSettings,
            overallProgress = overallProg.coerceIn(0f, 1f),
            effectiveLineHeightSp = effectiveLineHeight,
            isUsingWebView = false,
            bookshelfBooks = allBooks
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReaderUiState()
    )

    private var pageToChapterIndexMap: List<Int> = emptyList()

    init {
        loadBookData()
        loadBookmarksAndNotes()
        loadBookshelfBooks()
    }

    private fun loadBookshelfBooks() {
        viewModelScope.launch {
            repository.allBooks.collect {
                _bookshelfBooks.value = it
            }
        }
    }

    private fun loadBookData() {
        viewModelScope.launch {
            val book = repository.getBookById(bookId)
            _book.value = book

            val chapterList = repository.getChaptersListForBook(bookId)
            _chapters.value = chapterList

            if (chapterList.isEmpty()) {
                _pagesContent.value = listOf("暂无内容")
                return@launch
            }

            val resolvedChapterIdx = if (currentTargetChapterIndex >= 0) {
                currentTargetChapterIndex.coerceIn(0, (chapterList.size - 1).coerceAtLeast(0))
            } else {
                book?.currentChapterIndex?.coerceIn(0, (chapterList.size - 1).coerceAtLeast(0)) ?: 0
            }

            _currentChapterIndex.value = resolvedChapterIdx

            var initialPageOffset = 0
            if (currentTargetChapterIndex < 0 && book != null && book.currentPage > 0) {
                initialPageOffset = (book.currentPage - 1).coerceAtLeast(0)
            }

            startAsyncPagination(
                targetChapterIndex = resolvedChapterIdx,
                preserveOffset = false,
                onFirstChapterReady = {
                    if (initialHighlightKeyword.isNotBlank()) {
                        val clean = initialHighlightKeyword.trim()
                        val foundIdx = _pagesContent.value.indexOfFirst { it.contains(clean, ignoreCase = true) }
                        if (foundIdx >= 0) {
                            _currentPageIndex.value = foundIdx
                        }
                    } else if (initialPageOffset > 0) {
                        _currentPageIndex.value = initialPageOffset.coerceIn(0, (_pagesContent.value.size - 1).coerceAtLeast(0))
                    }
                }
            )
        }
    }

    /**
     * Resolves the exact page index for a search match within a specified chapter.
     * Uses a robust 4-level matching strategy confined STRICTLY to the target chapter:
     * 1. Long context anchor substring match from snippetText (15-25 chars around the keyword).
     * 2. Medium context anchor match (keyword + 4-6 chars).
     * 3. Occurrence index matching within the chapter (k-th occurrence of keyword in chapter).
     * 4. First page in this chapter containing keyword, or the chapter's start page.
     * Guarantees it will NEVER jump to another chapter or page 0!
     */
    fun findExactPageForSearchMatch(
        chapterIndex: Int,
        keyword: String,
        snippetText: String = "",
        matchIndexInChapter: Int = 0
    ): Int {
        val validChapterIndex = chapterIndex.coerceIn(0, (_chapters.value.size - 1).coerceAtLeast(0))
        val startPage = chapterStartPageMap[validChapterIndex] ?: 0
        val nextChapterStart = chapterStartPageMap[validChapterIndex + 1] ?: _pagesContent.value.size
        val chapterPageRange = startPage until nextChapterStart.coerceAtMost(_pagesContent.value.size)

        if (chapterPageRange.isEmpty()) {
            return startPage
        }

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
                        val pageContent = _pagesContent.value.getOrNull(p) ?: continue
                        if (pageContent.contains(longAnchor, ignoreCase = true)) {
                            return p
                        }
                    }
                }

                // Level 2: Medium anchor match
                val mEnd = (kwPos + cleanKeyword.length + 5).coerceAtMost(cleanSnippet.length)
                val medAnchor = cleanSnippet.substring(kwPos, mEnd).trim()
                if (medAnchor.length > cleanKeyword.length) {
                    for (p in chapterPageRange) {
                        val pageContent = _pagesContent.value.getOrNull(p) ?: continue
                        if (pageContent.contains(medAnchor, ignoreCase = true)) {
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
                val pageContent = _pagesContent.value.getOrNull(p) ?: continue
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

        // Level 4: First page in THIS chapter that contains the keyword
        if (cleanKeyword.isNotBlank()) {
            for (p in chapterPageRange) {
                val pageContent = _pagesContent.value.getOrNull(p) ?: continue
                if (pageContent.contains(cleanKeyword, ignoreCase = true)) {
                    return p
                }
            }
        }

        // Fallback: Chapter start page
        return startPage
    }

    private fun loadBookmarksAndNotes() {
        viewModelScope.launch {
            repository.getBookmarksForBook(bookId).collect {
                _bookmarks.value = it
            }
        }
        viewModelScope.launch {
            repository.allNotes.collect { allNotes ->
                _notes.value = allNotes.filter { it.bookId == bookId }
            }
        }
    }

    /**
     * Task 2: Switch book dynamically from Companion or External Navigation
     */
    fun switchBook(newBookId: Long, targetChapterIdx: Int = 0) {
        bookId = newBookId
        currentTargetChapterIndex = targetChapterIdx
        _currentChapterIndex.value = targetChapterIdx
        _currentPageIndex.value = 0
        loadBookData()
        loadBookmarksAndNotes()
    }

    private var displayDensity: Float = repository.context.resources.displayMetrics.density.coerceAtLeast(1.0f)
    private var displayFontScale: Float = run {
        val sysFontScale = repository.context.resources.configuration.fontScale
        val appScale = try {
            val prefs = repository.context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            prefs.getFloat("app_font_scale", 1.0f)
        } catch (_: Exception) { 1.0f }
        (sysFontScale * appScale).coerceAtLeast(0.5f)
    }
    private var paginationJob: Job? = null

    private fun paginateSingleChapter(
        chapterEntity: ChapterEntity,
        textPaint: TextPaint,
        widthPx: Int,
        maxLinesPerPage: Int
    ): List<String> {
        val cleanContent = chapterEntity.content.trim()
        if (EpubRichTextFormatter.hasHtmlMarkup(cleanContent)) {
            return paginateRichHtmlChapter(chapterEntity, textPaint, widthPx, maxLinesPerPage)
        }
        return paginatePlainChapter(chapterEntity, textPaint, widthPx, maxLinesPerPage)
    }

    private fun paginateRichHtmlChapter(
        chapterEntity: ChapterEntity,
        textPaint: TextPaint,
        widthPx: Int,
        maxLinesPerPage: Int
    ): List<String> {
        val cleanContent = chapterEntity.content.trim()

        // 1. Extract and preserve any images from EPUB HTML
        val imgRegex = Regex("""<img[^>]+src=["']([^"']+)["'][^>]*>""", RegexOption.IGNORE_CASE)
        val imageTagRegex = Regex("""<image[^>]+(?:xlink:href|href)=["']([^"']+)["'][^>]*>""", RegexOption.IGNORE_CASE)

        var contentWithImgMarkers = cleanContent
        contentWithImgMarkers = imgRegex.replace(contentWithImgMarkers) { match ->
            val src = match.groupValues[1]
            "\n\n[IMG:$src]\n\n"
        }
        contentWithImgMarkers = imageTagRegex.replace(contentWithImgMarkers) { match ->
            val src = match.groupValues[1]
            "\n\n[IMG:$src]\n\n"
        }

        // 2. Strip HTML tags and normalize whitespace, cleanly preserving [IMG:...]
        val plainText = EpubRichTextFormatter.stripHtml(contentWithImgMarkers)

        // 3. Split into blocks/paragraphs
        val rawBlocks = plainText.split(Regex("""\n{2,}"""))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val pages = mutableListOf<String>()
        val currentPageParagraphs = mutableListOf<String>()
        var linesUsedOnPage = 0
        val cleanChapTitle = com.example.util.normalizeChapterTitle(chapterEntity.title, chapterEntity.chapterIndex + 1)
        val titleBanner = "【$cleanChapTitle】"

        // Insert chapter title banner at the start if not already present, skipping cover-only pages
        if (cleanChapTitle != "封面" && !plainText.contains(cleanChapTitle) && !plainText.contains(chapterEntity.title)) {
            currentPageParagraphs.add(titleBanner)
            linesUsedOnPage += 2
        }

        for (rawBlock in rawBlocks) {
            if (rawBlock.startsWith("[IMG:") && rawBlock.endsWith("]")) {
                // Image block: flush current accumulated text to a page
                if (currentPageParagraphs.isNotEmpty()) {
                    pages.add(currentPageParagraphs.joinToString("\n\n"))
                    currentPageParagraphs.clear()
                    linesUsedOnPage = 0
                }
                // Image occupies its own dedicated page for full clarity
                pages.add(rawBlock)
                continue
            }

            // Normalize paragraph text: remove soft newlines from raw HTML formatting
            val normalizedText = EpubRichTextFormatter.normalizeParagraphText(rawBlock)
            if (normalizedText.isBlank()) continue

            var remainingPara = if (normalizedText.startsWith("　　") || normalizedText.startsWith("    ")) {
                normalizedText
            } else {
                "　　$normalizedText"
            }

            while (remainingPara.isNotBlank()) {
                val availableLines = maxLinesPerPage - linesUsedOnPage
                if (availableLines <= 0) {
                    pages.add(currentPageParagraphs.joinToString("\n\n"))
                    currentPageParagraphs.clear()
                    linesUsedOnPage = 0
                    continue
                }

                val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    StaticLayout.Builder.obtain(remainingPara, 0, remainingPara.length, textPaint, widthPx)
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setIncludePad(false)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    StaticLayout(remainingPara, textPaint, widthPx, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)
                }

                val totalParaLines = layout.lineCount
                if (totalParaLines <= availableLines) {
                    // Entire remaining paragraph fits comfortably on this page
                    currentPageParagraphs.add(remainingPara)
                    linesUsedOnPage += totalParaLines
                    // Add 1 line spacing between paragraphs if space permits
                    if (linesUsedOnPage < maxLinesPerPage) {
                        linesUsedOnPage += 1
                    }
                    remainingPara = ""
                } else {
                    // Paragraph must be sliced cleanly at the exact visual line boundary
                    val endLineIndex = (availableLines - 1).coerceAtLeast(0)
                    val splitCharIndex = layout.getLineEnd(endLineIndex)
                    val fitText = remainingPara.substring(0, splitCharIndex).trimEnd()
                    val remainder = remainingPara.substring(splitCharIndex).trimStart()

                    if (fitText.isNotBlank()) {
                        currentPageParagraphs.add(fitText)
                    }
                    pages.add(currentPageParagraphs.joinToString("\n\n"))
                    currentPageParagraphs.clear()
                    linesUsedOnPage = 0

                    remainingPara = remainder
                }
            }
        }

        if (currentPageParagraphs.isNotEmpty()) {
            pages.add(currentPageParagraphs.joinToString("\n\n"))
            currentPageParagraphs.clear()
        }

        return if (pages.isEmpty()) listOf("【${chapterEntity.title}】\n\n本章无内容") else pages
    }

    private val paginationCache = java.util.concurrent.ConcurrentHashMap<String, List<String>>()

    private fun paginatePlainChapter(
        chapterEntity: ChapterEntity,
        textPaint: TextPaint,
        widthPx: Int,
        maxLinesPerPage: Int
    ): List<String> {
        val cacheKey = "${chapterEntity.id}_${chapterEntity.content.length}_${textPaint.textSize.toInt()}_${widthPx}_$maxLinesPerPage"
        val cached = paginationCache[cacheKey]
        if (cached != null) return cached

        val cleanContent = chapterEntity.content.trim()
        val rawParagraphs = cleanContent.split(PARAGRAPH_SPLIT_REGEX)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { p ->
                if (p.startsWith("　　") || p.startsWith("    ")) p.trimStart()
                else p
            }

        val pages = mutableListOf<String>()
        val currentPageLines = mutableListOf<String>()

        // Break very large chapters (> 20k chars) into manageable chunks to prevent StaticLayout quadratic slowdown
        val chunks = mutableListOf<String>()
        val chunkSb = StringBuilder()
        chunkSb.append("【").append(chapterEntity.title).append("】\n\n")

        for (p in rawParagraphs) {
            if (chunkSb.length + p.length > 20000 && chunkSb.isNotEmpty()) {
                chunks.add(chunkSb.toString())
                chunkSb.clear()
            }
            chunkSb.append("　　").append(p).append("\n")
        }
        if (chunkSb.isNotEmpty()) {
            chunks.add(chunkSb.toString())
        }

        for (chunk in chunks) {
            val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(chunk, 0, chunk.length, textPaint, widthPx)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setIncludePad(false)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(
                    chunk,
                    textPaint,
                    widthPx,
                    Layout.Alignment.ALIGN_NORMAL,
                    1.0f,
                    0.0f,
                    false
                )
            }

            val totalLines = layout.lineCount
            for (lineIdx in 0 until totalLines) {
                val start = layout.getLineStart(lineIdx)
                val end = layout.getLineEnd(lineIdx)
                val cleanLine = chunk.substring(start, end).trimEnd('\r', '\n', ' ')

                currentPageLines.add(cleanLine)

                if (currentPageLines.size == maxLinesPerPage) {
                    pages.add(currentPageLines.joinToString("\n"))
                    currentPageLines.clear()
                }
            }
        }

        if (currentPageLines.isNotEmpty()) {
            pages.add(currentPageLines.joinToString("\n"))
            currentPageLines.clear()
        }

        val result = if (pages.isEmpty()) listOf("暂无内容") else pages
        paginationCache[cacheKey] = result
        return result
    }

    private fun startAsyncPagination(
        targetChapterIndex: Int = _currentChapterIndex.value,
        preserveOffset: Boolean = false,
        onFirstChapterReady: (() -> Unit)? = null
    ) {
        val chapters = _chapters.value
        if (chapters.isEmpty()) {
            _pagesContent.value = listOf("正在加载书籍内容...")
            pageToChapterIndexMap = listOf(0)
            _chapterStartPageMap.value = mapOf(0 to 0)
            return
        }

        val curChap = _currentChapterIndex.value.coerceIn(0, chapters.size - 1)
        val curPage = _currentPageIndex.value
        val oldStartPageOfChap = chapterStartPageMap[curChap] ?: 0
        val pageOffsetInChap = if (preserveOffset) (curPage - oldStartPageOfChap).coerceAtLeast(0) else 0
        val currentSnippet = if (preserveOffset) {
            _pagesContent.value.getOrNull(curPage)?.take(36)?.trim() ?: ""
        } else ""

        paginationJob?.cancel()
        paginationJob = viewModelScope.launch(Dispatchers.Default) {
            val fontSize = _settings.value.fontSize
            val lineSpacing = _settings.value.lineSpacingMultiplier

            val calculatedLineHeightSp = (fontSize * lineSpacing).coerceIn(fontSize * 1.15f, fontSize * 2.2f)
            val singleLineHeightDp = (calculatedLineHeightSp * displayFontScale).coerceAtLeast(18f)

            withContext(Dispatchers.Main) {
                _effectiveLineHeightSp.value = calculatedLineHeightSp
            }

            val effectiveHeightDp = if (measuredViewportHeightDp > 100f) measuredViewportHeightDp else 640f
            // Safe headroom of 14dp ensures the final line of text is never clipped or covered by footer
            val safeAvailableHeightDp = (effectiveHeightDp - 14f).coerceAtLeast(120f)
            // Whole-integer line truncation strictly guarantees complete bottom lines
            val maxLinesPerPage = (safeAvailableHeightDp / singleLineHeightDp).toInt().coerceIn(3, 45)

            val effectiveWidthDp = if (measuredViewportWidthDp > 100f) measuredViewportWidthDp else 390f
            val widthPx = (effectiveWidthDp * displayDensity).toInt().coerceAtLeast(100)
            val safeWidthPx = (widthPx - (8 * displayDensity).toInt()).coerceAtLeast(80)

            val curFont = _settings.value.fontType
            val isBold = _settings.value.isBold

            val baseTypeface = try {
                when (curFont) {
                    com.example.ui.theme.ReaderFontType.SERIF -> android.graphics.Typeface.SERIF
                    com.example.ui.theme.ReaderFontType.SANS_SERIF -> android.graphics.Typeface.DEFAULT
                    com.example.ui.theme.ReaderFontType.MONOSPACE -> android.graphics.Typeface.MONOSPACE
                    com.example.ui.theme.ReaderFontType.NOTO_SERIF -> {
                        androidx.core.content.res.ResourcesCompat.getFont(repository.context, com.example.R.font.noto_serif_sc) ?: android.graphics.Typeface.SERIF
                    }
                    com.example.ui.theme.ReaderFontType.NOTO_SANS -> {
                        androidx.core.content.res.ResourcesCompat.getFont(repository.context, com.example.R.font.noto_sans_sc) ?: android.graphics.Typeface.DEFAULT
                    }
                    com.example.ui.theme.ReaderFontType.CALLIGRAPHY -> {
                        androidx.core.content.res.ResourcesCompat.getFont(repository.context, com.example.R.font.ma_shan_zheng) ?: android.graphics.Typeface.SERIF
                    }
                }
            } catch (_: Exception) {
                android.graphics.Typeface.SERIF
            }

            val finalTypeface = if (isBold) {
                android.graphics.Typeface.create(baseTypeface, android.graphics.Typeface.BOLD)
            } else {
                baseTypeface
            }

            val effectiveTextSizePx = fontSize * displayDensity * displayFontScale
            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = effectiveTextSizePx
                letterSpacing = 0.5f / fontSize
                typeface = finalTypeface
                isFakeBoldText = isBold
            }

            val validTargetChapter = targetChapterIndex.coerceIn(0, chapters.size - 1)

            // Fast Path: Immediately paginate active chapter so screen renders in < 5ms
            val needsFastPath = _pagesContent.value.isEmpty() || _pagesContent.value.first() == "正在加载书籍内容..."
            if (needsFastPath) {
                val targetChapterPages = paginateSingleChapter(
                    chapters[validTargetChapter],
                    textPaint,
                    safeWidthPx,
                    maxLinesPerPage
                )
                if (isActive) {
                    withContext(Dispatchers.Main) {
                        _pagesContent.value = targetChapterPages
                        pageToChapterIndexMap = List(targetChapterPages.size) { validTargetChapter }
                        _chapterStartPageMap.value = mapOf(validTargetChapter to 0)
                        onFirstChapterReady?.invoke()
                    }
                }
            }

            // Background Path: Full book pagination on background thread
            val allPages = mutableListOf<String>()
            val allPageToChap = mutableListOf<Int>()
            val allChapToStartPage = mutableMapOf<Int, Int>()

            for (chapIdx in chapters.indices) {
                if (!isActive) return@launch
                val chapterEntity = chapters[chapIdx]
                allChapToStartPage[chapIdx] = allPages.size

                val chapPages = paginateSingleChapter(chapterEntity, textPaint, safeWidthPx, maxLinesPerPage)
                for (p in chapPages) {
                    allPages.add(p)
                    allPageToChap.add(chapIdx)
                }
            }

            if (!isActive) return@launch

            if (allPages.isEmpty()) {
                allPages.add("暂无内容")
                allPageToChap.add(0)
            }

            withContext(Dispatchers.Main) {
                _pagesContent.value = allPages
                pageToChapterIndexMap = allPageToChap
                _chapterStartPageMap.value = allChapToStartPage

                if (preserveOffset) {
                    val newStartPageOfChap = allChapToStartPage[curChap] ?: 0
                    val nextChapStartPage = allChapToStartPage[curChap + 1] ?: allPages.size
                    val pagesInThisChap = (nextChapStartPage - newStartPageOfChap).coerceAtLeast(1)

                    var matchedPage = -1
                    if (currentSnippet.isNotBlank()) {
                        val searchStart = newStartPageOfChap
                        val searchEnd = (nextChapStartPage - 1).coerceIn(searchStart, allPages.size - 1)
                        for (pIdx in searchStart..searchEnd) {
                            val pageText = allPages[pIdx]
                            if (pageText.contains(currentSnippet)) {
                                matchedPage = pIdx
                                break
                            }
                        }
                    }

                    val finalPage = if (matchedPage >= 0) {
                        matchedPage
                    } else {
                        val maxPageInChap = (pagesInThisChap - 1).coerceAtLeast(0)
                        val oldTotalInChap = ((chapterStartPageMap[curChap + 1] ?: _pagesContent.value.size) - oldStartPageOfChap).coerceAtLeast(1)
                        val ratio = (pageOffsetInChap.toFloat() / oldTotalInChap.toFloat()).coerceIn(0f, 1f)
                        val targetOffset = (ratio * maxPageInChap).toInt().coerceIn(0, maxPageInChap)
                        (newStartPageOfChap + targetOffset).coerceIn(0, allPages.size - 1)
                    }

                    _currentPageIndex.value = finalPage
                    _currentChapterIndex.value = allPageToChap.getOrNull(finalPage) ?: curChap
                } else {
                    val newStartPageOfChap = allChapToStartPage[validTargetChapter] ?: 0
                    var targetPage = newStartPageOfChap

                    if (initialHighlightKeyword.isNotBlank()) {
                        val clean = initialHighlightKeyword.trim()
                        if (initialChapterIndex >= 0) {
                            targetPage = findExactPageForSearchMatch(
                                chapterIndex = validTargetChapter,
                                keyword = clean,
                                snippetText = initialSnippetText,
                                matchIndexInChapter = initialMatchIndexInChapter
                            )
                        } else {
                            val foundIdx = allPages.indexOfFirst { it.contains(clean, ignoreCase = true) }
                            if (foundIdx >= 0) {
                                targetPage = foundIdx
                            }
                        }
                    } else if (initialChapterIndex < 0 && _book.value != null && (_book.value?.currentPage ?: 0) > 0) {
                        targetPage = ((_book.value?.currentPage ?: 1) - 1).coerceIn(0, (allPages.size - 1).coerceAtLeast(0))
                    }

                    _currentPageIndex.value = targetPage.coerceIn(0, (allPages.size - 1).coerceAtLeast(0))
                    _currentChapterIndex.value = allPageToChap.getOrNull(_currentPageIndex.value) ?: validTargetChapter
                }
                saveProgress()
            }
        }
    }

    fun toggleToolbars() {
        val newState = !_showToolbars.value
        _showToolbars.value = newState
        autoHideToolbarJob?.cancel()
    }

    fun hideToolbars() {
        _showToolbars.value = false
        autoHideToolbarJob?.cancel()
    }

    fun nextPage(): Boolean {
        if (_showToolbars.value) {
            _showToolbars.value = false
        }
        if (isExternalSearchHighlight) {
            _highlightKeyword.value = ""
            isExternalSearchHighlight = false
        }
        val maxPage = _pagesContent.value.size - 1
        if (_currentPageIndex.value < maxPage) {
            _currentPageIndex.value++
            _currentChapterIndex.value = pageToChapterIndexMap.getOrNull(_currentPageIndex.value) ?: _currentChapterIndex.value
            saveProgress()
            return true
        }
        val nextChap = _currentChapterIndex.value + 1
        if (nextChap < _chapters.value.size) {
            jumpToChapter(nextChap, targetPage = 0)
            return true
        }
        return false
    }

    fun prevPage(): Boolean {
        if (_showToolbars.value) {
            _showToolbars.value = false
        }
        if (isExternalSearchHighlight) {
            _highlightKeyword.value = ""
            isExternalSearchHighlight = false
        }
        if (_currentPageIndex.value > 0) {
            _currentPageIndex.value--
            _currentChapterIndex.value = pageToChapterIndexMap.getOrNull(_currentPageIndex.value) ?: _currentChapterIndex.value
            saveProgress()
            return true
        }
        val prevChap = _currentChapterIndex.value - 1
        if (prevChap >= 0) {
            jumpToChapter(prevChap, targetPage = -1)
            return true
        }
        return false
    }

    fun updateEpubPageCount(chapterIndex: Int, totalPages: Int) {
        val count = totalPages.coerceAtLeast(1)
        val currentMap = _epubChapterPageCounts.value.toMutableMap()
        currentMap[chapterIndex] = count
        _epubChapterPageCounts.value = currentMap

        if (chapterIndex == _currentChapterIndex.value) {
            if (isJumpingToLastPage) {
                isJumpingToLastPage = false
                _epubCurrentPageInChapter.value = count
            } else if (_epubCurrentPageInChapter.value > count) {
                _epubCurrentPageInChapter.value = count
            }
            saveProgress()
        }
    }

    fun updateEpubCurrentPage(pageInChapter: Int) {
        val total = _epubChapterPageCounts.value[_currentChapterIndex.value] ?: 1
        _epubCurrentPageInChapter.value = pageInChapter.coerceIn(1, total)
        saveProgress()
    }

    fun jumpToChapter(index: Int, targetPage: Int = 0) {
        if (isExternalSearchHighlight) {
            _highlightKeyword.value = ""
            isExternalSearchHighlight = false
        }
        val validIndex = index.coerceIn(0, (_chapters.value.size - 1).coerceAtLeast(0))
        _currentChapterIndex.value = validIndex
        val startPage = chapterStartPageMap[validIndex] ?: 0

        if (targetPage < 0) {
            isJumpingToLastPage = true
            val knownTotal = _epubChapterPageCounts.value[validIndex] ?: 1
            _epubCurrentPageInChapter.value = knownTotal
        } else {
            isJumpingToLastPage = false
            _epubCurrentPageInChapter.value = (targetPage + 1).coerceAtLeast(1)
        }

        val targetIdx = if (targetPage < 0) {
            val nextStart = chapterStartPageMap[validIndex + 1] ?: _pagesContent.value.size
            (nextStart - 1).coerceAtLeast(startPage)
        } else {
            (startPage + targetPage).coerceIn(0, (_pagesContent.value.size - 1).coerceAtLeast(0))
        }
        _currentPageIndex.value = targetIdx
        _currentChapterIndex.value = pageToChapterIndexMap.getOrNull(_currentPageIndex.value) ?: validIndex
        saveProgress()
        _showTocDrawer.value = false
        _showToolbars.value = false
        _showSearchDialog.value = false
        _showInBookSearchBar.value = false
    }

    fun jumpToSearchResult(snippet: SearchResultSnippet, query: String = "") {
        jumpToChapterWithQuery(
            index = snippet.chapterIndex,
            querySnippet = snippet.snippetText,
            highlightKeyword = query.ifBlank { snippet.snippetText },
            matchIndexInChapter = snippet.matchIndexInChapter
        )
    }

    fun jumpToChapterWithQuery(
        index: Int,
        querySnippet: String = "",
        highlightKeyword: String = "",
        matchIndexInChapter: Int = 0
    ) {
        // Internal book search highlight stays active across page turns
        isExternalSearchHighlight = false
        val validIndex = index.coerceIn(0, (_chapters.value.size - 1).coerceAtLeast(0))
        _currentChapterIndex.value = validIndex

        val effectiveHighlight = when {
            highlightKeyword.isNotBlank() -> highlightKeyword.trim()
            querySnippet.isNotBlank() -> querySnippet.replace("...", "").trim().take(15)
            else -> ""
        }
        _highlightKeyword.value = effectiveHighlight
        if (effectiveHighlight.isNotBlank()) {
            recordSearchHistory(effectiveHighlight)
        }

        val targetPage = findExactPageForSearchMatch(
            chapterIndex = validIndex,
            keyword = effectiveHighlight,
            snippetText = querySnippet,
            matchIndexInChapter = matchIndexInChapter
        )

        _currentPageIndex.value = targetPage.coerceIn(0, (_pagesContent.value.size - 1).coerceAtLeast(0))
        _currentChapterIndex.value = pageToChapterIndexMap.getOrNull(_currentPageIndex.value) ?: validIndex
        saveProgress()
        _showTocDrawer.value = false
        _showToolbars.value = false
        _showSearchDialog.value = false
        _showInBookSearchBar.value = false
    }

    fun recordSearchHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) {
            val updated = _searchHistory.value.filter { it != trimmed }.toMutableList()
            updated.add(0, trimmed)
            _searchHistory.value = updated.take(2)
        }
    }

    fun setHighlightKeyword(keyword: String, isExternal: Boolean = false) {
        isExternalSearchHighlight = isExternal
        _highlightKeyword.value = keyword
    }

    fun clearHighlightKeyword() {
        _highlightKeyword.value = ""
        isExternalSearchHighlight = false
    }

    fun nextChapter() {
        if (isExternalSearchHighlight) {
            _highlightKeyword.value = ""
            isExternalSearchHighlight = false
        }
        if (_currentChapterIndex.value < _chapters.value.size - 1) {
            val nextChapIdx = _currentChapterIndex.value + 1
            val targetPage = chapterStartPageMap[nextChapIdx] ?: return
            _currentPageIndex.value = targetPage.coerceIn(0, (_pagesContent.value.size - 1).coerceAtLeast(0))
            _currentChapterIndex.value = nextChapIdx
            saveProgress()
        }
    }

    fun prevChapter(toLastPage: Boolean = false) {
        if (isExternalSearchHighlight) {
            _highlightKeyword.value = ""
            isExternalSearchHighlight = false
        }
        if (_currentChapterIndex.value > 0) {
            val prevChapIdx = _currentChapterIndex.value - 1
            val targetPage = chapterStartPageMap[prevChapIdx] ?: 0
            _currentPageIndex.value = targetPage.coerceIn(0, (_pagesContent.value.size - 1).coerceAtLeast(0))
            _currentChapterIndex.value = prevChapIdx
            saveProgress()
        }
    }

    fun seekToProgress(progressFraction: Float) {
        val total = _pagesContent.value.size
        if (total > 0) {
            val targetPage = (progressFraction * (total - 1)).toInt().coerceIn(0, total - 1)
            _currentPageIndex.value = targetPage
            _currentChapterIndex.value = pageToChapterIndexMap.getOrNull(targetPage) ?: _currentChapterIndex.value
            saveProgress()
        }
    }

    fun seekChapterProgress(progressFraction: Float) {
        val chapIdx = _currentChapterIndex.value
        val startMap = _chapterStartPageMap.value
        val startPage = startMap[chapIdx] ?: 0
        val nextStart = startMap[chapIdx + 1] ?: _pagesContent.value.size
        val totalPagesInChap = (nextStart - startPage).coerceAtLeast(1)
        val offset = Math.round(progressFraction.coerceIn(0f, 1f) * (totalPagesInChap - 1)).toInt()
        val targetPage = (startPage + offset).coerceIn(startPage, (nextStart - 1).coerceAtLeast(startPage))
        val maxPage = (_pagesContent.value.size - 1).coerceAtLeast(0)
        _currentPageIndex.value = targetPage.coerceIn(0, maxPage)
        _currentChapterIndex.value = pageToChapterIndexMap.getOrNull(_currentPageIndex.value) ?: chapIdx
        saveProgress()
    }

    fun setTocDrawerOpen(open: Boolean) {
        _showTocDrawer.value = open
        if (open) autoHideToolbarJob?.cancel()
    }

    fun setSettingsSheetOpen(open: Boolean) {
        _showSettingsSheet.value = open
        if (open) autoHideToolbarJob?.cancel()
    }

    fun setSearchDialogOpen(open: Boolean) {
        _showSearchDialog.value = open
        if (open) autoHideToolbarJob?.cancel()
    }

    fun setInBookSearchBarOpen(open: Boolean) {
        _showInBookSearchBar.value = open
        if (open) {
            autoHideToolbarJob?.cancel()
        }
    }

    fun clearInBookSearch() {
        _inBookSearchQuery.value = ""
        _inBookSearchResults.value = emptyList()
    }

    fun setNoteDialogOpen(open: Boolean, initialText: String = "") {
        _showNoteDialog.value = open
        _selectedTextForNote.value = initialText
    }

    // In-Book Search
    fun searchInBook(query: String) {
        _inBookSearchQuery.value = query
        if (query.isBlank()) {
            _inBookSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            val results = repository.searchInBook(bookId, query)
            _inBookSearchResults.value = results
        }
    }

    // Settings modifications
    fun updateTheme(theme: ReaderThemeType) {
        _settings.value = _settings.value.copy(theme = theme)
    }

    fun updateFontSize(newSize: Int) {
        val clamped = newSize.coerceIn(14, 30)
        _settings.value = _settings.value.copy(fontSize = clamped)
        paginationCache.clear()
        startAsyncPagination(targetChapterIndex = _currentChapterIndex.value, preserveOffset = true)
    }

    fun updateLineSpacing(spacing: Float) {
        _settings.value = _settings.value.copy(lineSpacingMultiplier = spacing)
        paginationCache.clear()
        startAsyncPagination(targetChapterIndex = _currentChapterIndex.value, preserveOffset = true)
    }

    fun updateHorizontalPadding(padding: Int) {
        _settings.value = _settings.value.copy(horizontalPadding = padding)
        paginationCache.clear()
        startAsyncPagination(targetChapterIndex = _currentChapterIndex.value, preserveOffset = true)
    }

    fun updatePageTurnMode(mode: PageTurnMode) {
        _settings.value = _settings.value.copy(pageTurnMode = mode)
    }

    fun updateEpubOriginalFormat(enabled: Boolean) {
        _settings.value = _settings.value.copy(useEpubOriginalFormat = enabled)
    }

    fun updateFontType(fontType: com.example.ui.theme.ReaderFontType) {
        _settings.value = _settings.value.copy(fontType = fontType)
        paginationCache.clear()
        startAsyncPagination(targetChapterIndex = _currentChapterIndex.value, preserveOffset = true)
    }

    fun toggleBold() {
        val newBold = !_settings.value.isBold
        _settings.value = _settings.value.copy(isBold = newBold)
        paginationCache.clear()
        startAsyncPagination(targetChapterIndex = _currentChapterIndex.value, preserveOffset = true)
    }

    fun updateEncoding(encodingCode: String) {
        _settings.value = _settings.value.copy(encoding = encodingCode)
        paginationCache.clear()
        val book = _book.value
        if (book != null && book.format.equals("TXT", ignoreCase = true) && book.filePath.isNotBlank()) {
            val file = java.io.File(book.filePath)
            if (file.exists() && file.length() > 0) {
                viewModelScope.launch(Dispatchers.IO) {
                    val rawBytes = file.readBytes()
                    val decoded = com.example.util.CharsetDetector.detectAndDecode(
                        rawBytes,
                        forcedCharset = if (encodingCode == "AUTO") null else encodingCode
                    )
                    // Re-split into chapters
                    val lines = decoded.lines()
                    val newChapters = mutableListOf<ChapterEntity>()
                    val chapterRegex = Regex("""^(第[0-9零一二三四五六七八九十百千两]+[章节卷回篇集部].*|Chapter\s+[0-9]+.*|ACT\s+[0-9IVXLCDM]+.*|序[言章]|前言|后记|引子|楔子|尾声|附录).*""", RegexOption.IGNORE_CASE)
                    var currentTitle = "第一章 ${book.title}"
                    val currentBuffer = StringBuilder()

                    for (line in lines) {
                        val trimmed = line.trim()
                        if (trimmed.length in 2..40 && chapterRegex.matches(trimmed)) {
                            if (currentBuffer.isNotBlank()) {
                                val body = currentBuffer.toString().trim()
                                newChapters.add(
                                    ChapterEntity(
                                        bookId = bookId,
                                        chapterIndex = newChapters.size,
                                        title = com.example.util.normalizeChapterTitle(currentTitle, newChapters.size + 1),
                                        content = body,
                                        wordCount = body.length
                                    )
                                )
                                currentBuffer.clear()
                            }
                            currentTitle = trimmed
                        } else {
                            currentBuffer.append(line).append("\n")
                        }
                    }
                    if (currentBuffer.isNotBlank() || newChapters.isEmpty()) {
                        val body = currentBuffer.toString().trim().ifBlank { "文档内容为空" }
                        newChapters.add(
                            ChapterEntity(
                                bookId = bookId,
                                chapterIndex = newChapters.size,
                                title = com.example.util.normalizeChapterTitle(currentTitle, newChapters.size + 1),
                                content = body,
                                wordCount = body.length
                            )
                        )
                    }

                    // Save re-decoded chapters to repository
                    repository.replaceChaptersForBook(bookId, newChapters)
                    withContext(Dispatchers.Main) {
                        _chapters.value = newChapters
                        startAsyncPagination(targetChapterIndex = _currentChapterIndex.value, preserveOffset = true)
                    }
                }
                return
            }
        }
        startAsyncPagination(targetChapterIndex = _currentChapterIndex.value, preserveOffset = true)
    }

    fun updateShowPageIndicatorAlways(enabled: Boolean) {
        _settings.value = _settings.value.copy(showPageIndicatorAlways = enabled)
    }

    fun updateTotalPages(newTotal: Int) {
        if (newTotal > 0 && _pagesContent.value.size != newTotal) {
            val oldTotal = _pagesContent.value.size.coerceAtLeast(1)
            val ratio = _currentPageIndex.value.toFloat() / oldTotal
            val newPage = (ratio * (newTotal - 1)).toInt().coerceIn(0, newTotal - 1)
            _currentPageIndex.value = newPage
            saveProgress()
        }
    }

    /**
     * Called when the actual reading container viewport size is measured by Compose.
     * Repaginates content seamlessly to fill 100% of the screen height without leaving bottom blanks.
     */
    fun updateViewportDimensions(
        widthDp: Float,
        heightDp: Float,
        density: Float = 2.5f,
        fontScale: Float = 1.0f
    ) {
        if (widthDp <= 50f || heightDp <= 100f) return
        var scaleChanged = false
        if (density > 0.5f && kotlin.math.abs(density - displayDensity) > 0.01f) {
            displayDensity = density
            scaleChanged = true
        }
        if (fontScale > 0.4f && kotlin.math.abs(fontScale - displayFontScale) > 0.01f) {
            displayFontScale = fontScale
            scaleChanged = true
        }
        val heightDiff = kotlin.math.abs(heightDp - measuredViewportHeightDp)
        val widthDiff = kotlin.math.abs(widthDp - measuredViewportWidthDp)
        if (!hasMeasuredViewport || heightDiff > 2f || widthDiff > 2f || scaleChanged) {
            hasMeasuredViewport = true
            measuredViewportWidthDp = widthDp
            measuredViewportHeightDp = heightDp

            paginationCache.clear()
            startAsyncPagination(targetChapterIndex = _currentChapterIndex.value, preserveOffset = true)
        }
    }

    fun toggleNightMode() {
        val newTheme = if (_settings.value.theme.isDark) {
            ReaderThemeType.PAPER_CREAM
        } else {
            ReaderThemeType.NIGHT
        }
        _settings.value = _settings.value.copy(theme = newTheme)
    }

    // Bookmarks & Notes
    fun addCurrentBookmark() {
        val curChapter = _chapters.value.getOrNull(_currentChapterIndex.value) ?: return
        val currentContent = _pagesContent.value.getOrNull(_currentPageIndex.value) ?: ""
        val excerpt = if (currentContent.length > 50) currentContent.substring(0, 50) + "..." else currentContent

        viewModelScope.launch {
            repository.addBookmark(
                bookId = bookId,
                chapterIndex = _currentChapterIndex.value,
                chapterTitle = curChapter.title,
                progress = uiState.value.overallProgress,
                excerpt = excerpt
            )
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
            repository.deleteBookmark(id)
        }
    }

    fun saveNote(noteText: String, colorHex: String = "#FFFF5A00") {
        val curChapter = _chapters.value.getOrNull(_currentChapterIndex.value) ?: return
        val quote = _selectedTextForNote.value.ifBlank {
            _pagesContent.value.getOrNull(_currentPageIndex.value)?.take(80) ?: ""
        }

        viewModelScope.launch {
            repository.addNote(
                bookId = bookId,
                bookTitle = _book.value?.title ?: "未知书籍",
                chapterTitle = curChapter.title,
                quoteText = quote,
                noteContent = noteText,
                colorHex = colorHex
            )
            _showNoteDialog.value = false
            _selectedTextForNote.value = ""
        }
    }

    /**
     * 用户在读书时给文字画线（Underline/Highlight）
     * 立即保存画线并在正文中实时呈现下划线标记
     */
    fun addUnderline(quoteText: String, colorHex: String = "#FFFFD54F") {
        val cleanQuote = quoteText.trim()
        if (cleanQuote.isBlank()) return
        val curChapter = _chapters.value.getOrNull(_currentChapterIndex.value) ?: return

        viewModelScope.launch {
            repository.addNote(
                bookId = bookId,
                bookTitle = _book.value?.title ?: "未知书籍",
                chapterTitle = curChapter.title,
                quoteText = cleanQuote,
                noteContent = "画线标记",
                colorHex = colorHex
            )
        }
    }

    /**
     * 直接把选择的文字存到笔记里（人性化极简一步即存）
     */
    fun quickSaveNote(quoteText: String, noteText: String = "", colorHex: String = "#FFFF5A00") {
        val cleanQuote = quoteText.trim()
        if (cleanQuote.isBlank()) return
        val curChapter = _chapters.value.getOrNull(_currentChapterIndex.value) ?: return

        viewModelScope.launch {
            repository.addNote(
                bookId = bookId,
                bookTitle = _book.value?.title ?: "未知书籍",
                chapterTitle = curChapter.title,
                quoteText = cleanQuote,
                noteContent = noteText.trim(),
                colorHex = colorHex
            )
            _selectedTextForNote.value = ""
            _showNoteDialog.value = false
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    fun updateCurrentChapterFromScroll(chapterIndex: Int) {
        val validIndex = chapterIndex.coerceIn(0, (_chapters.value.size - 1).coerceAtLeast(0))
        if (_currentChapterIndex.value != validIndex) {
            _currentChapterIndex.value = validIndex
            val startPage = chapterStartPageMap[validIndex] ?: 0
            _currentPageIndex.value = startPage.coerceIn(0, (_pagesContent.value.size - 1).coerceAtLeast(0))
            saveProgress()
        }
    }

    private fun saveProgress() {
        viewModelScope.launch {
            val isEpub = uiState.value.isUsingWebView
            val savedCurPage = if (isEpub) _epubCurrentPageInChapter.value else _currentPageIndex.value + 1
            val savedTotalPages = if (isEpub) {
                (_chapters.value.size * 4).coerceAtLeast(1)
            } else {
                _pagesContent.value.size.coerceAtLeast(1)
            }
            repository.updateBookProgress(
                bookId = bookId,
                chapterIndex = _currentChapterIndex.value,
                progress = uiState.value.overallProgress,
                currentPage = savedCurPage,
                totalPages = savedTotalPages
            )

            // Save fine-grained paragraph progress in Room ReadingProgressEntity
            val currentChapter = _chapters.value.getOrNull(_currentChapterIndex.value)
            val paragraphs = currentChapter?.content?.split(PARAGRAPH_SPLIT_REGEX)?.filter { it.isNotBlank() } ?: emptyList()
            val totalParas = paragraphs.size.coerceAtLeast(1)
            val estimatedParaIndex = ((uiState.value.chapterProgress) * (totalParas - 1)).toInt().coerceIn(0, totalParas - 1)
            repository.saveReadingProgress(
                bookId = bookId,
                chapterIndex = _currentChapterIndex.value,
                paragraphIndex = estimatedParaIndex,
                progressPercent = uiState.value.overallProgress,
                charOffsetInParagraph = 0,
                totalParagraphsInChapter = totalParas
            )
        }
    }

    companion object {
        private val PARAGRAPH_SPLIT_REGEX = Regex("\r?\n+")
    }
}

class ReaderViewModelFactory(
    private val bookId: Long,
    private val repository: BookRepository,
    private val initialChapterIndex: Int = -1,
    private val initialHighlightKeyword: String = "",
    private val initialSnippetText: String = "",
    private val initialMatchIndexInChapter: Int = 0
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReaderViewModel(
            bookId,
            repository,
            initialChapterIndex,
            initialHighlightKeyword,
            initialSnippetText,
            initialMatchIndexInChapter
        ) as T
    }
}
