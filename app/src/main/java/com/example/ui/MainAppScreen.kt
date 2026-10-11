package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.BookRepository
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import com.example.ui.ambiance.AmbianceSceneOverlay
import com.example.ui.bookshelf.BookshelfScreen
import com.example.ui.components.drawBotanicalFoliage
import com.example.ui.components.drawWonderlandRealisticFoliage
import com.example.ui.components.TrapezoidBookshelfIcons
import com.example.ui.notes.NotesVaultScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.reader.ReaderScreen
import com.example.ui.search.RealtimeSearchScreen
import com.example.ui.theme.StitchTokens
import com.example.ui.viewmodel.BookshelfViewModel
import com.example.ui.viewmodel.BookshelfViewModelFactory
import com.example.ui.viewmodel.ReaderViewModel
import com.example.ui.viewmodel.ReaderViewModelFactory
import com.example.ui.viewmodel.SearchViewModel
import com.example.ui.viewmodel.SearchViewModelFactory
import com.example.util.LocalAppLangCode
import com.example.util.LocalAppLanguage
import com.example.util.LocaleHelper

/**
 * Fixed 4 bottom entries according to Stitch Design specification:
 * 书架、搜索、笔记、我的
 */
enum class BottomNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    SHELF("书架", TrapezoidBookshelfIcons.Filled, TrapezoidBookshelfIcons.Outlined, "tab_shelf"),
    SEARCH("搜索", Icons.Filled.Search, Icons.Outlined.Search, "tab_search"),
    NOTES("笔记", Icons.Filled.EditNote, Icons.Outlined.EditNote, "tab_notes"),
    MINE("我的", Icons.Filled.Person, Icons.Outlined.Person, "tab_mine")
}

