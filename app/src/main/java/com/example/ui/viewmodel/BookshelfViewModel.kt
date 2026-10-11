package com.example.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.BookEntity
import com.example.data.repository.BookRepository
import com.example.ui.model.ReaderSettings
import com.example.util.AppLanguage
import com.example.util.BatchImportHelper
import com.example.util.BookParser
import com.example.util.LocaleHelper
import com.example.util.cleanBookTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.Locale

enum class BookshelfViewMode {
    SPINE,  // 实体书架侧面（书脊）
    GRID,   // 经典网格封面
    LIST    // 详细列表
}

enum class BookshelfSortMode(val displayName: String) {
    MANUAL("手动排列"),
    NAME_ASC("按名称 (A - Z)"),
    NAME_DESC("按名称 (Z - A)"),
    RECENT_READ("按阅读时间"),
    IMPORT_TIME("按导入时间")
}

data class BatchImportProgress(
    val isImporting: Boolean = false,
    val isScanning: Boolean = false,
    val current: Int = 0,
    val total: Int = 0,
    val currentBookName: String = "",
    val resultMessage: String? = null
)

data class BookshelfUiState(
    val books: List<BookEntity> = emptyList(),
    val viewMode: BookshelfViewMode = BookshelfViewMode.GRID,
    val isGridView: Boolean = false,
    val selectedCategory: String = "默认书架",
    val availableCategories: List<String> = listOf("默认书架", "所有书籍", "歌本", "收藏"),
    val selectedBookForAction: BookEntity? = null,
    val isImportDialogOpen: Boolean = false,
    val isPresetSelectorOpen: Boolean = false,
    val isBatchMode: Boolean = false,
    val selectedBookIds: Set<Long> = emptySet(),
    val settings: ReaderSettings = ReaderSettings(),
    val appFontScale: Float = 1.0f,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val sortMode: BookshelfSortMode = BookshelfSortMode.MANUAL,
    val batchImportProgress: BatchImportProgress = BatchImportProgress(),
    val isSortSheetOpen: Boolean = false,
    val isManualReorderOpen: Boolean = false,
    val isLoading: Boolean = false
)

class BookshelfViewModel(private val repository: BookRepository) : ViewModel() {

    private val _viewMode = MutableStateFlow(BookshelfViewMode.GRID)
    private val _selectedCategory = MutableStateFlow("默认书架")
    private val _selectedBookForAction = MutableStateFlow<BookEntity?>(null)
    private val _isImportDialogOpen = MutableStateFlow(false)
    private val _isPresetSelectorOpen = MutableStateFlow(false)
    private val _isBatchMode = MutableStateFlow(false)
    private val _selectedBookIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _settings = MutableStateFlow(ReaderSettings())
    private val _appFontScale = MutableStateFlow(1.0f)
    private val _appLanguage = MutableStateFlow(AppLanguage.SYSTEM)

    // Bookshelf sorting & manual arrangement
    private val _sortMode = MutableStateFlow(BookshelfSortMode.MANUAL)
    private val _manualOrderBookIds = MutableStateFlow<List<Long>>(emptyList())
    private val _isSortSheetOpen = MutableStateFlow(false)
    private val _isManualReorderOpen = MutableStateFlow(false)

    // Batch import state
    private val _batchImportProgress = MutableStateFlow(BatchImportProgress())

    init {
        viewModelScope.launch {
            repository.initializePresetIfNeeded()
            try {
                val prefs = repository.context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                _appFontScale.value = prefs.getFloat("app_font_scale", 1.0f)
                _appLanguage.value = LocaleHelper.getSelectedLanguage(repository.context)
                val savedMode = prefs.getString("bookshelf_view_mode", BookshelfViewMode.GRID.name)
                if (savedMode != null) {
                    _viewMode.value = try {
                        BookshelfViewMode.valueOf(savedMode)
                    } catch (_: Exception) {
                        BookshelfViewMode.GRID
                    }
                }

                // Load saved sort mode
                val savedSort = prefs.getString("bookshelf_sort_mode", BookshelfSortMode.MANUAL.name)
                if (savedSort != null) {
                    _sortMode.value = try {
                        BookshelfSortMode.valueOf(savedSort)
                    } catch (_: Exception) {
                        BookshelfSortMode.MANUAL
                    }
                }

                // Load saved manual order
                val savedOrder = prefs.getString("bookshelf_manual_order", "")
                if (!savedOrder.isNullOrBlank()) {
                    _manualOrderBookIds.value = savedOrder.split(",").mapNotNull { it.trim().toLongOrNull() }
                }
            } catch (_: Exception) {}
        }
    }

