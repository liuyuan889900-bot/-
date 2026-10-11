package com.example.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.dao.ChapterWithBookInfo
import com.example.data.entity.BookEntity
import com.example.data.repository.SearchResultSnippet
import com.example.ui.components.BookCover
import com.example.ui.theme.StitchTokens
import com.example.ui.viewmodel.SearchTab
import com.example.ui.viewmodel.SearchViewModel
import com.example.util.cleanBookTitle

@Composable
fun RealtimeSearchScreen(
    viewModel: SearchViewModel,
    onBookClick: (Long) -> Unit,
    onChapterClick: (bookId: Long, chapterIndex: Int, searchKeyword: String) -> Unit,
    onSnippetClick: (snippet: SearchResultSnippet, searchKeyword: String) -> Unit = { snippet, kw ->
        onChapterClick(snippet.bookId, snippet.chapterIndex, kw)
    },
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        if (uiState.query.isEmpty()) {
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StitchTokens.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ================= 1. STITCH MINIMALIST TOP BAR =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("search_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = StitchTokens.Primary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Unified Pill Input Field
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(percent = 50),
                    color = StitchTokens.SurfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 44.dp)
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = uiState.query,
                            onValueChange = { viewModel.onQueryChange(it) },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                                .testTag("search_input_field"),
                            singleLine = true,
                            textStyle = TextStyle(
                                color = StitchTokens.Primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(StitchTokens.Primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                focusManager.clearFocus()
                                viewModel.onSearchSubmitted(uiState.query)
                            }),
                            decorationBox = { innerTextField ->
                                if (uiState.query.isEmpty()) {
                                    Text(
                                        text = "搜索目录、正文、作者、笔记...",
                                        color = StitchTokens.Outline,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        )

                        if (uiState.query.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.clearQuery() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "清除",
                                    tint = StitchTokens.Outline,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.onSearchSubmitted(uiState.query)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "搜索",
                                tint = StitchTokens.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // ================= 2. TAB ROW (TOC 目录搜索 | 正文全文搜索 | 书名搜索) =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchTab.entries.forEach { tab ->
                    val isSelected = tab == uiState.activeTab
                    val count = when (tab) {
                        SearchTab.TOC -> uiState.tocResults.size
                        SearchTab.CONTENT -> uiState.contentResults.size
                        SearchTab.TITLE -> uiState.titleResults.size
                    }
                    val label = if (uiState.query.isNotBlank() && count > 0) {
                        "${tab.title} ($count)"
                    } else {
                        tab.title
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isSelected) StitchTokens.Primary else StitchTokens.SurfaceContainerLow
                            )
                            .clickable { viewModel.onTabSelected(tab) }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) StitchTokens.Background else StitchTokens.Outline,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            HorizontalDivider(
                color = StitchTokens.BorderDivider,
                thickness = 0.5.dp,
                modifier = Modifier.padding(top = 4.dp)
            )

            // ================= 3. RESULTS AREA =================
            if (uiState.query.isBlank()) {
                SearchEmptyState(
                    history = uiState.searchHistory.map { it.query },
                    onKeywordClick = { kw ->
                        viewModel.selectHistoryItem(kw)
                        focusManager.clearFocus()
                    },
                    onDeleteHistoryItem = { viewModel.deleteHistoryItem(it) },
                    onClearAllHistory = { viewModel.clearAllHistory() }
                )
            } else {
                when (uiState.activeTab) {
                    SearchTab.TOC -> {
                        TocResultsView(
                            results = uiState.tocResults,
                            query = uiState.query,
                            onChapterClick = { bookId, chapterIdx ->
                                onChapterClick(bookId, chapterIdx, uiState.query)
                            }
                        )
                    }
                    SearchTab.CONTENT -> {
                        ContentResultsView(
                            results = uiState.contentResults,
                            query = uiState.query,
                            onSnippetClick = { snippet ->
                                onSnippetClick(snippet, uiState.query)
                            }
                        )
                    }
                    SearchTab.TITLE -> {
                        TitleResultsView(
                            results = uiState.titleResults,
                            query = uiState.query,
                            onBookClick = onBookClick
                        )
                    }
                }
            }
        }
    }
}

/**
 * TOC (目录) Search Results View
 * Highlights matching chapter titles with estimated page position and direct click-to-jump.
 */
