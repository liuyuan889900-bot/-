package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.ReadingProgressEntity
import com.example.data.repository.BookRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI State for Book Reading Progress
 */
data class ReadingProgressUiState(
    val bookId: Long = -1L,
    val progress: ReadingProgressEntity? = null,
    val isLoading: Boolean = false,
    val isSavedSuccessfully: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel to save and retrieve book reading progress (paragraph-level tracking)
 * backed by Room Database via Repository pattern.
 */
class ReadingProgressViewModel(
    private val repository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadingProgressUiState())
    val uiState: StateFlow<ReadingProgressUiState> = _uiState.asStateFlow()

    private var currentObserveJob: Job? = null

    // Reactive list of all reading progress across books
    val allProgress: StateFlow<List<ReadingProgressEntity>> = repository.allReadingProgress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Loads and reactively observes reading progress for a specific book.
     */
    fun loadProgress(bookId: Long) {
        if (bookId <= 0) return
        currentObserveJob?.cancel()
        _uiState.value = _uiState.value.copy(bookId = bookId, isLoading = true, errorMessage = null)

        currentObserveJob = viewModelScope.launch {
            repository.getReadingProgress(bookId)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage ?: "读取进度失败"
                    )
                }
                .collect { progressEntity ->
                    _uiState.value = _uiState.value.copy(
                        bookId = bookId,
                        progress = progressEntity,
                        isLoading = false,
                        errorMessage = null
                    )
                }
        }
    }

    /**
     * One-shot fetch for reading progress of a specific book.
     */
    suspend fun getProgressOnce(bookId: Long): ReadingProgressEntity? {
        return repository.getReadingProgressOnce(bookId)
    }

    /**
     * Returns a direct Flow of reading progress for custom composition.
     */
    fun observeProgress(bookId: Long): Flow<ReadingProgressEntity?> {
        return repository.getReadingProgress(bookId)
    }

    /**
     * Saves or updates reading progress (paragraph index, chapter index, progress percentage).
     */
    fun saveProgress(
        bookId: Long,
        chapterIndex: Int,
        paragraphIndex: Int,
        progressPercent: Float = 0f,
        charOffsetInParagraph: Int = 0,
        totalParagraphsInChapter: Int = 1
    ) {
        if (bookId <= 0) return
        viewModelScope.launch {
            try {
                repository.saveReadingProgress(
                    bookId = bookId,
                    chapterIndex = chapterIndex,
                    paragraphIndex = paragraphIndex,
                    progressPercent = progressPercent,
                    charOffsetInParagraph = charOffsetInParagraph,
                    totalParagraphsInChapter = totalParagraphsInChapter
                )
                _uiState.value = _uiState.value.copy(
                    isSavedSuccessfully = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSavedSuccessfully = false,
                    errorMessage = e.localizedMessage ?: "保存阅读进度失败"
                )
            }
        }
    }

    /**
     * Convenient helper to quickly record current chapter and paragraph progress.
     */
    fun updateParagraphProgress(
        bookId: Long,
        chapterIndex: Int,
        paragraphIndex: Int,
        totalParagraphsInChapter: Int = 1
    ) {
        val calculatedPercent = if (totalParagraphsInChapter > 0) {
            (paragraphIndex.toFloat() / totalParagraphsInChapter.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        saveProgress(
            bookId = bookId,
            chapterIndex = chapterIndex,
            paragraphIndex = paragraphIndex,
            progressPercent = calculatedPercent,
            charOffsetInParagraph = 0,
            totalParagraphsInChapter = totalParagraphsInChapter
        )
    }

    /**
     * Deletes reading progress for a given book.
     */
    fun deleteProgress(bookId: Long) {
        if (bookId <= 0) return
        viewModelScope.launch {
            try {
                repository.deleteReadingProgress(bookId)
                if (_uiState.value.bookId == bookId) {
                    _uiState.value = _uiState.value.copy(progress = null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.localizedMessage ?: "删除进度失败"
                )
            }
        }
    }
}

/**
 * Factory for creating [ReadingProgressViewModel] instances with [BookRepository].
 */
class ReadingProgressViewModelFactory(
    private val repository: BookRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReadingProgressViewModel::class.java)) {
            return ReadingProgressViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