    private data class IntermediateState(
        val rawBooks: List<BookEntity>,
        val viewMode: BookshelfViewMode,
        val category: String,
        val actionBook: BookEntity?,
        val importOpen: Boolean,
        val presetOpen: Boolean,
        val sortMode: BookshelfSortMode,
        val manualOrder: List<Long>,
        val batchImport: BatchImportProgress,
        val isSortOpen: Boolean,
        val isReorderOpen: Boolean
    )

    private val intermediateFlow = combine(
        combine(
            repository.allBooks,
            _viewMode,
            _selectedCategory,
            _selectedBookForAction,
            _isImportDialogOpen
        ) { books, viewMode, cat, actionBook, importOpen ->
            Triple(books, Pair(viewMode, cat), Pair(actionBook, importOpen))
        },
        combine(
            _isPresetSelectorOpen,
            _sortMode,
            _manualOrderBookIds,
            _batchImportProgress,
            _isSortSheetOpen
        ) { presetOpen, sortMode, manualOrder, batchProgress, isSortOpen ->
            Triple(presetOpen, Pair(sortMode, manualOrder), Pair(batchProgress, isSortOpen))
        },
        _isManualReorderOpen
    ) { part1, part2, isReorderOpen ->
        IntermediateState(
            rawBooks = part1.first,
            viewMode = part1.second.first,
            category = part1.second.second,
            actionBook = part1.third.first,
            importOpen = part1.third.second,
            presetOpen = part2.first,
            sortMode = part2.second.first,
            manualOrder = part2.second.second,
            batchImport = part2.third.first,
            isSortOpen = part2.third.second,
            isReorderOpen = isReorderOpen
        )
    }