@Composable
private fun TocResultsView(
    results: List<ChapterWithBookInfo>,
    query: String,
    onChapterClick: (bookId: Long, chapterIndex: Int) -> Unit
) {
    if (results.isEmpty()) {
        EmptyNotice("未在目录中找到与“$query”匹配的章节")
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("toc_results_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(results, key = { "toc_${it.bookId}_${it.id}" }) { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StitchTokens.SurfaceContainerLowest)
                    .clickable { onChapterClick(item.bookId, item.chapterIndex) }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "《${item.bookTitle}》",
                        fontSize = 12.sp,
                        color = StitchTokens.Outline,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Estimated page / chapter position
                    Text(
                        text = "第 ${item.chapterIndex + 1} 章",
                        fontSize = 11.sp,
                        color = StitchTokens.OutlineVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Chapter title with highlighted keyword
                val annotatedTitle = buildAnnotatedString {
                    val title = item.title
                    val idx = title.indexOf(query, ignoreCase = true)
                    if (idx >= 0) {
                        append(title.substring(0, idx))
                        withStyle(SpanStyle(color = StitchTokens.Primary, fontWeight = FontWeight.Bold)) {
                            append(title.substring(idx, idx + query.length))
                        }
                        append(title.substring(idx + query.length))
                    } else {
                        withStyle(SpanStyle(color = StitchTokens.Primary, fontWeight = FontWeight.SemiBold)) {
                            append(title)
                        }
                    }
                }

                Text(
                    text = annotatedTitle,
                    fontSize = 14.5.sp,
                    color = StitchTokens.Primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Full-Text Content Search Results View
 * Supports short terms, long continuous paragraphs, and fuzzy match indicators.
 */
@Composable
private fun ContentResultsView(
    results: List<SearchResultSnippet>,
    query: String,
    onSnippetClick: (SearchResultSnippet) -> Unit
) {
    if (results.isEmpty()) {
        EmptyNotice("未在正文中检索到与“$query”匹配的内容\n可尝试简化关键词或使用近似字词")
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("content_results_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(results, key = { "snippet_${it.bookId}_${it.chapterIndex}_${it.charOffset}_${it.matchIndexInChapter}" }) { snippet ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StitchTokens.SurfaceContainerLowest)
                    .clickable { onSnippetClick(snippet) }
                    .padding(14.dp)
            ) {
                // Book Title, Chapter Title, and Estimated Page Location Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "《${snippet.bookTitle}》· ${snippet.chapterTitle}",
                        fontSize = 12.sp,
                        color = StitchTokens.Outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Match Type Badge & Page
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (snippet.isFuzzyMatch) StitchTokens.SurfaceContainerHigh else StitchTokens.SurfaceContainer
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = snippet.matchLabel,
                                fontSize = 10.sp,
                                color = if (snippet.isFuzzyMatch) StitchTokens.GoldAccent else StitchTokens.OnSurfaceVariant
                            )
                        }

                        Text(
                            text = "约第 ${snippet.estimatedPage} 页",
                            fontSize = 11.sp,
                            color = StitchTokens.OutlineVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Rich context snippet text with query highlighting
                val cleanSnippet = snippet.snippetText
                val annotatedSnippet = buildAnnotatedString {
                    val idx = cleanSnippet.indexOf(query, ignoreCase = true)
                    if (idx >= 0) {
                        append(cleanSnippet.substring(0, idx))
                        withStyle(
                            SpanStyle(
                                color = StitchTokens.Primary,
                                fontWeight = FontWeight.Bold,
                                background = StitchTokens.SurfaceContainerHigh
                            )
                        ) {
                            append(cleanSnippet.substring(idx, idx + query.length))
                        }
                        append(cleanSnippet.substring(idx + query.length))
                    } else {
                        // For long paragraphs or fuzzy match, highlight key segment
                        withStyle(SpanStyle(color = StitchTokens.OnSurface)) {
                            append(cleanSnippet)
                        }
                    }
                }

                Text(
                    text = annotatedSnippet,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = StitchTokens.OnSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Title & Author Search Results View
 */
@Composable
private fun TitleResultsView(
    results: List<BookEntity>,
    query: String,
    onBookClick: (Long) -> Unit
) {
    if (results.isEmpty()) {
        EmptyNotice("未找到书名或作者中包含“$query”的书籍")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(results, key = { it.id }) { book ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StitchTokens.SurfaceContainerLowest)
                    .clickable { onBookClick(book.id) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BookCover(
                    book = book,
                    modifier = Modifier
                        .width(44.dp)
                        .clip(RoundedCornerShape(2.dp))
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cleanBookTitle(book.title),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = StitchTokens.Primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "作者：${book.author.ifBlank { "佚名" }} · 格式：${book.format}",
                        fontSize = 12.sp,
                        color = StitchTokens.Outline
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchEmptyState(
    history: List<String>,
    onKeywordClick: (String) -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onClearAllHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (history.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = StitchTokens.Outline,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "搜索历史",
                    fontSize = 13.sp,
                    color = StitchTokens.Outline,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onClearAllHistory,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "清空历史",
                        tint = StitchTokens.OutlineVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                history.take(12).forEach { item ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(StitchTokens.SurfaceContainerLow)
                            .clickable { onKeywordClick(item) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = item,
                            color = StitchTokens.OnSurface,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyNotice(msg: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = msg,
            color = StitchTokens.Outline,
            fontSize = 13.5.sp,
            lineHeight = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
