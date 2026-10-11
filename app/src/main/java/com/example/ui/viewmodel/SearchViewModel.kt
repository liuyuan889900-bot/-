package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.dao.ChapterWithBookInfo
import com.example.data.entity.BookEntity
import com.example.data.entity.SearchHistoryEntity
import com.example.data.repository.BookRepository
import com.example.data.repository.SearchResultSnippet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SearchTab(val title: String, val description: String) {
    TOC("目录搜索", "搜索全书章节目录及标题"),
    TITLE("书名搜索", "快速检索书名、作者与分类"),
    CONTENT("正文搜索", "实时检索书籍正文与上下文")
}

data class SearchUiState(
    val query: String = "",
    val activeTab: SearchTab = SearchTab.TOC,
    val titleResults: List<BookEntity> = emptyList(),
    val tocResults: List<ChapterWithBookInfo> = emptyList(),
    val contentResults: List<SearchResultSnippet> = emptyList(),
    val isSearching: Boolean = false,
    val searchHistory: List<SearchHistoryEntity> = emptyList()
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(private val repository: BookRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _activeTab = MutableStateFlow(SearchTab.TOC)
    val activeTab: StateFlow<SearchTab> = _activeTab.asStateFlow()

    private val _titleResults = MutableStateFlow<List<BookEntity>>(emptyList())
    private val _tocResults = MutableStateFlow<List<ChapterWithBookInfo>>(emptyList())
    private val _contentResults = MutableStateFlow<List<SearchResultSnippet>>(emptyList())
    private val _isSearching = MutableStateFlow(false)

    val searchHistory: StateFlow<List<SearchHistoryEntity>> = repository.searchHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<SearchUiState> = combine(
        _query,
        _activeTab,
        _titleResults,
        _tocResults,
        _contentResults,
        _isSearching,
        searchHistory
    ) { args: Array<Any?> ->
        val q = args[0] as String
        val tab = args[1] as SearchTab
        @Suppress("UNCHECKED_CAST")
        val titleRes = args[2] as List<BookEntity>
        @Suppress("UNCHECKED_CAST")
        val tocRes = args[3] as List<ChapterWithBookInfo>
        @Suppress("UNCHECKED_CAST")
        val contentRes = args[4] as List<SearchResultSnippet>
        val isSearching = args[5] as Boolean
        @Suppress("UNCHECKED_CAST")
        val history = args[6] as List<SearchHistoryEntity>

        SearchUiState(
            query = q,
            activeTab = tab,
            titleResults = titleRes,
            tocResults = tocRes,
            contentResults = contentRes,
            isSearching = isSearching,
            searchHistory = history
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    init {
        // High performance real-time debounce search pipeline
        viewModelScope.launch {
            _query
                .debounce(120) // Instant 120ms feel without lag
                .distinctUntilChanged()
                .collect { searchStr ->
                    performSearch(searchStr, _activeTab.value)
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        // For Title search, trigger instantaneous memory filter right away (<5ms)
        if (_activeTab.value == SearchTab.TITLE && newQuery.isNotBlank()) {
            viewModelScope.launch(Dispatchers.Default) {
                val instantMatches = repository.searchBooksByTitleInstant(newQuery)
                _titleResults.value = instantMatches
            }
        }
    }

    fun onTabSelected(tab: SearchTab) {
        _activeTab.value = tab
        if (_query.value.isNotBlank()) {
            performSearch(_query.value, tab)
        }
    }

    fun onSearchSubmitted(searchStr: String) {
        if (searchStr.isNotBlank()) {
            viewModelScope.launch {
                repository.saveSearchHistory(searchStr, _activeTab.value.name)
            }
            performSearch(searchStr, _activeTab.value)
        }
    }

    fun selectHistoryItem(historyItem: String) {
        _query.value = historyItem
        performSearch(historyItem, _activeTab.value)
    }

    fun deleteHistoryItem(item: String) {
        viewModelScope.launch {
            repository.deleteSearchHistoryItem(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }

    fun clearQuery() {
        _query.value = ""
        _titleResults.value = emptyList()
        _tocResults.value = emptyList()
        _contentResults.value = emptyList()
        _isSearching.value = false
    }

    private fun performSearch(searchStr: String, tab: SearchTab) {
        if (searchStr.isBlank()) {
            _titleResults.value = emptyList()
            _tocResults.value = emptyList()
            _contentResults.value = emptyList()
            _isSearching.value = false
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            when (tab) {
                SearchTab.TITLE -> {
                    val results = repository.searchBooksByTitleInstant(searchStr)
                    _titleResults.value = results
                }
                SearchTab.TOC -> {
                    val results = repository.searchChaptersByTitle(searchStr)
                    _tocResults.value = results
                }
                SearchTab.CONTENT -> {
                    val results = repository.searchContentWithSnippets(searchStr)
                    _contentResults.value = results
                }
            }
            _isSearching.value = false
        }
    }
}

class SearchViewModelFactory(private val repository: BookRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchViewModel(repository) as T
    }
}