    val uiState: StateFlow<BookshelfUiState> = combine(
        intermediateFlow,
        _settings,
        _appFontScale,
        _appLanguage
    ) { inter, currentSettings, fontScale, language ->
        val rawBooks = inter.rawBooks
        val collator = Collator.getInstance(Locale.CHINA)

        // 1. Filter by category
        val categoryFiltered = when (inter.category) {
            "默认书架" -> rawBooks
            "所有书籍", "全部" -> rawBooks
            "歌本" -> rawBooks.filter { it.title.contains("歌") || it.category == "歌本" || it.category.contains("歌") }
            "收藏", "我的收藏" -> rawBooks.filter { it.isFavorite || it.category == "收藏" || it.category == "我的收藏" }
            else -> rawBooks.filter { it.category == inter.category }
        }

        // 2. Sort by user's selected Sort Mode (Manual / Name / Read Time / Import Time)
        val sortedBooks = when (inter.sortMode) {
            BookshelfSortMode.MANUAL -> {
                if (inter.manualOrder.isEmpty()) {
                    // Default fallback: books with read progress first, then by id DESC
                    categoryFiltered
                } else {
                    val orderMap = inter.manualOrder.withIndex().associate { it.value to it.index }
                    // Known manual books keep their order; any new unranked books appear at top (-1)
                    categoryFiltered.sortedWith { b1, b2 ->
                        val r1 = orderMap[b1.id] ?: -1
                        val r2 = orderMap[b2.id] ?: -1
                        r1.compareTo(r2)
                    }
                }
            }
            BookshelfSortMode.NAME_ASC -> {
                categoryFiltered.sortedWith { b1, b2 ->
                    collator.compare(cleanBookTitle(b1.title), cleanBookTitle(b2.title))
                }
            }
            BookshelfSortMode.NAME_DESC -> {
                categoryFiltered.sortedWith { b1, b2 ->
                    collator.compare(cleanBookTitle(b2.title), cleanBookTitle(b1.title))
                }
            }
            BookshelfSortMode.RECENT_READ -> {
                categoryFiltered.sortedByDescending { it.lastReadTime }
            }
            BookshelfSortMode.IMPORT_TIME -> {
                categoryFiltered.sortedByDescending { it.id }
            }
        }

        BookshelfUiState(
            books = sortedBooks,
            viewMode = inter.viewMode,
            isGridView = inter.viewMode == BookshelfViewMode.GRID,
            selectedCategory = inter.category,
            selectedBookForAction = inter.actionBook,
            isImportDialogOpen = inter.importOpen,
            isPresetSelectorOpen = inter.presetOpen,
            isBatchMode = false,
            selectedBookIds = emptySet(),
            settings = currentSettings,
            appFontScale = fontScale,
            appLanguage = language,
            sortMode = inter.sortMode,
            batchImportProgress = inter.batchImport,
            isSortSheetOpen = inter.isSortOpen,
            isManualReorderOpen = inter.isReorderOpen,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BookshelfUiState(isLoading = true)
    )

    // ================= SORTING & MANUAL ARRANGEMENT =================

    fun setSortMode(mode: BookshelfSortMode) {
        _sortMode.value = mode
        viewModelScope.launch {
            try {
                val prefs = repository.context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().putString("bookshelf_sort_mode", mode.name).apply()
            } catch (_: Exception) {}
        }
    }

    fun setSortSheetOpen(open: Boolean) {
        _isSortSheetOpen.value = open
    }

    fun setManualReorderOpen(open: Boolean) {
        _isManualReorderOpen.value = open
    }

    private fun persistManualOrder(ids: List<Long>) {
        _manualOrderBookIds.value = ids
        _sortMode.value = BookshelfSortMode.MANUAL
        viewModelScope.launch {
            try {
                val prefs = repository.context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("bookshelf_manual_order", ids.joinToString(","))
                    .putString("bookshelf_sort_mode", BookshelfSortMode.MANUAL.name)
                    .apply()
            } catch (_: Exception) {}
        }
    }

    fun moveBookToTop(bookId: Long) {
        val currentList = uiState.value.books.map { it.id }.toMutableList()
        currentList.remove(bookId)
        currentList.add(0, bookId)
        persistManualOrder(currentList)
        _selectedBookForAction.value = null
    }

    fun moveBookToBottom(bookId: Long) {
        val currentList = uiState.value.books.map { it.id }.toMutableList()
        currentList.remove(bookId)
        currentList.add(bookId)
        persistManualOrder(currentList)
        _selectedBookForAction.value = null
    }

    fun moveBookUp(bookId: Long) {
        val currentList = uiState.value.books.map { it.id }.toMutableList()
        val index = currentList.indexOf(bookId)
        if (index > 0) {
            val temp = currentList[index]
            currentList[index] = currentList[index - 1]
            currentList[index - 1] = temp
            persistManualOrder(currentList)
        }
    }

    fun moveBookDown(bookId: Long) {
        val currentList = uiState.value.books.map { it.id }.toMutableList()
        val index = currentList.indexOf(bookId)
        if (index in 0 until currentList.size - 1) {
            val temp = currentList[index]
            currentList[index] = currentList[index + 1]
            currentList[index + 1] = temp
            persistManualOrder(currentList)
        }
    }

    fun reorderManualList(newIds: List<Long>) {
        persistManualOrder(newIds)
    }

    // ================= BATCH IMPORT =================

    fun importMultipleUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _isImportDialogOpen.value = false
            val filePairs = uris.map { uri ->
                val name = BookParser.getFileName(repository.context, uri)
                uri to name
            }
            _batchImportProgress.value = BatchImportProgress(
                isImporting = true,
                isScanning = false,
                current = 0,
                total = filePairs.size,
                currentBookName = "准备批量导入..."
            )
            val newIds = repository.importMultipleUris(filePairs) { current, total, name ->
                _batchImportProgress.value = BatchImportProgress(
                    isImporting = true,
                    isScanning = false,
                    current = current,
                    total = total,
                    currentBookName = name
                )
            }
            // Add new books to the front of manual order so user sees them right away
            if (newIds.isNotEmpty()) {
                val existing = _manualOrderBookIds.value.toMutableList()
                newIds.asReversed().forEach { id ->
                    existing.remove(id)
                    existing.add(0, id)
                }
                persistManualOrder(existing)
            }
            _batchImportProgress.value = BatchImportProgress(
                isImporting = false,
                isScanning = false,
                resultMessage = "成功导入 ${newIds.size} 本书籍"
            )
        }
    }

    fun importFolderTree(treeUri: Uri) {
        viewModelScope.launch {
            _isImportDialogOpen.value = false
            _batchImportProgress.value = BatchImportProgress(
                isImporting = true,
                isScanning = true,
                current = 0,
                total = 0,
                currentBookName = "正在扫描文件夹中的电子书..."
            )
            val files = withContext(Dispatchers.IO) {
                BatchImportHelper.scanFolderForBooks(repository.context, treeUri)
            }
            if (files.isEmpty()) {
                _batchImportProgress.value = BatchImportProgress(
                    isImporting = false,
                    isScanning = false,
                    resultMessage = "未在选定文件夹中发现支持的电子书格式 (.txt, .epub, .docx)"
                )
                return@launch
            }
            _batchImportProgress.value = BatchImportProgress(
                isImporting = true,
                isScanning = false,
                current = 0,
                total = files.size,
                currentBookName = "发现 ${files.size} 本书籍，开始导入..."
            )
            val newIds = repository.importMultipleUris(files) { current, total, name ->
                _batchImportProgress.value = BatchImportProgress(
                    isImporting = true,
                    isScanning = false,
                    current = current,
                    total = total,
                    currentBookName = name
                )
            }
            if (newIds.isNotEmpty()) {
                val existing = _manualOrderBookIds.value.toMutableList()
                newIds.asReversed().forEach { id ->
                    existing.remove(id)
                    existing.add(0, id)
                }
                persistManualOrder(existing)
            }
            _batchImportProgress.value = BatchImportProgress(
                isImporting = false,
                isScanning = false,
                resultMessage = "文件夹扫描导入完成，共成功导入 ${newIds.size} 本书籍"
            )
        }
    }

    fun dismissBatchImportProgress() {
        _batchImportProgress.value = BatchImportProgress()
    }

    fun setAppLanguage(language: AppLanguage) {
        _appLanguage.value = language
        viewModelScope.launch {
            try {
                LocaleHelper.setLanguage(repository.context, language)
            } catch (_: Exception) {}
        }
    }

    fun setAppFontScale(scale: Float) {
        _appFontScale.value = scale
        viewModelScope.launch {
            try {
                val prefs = repository.context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().putFloat("app_font_scale", scale).apply()
            } catch (_: Exception) {}
        }
    }

    fun cycleViewMode() {
        val nextMode = when (_viewMode.value) {
            BookshelfViewMode.SPINE -> BookshelfViewMode.LIST
            BookshelfViewMode.LIST -> BookshelfViewMode.GRID
            BookshelfViewMode.GRID -> BookshelfViewMode.SPINE
        }
        setViewMode(nextMode)
    }

    fun setViewMode(mode: BookshelfViewMode) {
        _viewMode.value = mode
        viewModelScope.launch {
            try {
                val prefs = repository.context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().putString("bookshelf_view_mode", mode.name).apply()
            } catch (_: Exception) {}
        }
    }

    fun toggleViewMode() {
        cycleViewMode()
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun openBookAction(book: BookEntity) {
        _selectedBookForAction.value = book
    }

    fun dismissBookAction() {
        _selectedBookForAction.value = null
    }

    fun toggleFavorite(bookId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(bookId)
        }
    }

    fun updateCategory(bookId: Long, newCategory: String) {
        viewModelScope.launch {
            repository.updateBookCategory(bookId, newCategory)
            _selectedBookForAction.value = null
        }
    }

    fun deleteBook(bookId: Long) {
        viewModelScope.launch {
            repository.deleteBook(bookId)
            _selectedBookForAction.value = null
        }
    }

    fun setImportDialogOpen(open: Boolean) {
        _isImportDialogOpen.value = open
    }

    fun setPresetSelectorOpen(open: Boolean) {
        _isPresetSelectorOpen.value = open
    }

    fun importCustomBook(title: String, author: String, content: String, category: String = "导入文档") {
        viewModelScope.launch {
            val newId = repository.importLocalTextBook(title, author, content, category)
            val existing = _manualOrderBookIds.value.toMutableList()
            existing.remove(newId)
            existing.add(0, newId)
            persistManualOrder(existing)
            _isImportDialogOpen.value = false
        }
    }

    fun importFileUri(uri: Uri, fileName: String) {
        viewModelScope.launch {
            val newId = repository.importFileUri(uri, fileName)
            val existing = _manualOrderBookIds.value.toMutableList()
            existing.remove(newId)
            existing.add(0, newId)
            persistManualOrder(existing)
            _isImportDialogOpen.value = false
        }
    }

    fun toggleWoodShelf() {
        _settings.value = _settings.value.copy(
            woodShelfEnabled = !_settings.value.woodShelfEnabled
        )
    }
}

class BookshelfViewModelFactory(private val repository: BookRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BookshelfViewModel(repository) as T
    }
}