@Composable
fun MainAppScreen(
    repository: BookRepository,
    modifier: Modifier = Modifier,
    initialBookId: Long = -1L,
    initialAction: String? = null,
    hasWindowFocus: Boolean = false,
    onConsumedIntent: () -> Unit = {}
) {
    val context = LocalContext.current
    val bookshelfViewModel: BookshelfViewModel = viewModel(
        factory = BookshelfViewModelFactory(repository)
    )
    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(repository)
    )

    var currentTab by remember { mutableStateOf(BottomNavTab.SHELF) }
    var activeReadingBookId by remember { mutableLongStateOf(-1L) }
    var targetChapterIndex by remember { mutableIntStateOf(-1) }
    var targetHighlightKeyword by remember { mutableStateOf("") }
    var targetSnippetText by remember { mutableStateOf("") }
    var targetMatchIndexInChapter by remember { mutableIntStateOf(0) }

    val allBooks by repository.allBooks.collectAsStateWithLifecycle(emptyList())
    val allNotes by repository.allNotes.collectAsStateWithLifecycle(emptyList())

    LaunchedEffect(initialBookId, initialAction) {
        if (initialBookId > 0) {
            activeReadingBookId = initialBookId
            targetChapterIndex = -1
            targetHighlightKeyword = ""
            targetSnippetText = ""
            targetMatchIndexInChapter = 0
            onConsumedIntent()
        } else if (initialAction == com.example.MainActivity.ACTION_SEARCH) {
            currentTab = BottomNavTab.SEARCH
            onConsumedIntent()
        }
    }

    val shelfUiState by bookshelfViewModel.uiState.collectAsStateWithLifecycle()
    val effectiveLangCode = remember(context, shelfUiState.appLanguage) {
        LocaleHelper.getEffectiveLanguage(context, shelfUiState.appLanguage)
    }

    val currentDensity = LocalDensity.current
    val customDensity = remember(currentDensity, shelfUiState.appFontScale) {
        Density(
            density = currentDensity.density,
            fontScale = currentDensity.fontScale * shelfUiState.appFontScale
        )
    }

    CompositionLocalProvider(
        LocalDensity provides customDensity,
        LocalAppLanguage provides shelfUiState.appLanguage,
        LocalAppLangCode provides effectiveLangCode
    ) {
        // ================= FULLSCREEN READER MODE =================
        if (activeReadingBookId > 0) {
            val readerViewModel: ReaderViewModel = viewModel(
                key = "reader_${activeReadingBookId}_${targetChapterIndex}_${targetHighlightKeyword}_${targetMatchIndexInChapter}",
                factory = ReaderViewModelFactory(
                    bookId = activeReadingBookId,
                    repository = repository,
                    initialChapterIndex = targetChapterIndex,
                    initialHighlightKeyword = targetHighlightKeyword,
                    initialSnippetText = targetSnippetText,
                    initialMatchIndexInChapter = targetMatchIndexInChapter
                )
            )

            BackHandler {
                activeReadingBookId = -1L
                targetChapterIndex = -1
                targetHighlightKeyword = ""
                targetSnippetText = ""
                targetMatchIndexInChapter = 0
            }

            ReaderScreen(
                viewModel = readerViewModel,
                onBackToShelf = {
                    activeReadingBookId = -1L
                    targetChapterIndex = -1
                    targetHighlightKeyword = ""
                    targetSnippetText = ""
                    targetMatchIndexInChapter = 0
                },
                hasWindowFocus = hasWindowFocus,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Back handler returns to SHELF if on secondary tab
            if (currentTab != BottomNavTab.SHELF) {
                BackHandler { currentTab = BottomNavTab.SHELF }
            }

            Scaffold(
                contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                containerColor = StitchTokens.Background,
                bottomBar = {
                    // Fixed 4-Entry Bottom Navigation Bar
                    Surface(
                        color = StitchTokens.Background,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Prominent hairline divider at top edge of bottom bar
                            HorizontalDivider(
                                color = if (StitchTokens.IsDark) androidx.compose.ui.graphics.Color(0xFF2A2A2A) else androidx.compose.ui.graphics.Color(0xFFD6D0C6),
                                thickness = 1.dp
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .defaultMinSize(minHeight = 62.dp)
                                    .wrapContentHeight(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BottomNavTab.entries.forEach { tab ->
                                    val isSelected = tab == currentTab
                                    val icon = if (isSelected) tab.selectedIcon else tab.unselectedIcon
                                    // Deepened text and icon colors for improved contrast and visual clarity
                                    val iconTint = if (isSelected) StitchTokens.Primary else if (StitchTokens.IsDark) androidx.compose.ui.graphics.Color(0xFF9E9E9E) else androidx.compose.ui.graphics.Color(0xFF7A756D)
                                    val textTint = if (isSelected) StitchTokens.Primary else if (StitchTokens.IsDark) androidx.compose.ui.graphics.Color(0xFFB5B5BA) else androidx.compose.ui.graphics.Color(0xFF5A5650)

                                    val tabTitle = when (tab) {
                                        BottomNavTab.SHELF -> com.example.util.appString("tab_shelf")
                                        BottomNavTab.SEARCH -> com.example.util.appString("tab_search")
                                        BottomNavTab.NOTES -> com.example.util.appString("tab_notes")
                                        BottomNavTab.MINE -> com.example.util.appString("tab_mine")
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .defaultMinSize(minHeight = 62.dp)
                                            .wrapContentHeight()
                                            .testTag(tab.testTag)
                                            .clickable { currentTab = tab }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = tabTitle,
                                            tint = iconTint,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = tabTitle,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontFamily = StitchTokens.AppFontFamily,
                                            color = textTint
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                val showBotanical = StitchTokens.ShowBotanicalBackground
                val botanicalColor = StitchTokens.BotanicalColor
                val showWonderland = StitchTokens.ShowWonderlandBackground
                val primaryColor = StitchTokens.Primary
                val wonderlandAccent = Color(0x35D4A359)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(StitchTokens.Background)
                        .drawBehind {
                            if (showWonderland) {
                                drawWonderlandRealisticFoliage(
                                    primaryColor = primaryColor,
                                    accentColor = wonderlandAccent
                                )
                            } else if (showBotanical && botanicalColor != Color.Transparent) {
                                drawBotanicalFoliage(botanicalColor)
                            }
                        }
                ) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "MainAppScreenTabTransition"
                    ) { targetTab ->
                        when (targetTab) {
                            BottomNavTab.SHELF -> {
                                BookshelfScreen(
                                    viewModel = bookshelfViewModel,
                                    onBookClick = { bookId ->
                                        targetChapterIndex = -1
                                        targetHighlightKeyword = ""
                                        targetSnippetText = ""
                                        targetMatchIndexInChapter = 0
                                        activeReadingBookId = bookId
                                    },
                                    onNavigateToSearch = {
                                        currentTab = BottomNavTab.SEARCH
                                    },
                                    onNavigateToSearchWithQuery = { q ->
                                        searchViewModel.onQueryChange(q)
                                        currentTab = BottomNavTab.SEARCH
                                    }
                                )
                            }
                            BottomNavTab.SEARCH -> {
                                RealtimeSearchScreen(
                                    viewModel = searchViewModel,
                                    onBookClick = { bookId ->
                                        targetChapterIndex = -1
                                        targetHighlightKeyword = ""
                                        targetSnippetText = ""
                                        targetMatchIndexInChapter = 0
                                        activeReadingBookId = bookId
                                    },
                                    onChapterClick = { bookId, chapterIdx, keyword ->
                                        targetChapterIndex = chapterIdx
                                        targetHighlightKeyword = keyword
                                        targetSnippetText = ""
                                        targetMatchIndexInChapter = 0
                                        activeReadingBookId = bookId
                                    },
                                    onSnippetClick = { snippet, keyword ->
                                        targetChapterIndex = snippet.chapterIndex
                                        targetHighlightKeyword = keyword
                                        targetSnippetText = snippet.snippetText
                                        targetMatchIndexInChapter = snippet.matchIndexInChapter
                                        activeReadingBookId = snippet.bookId
                                    },
                                    onBack = {
                                        currentTab = BottomNavTab.SHELF
                                    }
                                )
                            }
                            BottomNavTab.NOTES -> {
                                NotesVaultScreen(
                                    userNotes = allNotes,
                                    books = allBooks,
                                    onOpenBook = { bookId ->
                                        targetChapterIndex = -1
                                        targetHighlightKeyword = ""
                                        targetSnippetText = ""
                                        targetMatchIndexInChapter = 0
                                        activeReadingBookId = bookId
                                    }
                                )
                            }
                            BottomNavTab.MINE -> {
                                ProfileScreen(
                                    bookshelfViewModel = bookshelfViewModel
                                )
                            }
                        }
                    }

                    // Global atmospheric ambiance overlay (Apple 4S Window Rain, Snow, Stars, etc.)
                    AmbianceSceneOverlay(
                        modifier = Modifier.fillMaxSize(),
                        isDarkTheme = StitchTokens.IsDark
                    )
                }
            }
        }
    }
}
