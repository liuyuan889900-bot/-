package com.example.ui.reader

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.zIndex
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.material3.FilledTonalButton
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.BookmarkEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.ReadingNoteEntity
import com.example.data.repository.SearchResultSnippet
import com.example.ui.model.PageTurnMode
import com.example.ui.model.PaperTextureStyle
import com.example.ui.model.ReaderSettings
import com.example.ui.model.ReaderThemeType
import com.example.ui.theme.OrangeContainer
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.StitchTokens
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ReaderUiState
import com.example.ui.viewmodel.ReaderViewModel
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.outlined.Window
import com.example.ui.ambiance.AmbiancePickerSheet
import com.example.ui.ambiance.AmbianceSceneOverlay
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import coil.compose.AsyncImage
import com.example.ui.components.drawWonderlandRealisticFoliage
import com.example.ui.components.drawBotanicalFoliage
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onBackToShelf: () -> Unit,
    hasWindowFocus: Boolean = false,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = uiState.settings.theme
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var showAmbianceSheet by remember { mutableStateOf(false) }

    // Immersive Fullscreen Controller: hide system navigation keys & status bar by default, show when toolbars open
    val activity = context as? Activity
    val window = activity?.window

    DisposableEffect(Unit) {
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
                window.isStatusBarContrastEnforced = false
            }

            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            val hideBars = {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                    android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                )
            }

            hideBars()
            window.decorView.post { hideBars() }
            window.decorView.postDelayed({ hideBars() }, 50)
            window.decorView.postDelayed({ hideBars() }, 200)

            @Suppress("DEPRECATION")
            window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and android.view.View.SYSTEM_UI_FLAG_FULLSCREEN == 0 || visibility and android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION == 0) {
                    hideBars()
                }
            }
        }

        onDispose {
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
                @Suppress("DEPRECATION")
                window.decorView.setOnSystemUiVisibilityChangeListener(null)
            }
        }
    }

    // Intercept Back Press: If search bar is open, exit search bar first rather than returning to bookshelf
    BackHandler(enabled = uiState.showInBookSearchBar) {
        keyboardController?.hide()
        focusManager.clearFocus()
        viewModel.setInBookSearchBarOpen(false)
        viewModel.clearInBookSearch()
    }

    // Task 1: Clipboard & Text Selection to Note linkage
    // Detects when text was copied while reading and provides a one-click direct import to notes
    var copiedQuoteForNote by remember { mutableStateOf<String?>(null) }
    val androidClipboard = remember {
        context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
    }

    DisposableEffect(androidClipboard) {
        val listener = android.content.ClipboardManager.OnPrimaryClipChangedListener {
            try {
                val clip = androidClipboard?.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    val rawText = clip.getItemAt(0)?.coerceToText(context)?.toString()?.trim()
                    if (!rawText.isNullOrBlank() && rawText.length >= 2) {
                        copiedQuoteForNote = rawText
                    }
                }
            } catch (_: Exception) {}
        }
        androidClipboard?.addPrimaryClipChangedListener(listener)
        onDispose {
            androidClipboard?.removePrimaryClipChangedListener(listener)
        }
    }

    // In-reader text selection state (支持复制、画线、直接存入笔记)
    var selectedText by remember { mutableStateOf("") }
    var selectedRange by remember { mutableStateOf<IntRange?>(null) }
    val isSelectionActive = selectedText.isNotBlank() && selectedRange != null

    // Reset selection when changing page or chapter
    LaunchedEffect(uiState.currentPageIndex, uiState.currentChapterIndex) {
        selectedText = ""
        selectedRange = null
    }

    // Intercept Back Press: If selection is active, clear selection first
    BackHandler(enabled = isSelectionActive) {
        selectedText = ""
        selectedRange = null
    }

    val showBotanical = StitchTokens.ShowBotanicalBackground
    val botanicalColor = StitchTokens.BotanicalColor
    val showWonderland = StitchTokens.ShowWonderlandBackground
    val primaryColor = StitchTokens.Primary
    val wonderlandAccent = Color(0x35D4A359)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .drawBehind {
                drawPaperTexture(theme)
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
        // Main Reading Content Page (Simulation curl, continuous vertical scroll, or slide)
        ReaderContentView(
            modifier = Modifier.fillMaxSize(),
            uiState = uiState,
            isSelectionActive = isSelectionActive,
            activeSelectedRange = selectedRange,
            onTextSelected = { text, range ->
                selectedText = text
                selectedRange = range
            },
            onClearSelection = {
                selectedText = ""
                selectedRange = null
            },
            onBackToShelf = onBackToShelf,
            onOpenSearch = { viewModel.setInBookSearchBarOpen(true) },
            onCenterTap = {
                if (uiState.showInBookSearchBar) {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    viewModel.setInBookSearchBarOpen(false)
                    viewModel.clearInBookSearch()
                    viewModel.hideToolbars()
                } else {
                    viewModel.toggleToolbars()
                }
            },
            onLeftTap = { viewModel.prevPage() },
            onRightTap = { viewModel.nextPage() },
            onNextChapter = { viewModel.jumpToChapter(uiState.currentChapterIndex + 1, targetPage = 0) },
            onPrevChapter = { viewModel.jumpToChapter(uiState.currentChapterIndex - 1, targetPage = -1) },
            onEpubPageChanged = { page -> viewModel.updateEpubCurrentPage(page) },
            onClearHighlight = { viewModel.clearHighlightKeyword() },
            onAddNote = { quote ->
                viewModel.setNoteDialogOpen(true, quote)
            },
            onChapterScrolled = { chapIdx ->
                viewModel.updateCurrentChapterFromScroll(chapIdx)
            },
            hasWindowFocus = hasWindowFocus,
            onPageCountUpdated = { totalPages ->
                viewModel.updateEpubPageCount(uiState.currentChapterIndex, totalPages)
                viewModel.updateTotalPages(totalPages)
            },
            onViewportDimensionsChanged = { widthDp, heightDp, density, fontScale ->
                viewModel.updateViewportDimensions(widthDp, heightDp, density, fontScale)
            }
        )

        // Task 9: Atmospheric Reading Scenes Overlay (Rain, Snow, Stars, Petals)
        // Positioned directly over the reading page, fully non-intrusive and transparent to touches
        AmbianceSceneOverlay(
            modifier = Modifier.fillMaxSize(),
            isDarkTheme = theme.isDark
        )

        // Top Immersive Reading Toolbar (Instant display, zero animation latency)
        if (uiState.showToolbars && !uiState.showInBookSearchBar) {
            Box(modifier = Modifier.align(Alignment.TopCenter)) {
                ReaderTopBar(
                    bookTitle = uiState.book?.title ?: "读丛书",
                    chapterTitle = uiState.currentChapter?.title,
                    isBookmarked = uiState.bookmarks.any { it.chapterIndex == uiState.currentChapterIndex },
                    isDark = uiState.settings.theme == ReaderThemeType.NIGHT,
                    onBack = onBackToShelf,
                    onOpenToc = { viewModel.setTocDrawerOpen(true) },
                    onOpenSearch = { viewModel.setInBookSearchBarOpen(true) },
                    onToggleBookmark = { viewModel.addCurrentBookmark() }
                )
            }
        }

        // Inline Top Search Bar (Displayed directly at the top without dialog popup)
        if (uiState.showInBookSearchBar) {
            Box(modifier = Modifier.align(Alignment.TopCenter)) {
                ReaderTopSearchBar(
                    uiState = uiState,
                    onClose = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        viewModel.setInBookSearchBarOpen(false)
                        viewModel.clearInBookSearch()
                    },
                    onQueryChange = { viewModel.searchInBook(it) },
                    onSelectResult = { snippet, query ->
                        // Automatically hide software keyboard when search item is clicked to view content
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        viewModel.jumpToSearchResult(snippet, query)
                        viewModel.setInBookSearchBarOpen(false)
                        viewModel.hideToolbars()
                    }
                )
            }
        }

        // Bottom Immersive Reading Toolbar (Instant display, slim & compact layout)
        if (uiState.showToolbars && !uiState.showInBookSearchBar) {
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                ReaderBottomBar(
                    uiState = uiState,
                    onPrevPage = { viewModel.prevPage() },
                    onNextPage = { viewModel.nextPage() },
                    onProgressChange = { viewModel.seekChapterProgress(it) },
                    onToggleNight = { viewModel.toggleNightMode() },
                    onOpenSettings = { viewModel.setSettingsSheetOpen(true) },
                    onOpenToc = { viewModel.setTocDrawerOpen(true) },
                    onOpenAmbiance = { showAmbianceSheet = true },
                    onOpenNote = { viewModel.setNoteDialogOpen(true, copiedQuoteForNote ?: "") }
                )
            }
        }

        // TOC / Bookmarks / Notes Drawer / Modal
        if (uiState.showTocDrawer) {
            TocModalDrawer(
                uiState = uiState,
                onDismiss = { viewModel.setTocDrawerOpen(false) },
                onSelectChapter = { idx -> viewModel.jumpToChapter(idx) },
                onSelectBookmark = { bm -> viewModel.jumpToChapter(bm.chapterIndex) },
                onDeleteBookmark = { bmId -> viewModel.deleteBookmark(bmId) },
                onDeleteNote = { noteId -> viewModel.deleteNote(noteId) }
            )
        }

        // Font & Theme Settings Sheet
        if (uiState.showSettingsSheet) {
            ReaderSettingsBottomSheet(
                settings = uiState.settings,
                onDismiss = { viewModel.setSettingsSheetOpen(false) },
                onThemeSelect = { viewModel.updateTheme(it) },
                onFontSizeChange = { viewModel.updateFontSize(it) },
                onLineSpacingChange = { viewModel.updateLineSpacing(it) },
                onPageModeChange = { viewModel.updatePageTurnMode(it) },
                onToggleShowPageIndicatorAlways = { viewModel.updateShowPageIndicatorAlways(it) },
                onFontTypeChange = { viewModel.updateFontType(it) },
                onToggleBold = { viewModel.toggleBold() },
                onEncodingChange = { viewModel.updateEncoding(it) }
            )
        }

        // Task 1: Floating One-Click Note Import Banner for copied text
        AnimatedVisibility(
            visible = copiedQuoteForNote != null && !uiState.showNoteDialog,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (uiState.showToolbars) 140.dp else 40.dp, start = 16.dp, end = 16.dp)
                .zIndex(100f)
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = StitchTokens.SurfaceContainerHigh,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, StitchTokens.Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notes,
                            contentDescription = null,
                            tint = StitchTokens.Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "已复制选中文本",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchTokens.Primary,
                                fontFamily = StitchTokens.AppFontFamily
                            )
                            Text(
                                text = copiedQuoteForNote ?: "",
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = StitchTokens.Outline,
                                fontFamily = StitchTokens.AppFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val textToSave = copiedQuoteForNote ?: ""
                            viewModel.setNoteDialogOpen(true, textToSave)
                            copiedQuoteForNote = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StitchTokens.Primary,
                            contentColor = StitchTokens.OnPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 34.dp)
                            .wrapContentHeight()
                    ) {
                        Text(
                            text = "一键存为笔记",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = StitchTokens.AppFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { copiedQuoteForNote = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = StitchTokens.Outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Floating Text Selection Toolbar (复制 / 画线 / 存入笔记 / 写想法)
        AnimatedVisibility(
            visible = isSelectionActive && !uiState.showNoteDialog,
            enter = fadeIn(tween(180)) + slideInVertically(tween(200)) { it / 2 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (uiState.showToolbars) 120.dp else 56.dp)
                .zIndex(105f)
        ) {
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            SelectionActionToolbar(
                selectedText = selectedText,
                onCopy = {
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(selectedText))
                    android.widget.Toast.makeText(context, "已复制到剪贴板", android.widget.Toast.LENGTH_SHORT).show()
                    selectedText = ""
                    selectedRange = null
                },
                onUnderline = {
                    viewModel.addUnderline(selectedText)
                    android.widget.Toast.makeText(context, "已画线标记", android.widget.Toast.LENGTH_SHORT).show()
                    selectedText = ""
                    selectedRange = null
                },
                onSaveNote = {
                    // 人性化极简一步直存笔记
                    viewModel.quickSaveNote(selectedText)
                    android.widget.Toast.makeText(context, "已直接存入笔记", android.widget.Toast.LENGTH_SHORT).show()
                    selectedText = ""
                    selectedRange = null
                },
                onAddThought = {
                    val quote = selectedText
                    selectedText = ""
                    selectedRange = null
                    viewModel.setNoteDialogOpen(true, quote)
                },
                onExpandSentence = {
                    val curRange = selectedRange
                    val curText = uiState.pagesContent.getOrNull(uiState.currentPageIndex) ?: ""
                    if (curRange != null && curText.isNotEmpty()) {
                        val fullRange = expandToFullSentence(curText, curRange)
                        val expandedText = curText.substring(
                            fullRange.first,
                            (fullRange.last + 1).coerceAtMost(curText.length)
                        ).trim()
                        if (expandedText.isNotEmpty()) {
                            selectedRange = fullRange
                            selectedText = expandedText
                            android.widget.Toast.makeText(context, "已智能扩展至整句（${expandedText.length}字）", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onDismiss = {
                    selectedText = ""
                    selectedRange = null
                }
            )
        }

        // Note Taking Dialog
        if (uiState.showNoteDialog) {
            AddNoteDialog(
                quoteText = uiState.selectedTextForNote,
                onDismiss = { viewModel.setNoteDialogOpen(false) },
                onSave = { noteText, colorHex ->
                    viewModel.saveNote(noteText, colorHex)
                }
            )
        }

        // Task 9: Ambiance Atmospheric Scene Picker Sheet
        if (showAmbianceSheet) {
            AmbiancePickerSheet(
                onDismissRequest = { showAmbianceSheet = false }
            )
        }
    }
}

private fun DrawScope.drawPaperTexture(theme: ReaderThemeType) {
    if (theme.isDark) {
        return
    }

    // Subtle left-spine reading shadow for realistic bookbinding curvature
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color(0x14000000),
                Color(0x08000000),
                Color(0x02000000),
                Color.Transparent
            ),
            startX = 0f,
            endX = 32.dp.toPx()
        )
    )

    when (theme.textureStyle) {
        PaperTextureStyle.PARCHMENT -> {
            // 1. 掌阅真实复古羊皮纸: toasted vignette perimeter gradient
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0x0C7A4A1C), Color(0x20633B10)),
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    radius = size.width * 0.95f
                )
            )
        }
        PaperTextureStyle.XUAN -> {
            // 2. 传统东方宣纸: delicate ivory warmth
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0x0A4A4238)),
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    radius = size.width
                )
            )
        }
        PaperTextureStyle.APPLE, PaperTextureStyle.NONE, PaperTextureStyle.EINK -> {
            // 3. Apple Books: Pure silky smooth paper with zero frame latency
        }
        PaperTextureStyle.BAMBOO -> {
            // 4. 竹青绿色护眼纸: soft organic gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x0820482B), Color.Transparent, Color(0x0A20482B))
                )
            )
        }
    }
}

@Composable
private fun ReaderContentView(
    uiState: ReaderUiState,
    isSelectionActive: Boolean = false,
    activeSelectedRange: IntRange? = null,
    onTextSelected: (String, IntRange) -> Unit = { _, _ -> },
    onClearSelection: () -> Unit = {},
    onBackToShelf: () -> Unit,
    onOpenSearch: () -> Unit,
    onCenterTap: () -> Unit,
    onLeftTap: () -> Unit,
    onRightTap: () -> Unit,
    onNextChapter: () -> Unit,
    onPrevChapter: () -> Unit,
    onClearHighlight: () -> Unit,
    onAddNote: (String) -> Unit,
    onChapterScrolled: (Int) -> Unit = {},
    hasWindowFocus: Boolean = true,
    onPageCountUpdated: (Int) -> Unit = {},
    onEpubPageChanged: (Int) -> Unit = {},
    onViewportDimensionsChanged: (Float, Float, Float, Float) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val theme = uiState.settings.theme

    val chapterText = uiState.currentChapter?.content ?: ""
    // Enforce 100% native Compose reader engine for maximum speed and instant page flips
    val isUsingWebView = false

    val hPadding = if (isUsingWebView) 0.dp else uiState.settings.horizontalPadding.dp
    // Stable symmetric padding that never changes when toolbars open or close
    val tPadding = 8.dp
    val bPadding = 8.dp

    var lastKnownHeightPx by remember { mutableIntStateOf(0) }
    var lastKnownWidthPx by remember { mutableIntStateOf(0) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val context = LocalContext.current
    val currentLocalDensity = LocalDensity.current

    LaunchedEffect(hasWindowFocus, lastKnownHeightPx, lastKnownWidthPx, currentLocalDensity.fontScale, currentLocalDensity.density) {
        if (hasWindowFocus && lastKnownHeightPx > 0 && lastKnownWidthPx > 0) {
            kotlinx.coroutines.delay(60)
            val density = currentLocalDensity.density.coerceAtLeast(1.0f)
            val fontScale = currentLocalDensity.fontScale.coerceAtLeast(0.5f)
            val widthDp = lastKnownWidthPx / density
            val heightDp = lastKnownHeightPx / density
            onViewportDimensionsChanged(widthDp, heightDp, density, fontScale)
            webViewRef?.evaluateJavascript(
                "window.__setPageHeight && window.__setPageHeight($heightDp);",
                null
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = hPadding)
            .padding(top = tPadding, bottom = bPadding)
    ) {
        // 任务一：阅读页顶部固定显示当前章节标题和章节内页码，例如“第三章 归途 (12/45)”
        ReaderPersistentHeader(
            bookTitle = uiState.book?.title ?: "",
            chapterTitle = uiState.currentChapter?.title ?: "",
            currentPageInChapter = uiState.currentPageInChapter,
            totalPagesInChapter = uiState.totalPagesInChapter,
            theme = theme
        )

        // Highlight Notification Banner if keyword is active
        if (uiState.highlightKeyword.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (theme.isDark) Color(0x332E7D32) else Color(0x224CAF50),
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = if (theme.isDark) Color(0xFFA5D6A7) else Color(0xFF2E7D32),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "已高亮检索词：“${uiState.highlightKeyword}”",
                        fontSize = 11.sp,
                        color = if (theme.isDark) Color(0xFFA5D6A7) else Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "清除高亮",
                        fontSize = 11.sp,
                        color = if (theme.isDark) Color(0xFFC8E6C9) else Color(0xFF1B5E20),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onClearHighlight() }
                    )
                }
            }
        }

        // Content Display based on PageTurnMode (EPUB Native Rich Typography & Full Screen)
        val currentText = uiState.pagesContent.getOrNull(uiState.currentPageIndex) ?: "正在加载书籍..."
        val chapterNotes = remember(uiState.notes) {
            uiState.notes.mapNotNull { it.quoteText.ifBlank { null } }
        }
        val annotatedContent = remember(
            currentText,
            uiState.highlightKeyword,
            theme,
            uiState.settings.fontSize,
            chapterNotes,
            activeSelectedRange
        ) {
            buildHighlightedAnnotatedString(
                text = currentText,
                keyword = uiState.highlightKeyword,
                textColor = theme.textColor,
                isDark = theme.isDark,
                baseFontSizeSp = uiState.settings.fontSize.toFloat(),
                underlines = chapterNotes,
                selectedRange = activeSelectedRange
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .onGloballyPositioned { coordinates ->
                    val stableWidthPx = coordinates.size.width
                    val stableHeightPx = coordinates.size.height
                    if (stableHeightPx != lastKnownHeightPx || stableWidthPx != lastKnownWidthPx) {
                        lastKnownHeightPx = stableHeightPx
                        lastKnownWidthPx = stableWidthPx
                        val density = currentLocalDensity.density.coerceAtLeast(1.0f)
                        val fontScale = currentLocalDensity.fontScale.coerceAtLeast(0.5f)
                        val widthDp = stableWidthPx / density
                        val heightDp = stableHeightPx / density
                        onViewportDimensionsChanged(widthDp, heightDp, density, fontScale)
                        webViewRef?.evaluateJavascript(
                            "window.__setPageHeight && window.__setPageHeight($heightDp);",
                            null
                        )
                    }
                }
        ) {
            if (isUsingWebView) {
                // Render with WebView original EPUB layout (XHTML + CSS, edge-to-edge)
                EpubWebViewReader(
                    chapterHtml = chapterText,
                    chapterIndex = uiState.currentChapterIndex,
                    currentPageInChapter = uiState.currentPageInChapter,
                    theme = theme,
                    fontSize = (uiState.settings.fontSize * currentLocalDensity.fontScale).toInt(),
                    lineSpacingMultiplier = uiState.settings.lineSpacingMultiplier,
                    onCenterTap = onCenterTap,
                    onLeftTap = {
                        if (uiState.currentPageInChapter > 1) {
                            val prev = uiState.currentPageInChapter - 1
                            onEpubPageChanged(prev)
                            webViewRef?.evaluateJavascript("window.__scrollToPage && window.__scrollToPage(${prev - 1});", null)
                        } else {
                            onPrevChapter()
                        }
                    },
                    onRightTap = {
                        if (uiState.currentPageInChapter < uiState.totalPagesInChapter) {
                            val next = uiState.currentPageInChapter + 1
                            onEpubPageChanged(next)
                            webViewRef?.evaluateJavascript("window.__scrollToPage && window.__scrollToPage(${next - 1});", null)
                        } else {
                            onNextChapter()
                        }
                    },
                    onWebViewCreated = { webViewRef = it },
                    onPageCountUpdated = onPageCountUpdated
                )
            } else {
                when (uiState.settings.pageTurnMode) {
                    PageTurnMode.NONE -> {
                        // Instant Page Turn without animation (Default)
                        InstantPageTurnContentView(
                            rawContent = currentText,
                            annotatedContent = annotatedContent,
                            theme = theme,
                            fontSize = uiState.settings.fontSize,
                            lineSpacingMultiplier = uiState.settings.lineSpacingMultiplier,
                            effectiveLineHeightSp = uiState.effectiveLineHeightSp,
                            fontType = uiState.settings.fontType,
                            isBold = uiState.settings.isBold,
                            isSelectionActive = isSelectionActive,
                            selectedRange = activeSelectedRange,
                            onTextSelected = onTextSelected,
                            onClearSelection = onClearSelection,
                            onCenterTap = onCenterTap,
                            onLeftTap = onLeftTap,
                            onRightTap = onRightTap
                        )
                    }
                    PageTurnMode.SCROLL -> {
                        // Continuous Vertical Scroll Mode for entire book
                        ContinuousScrollContentView(
                            uiState = uiState,
                            onCenterTap = onCenterTap,
                            onChapterVisible = onChapterScrolled
                        )
                    }
                    PageTurnMode.SIMULATION -> {
                        // Realistic 3D Simulation Page Turn with Curvature & Shadows
                        SimulationPageTurnContentView(
                            uiState = uiState,
                            rawContent = currentText,
                            annotatedContent = annotatedContent,
                            isSelectionActive = isSelectionActive,
                            selectedRange = activeSelectedRange,
                            onTextSelected = onTextSelected,
                            onClearSelection = onClearSelection,
                            onCenterTap = onCenterTap,
                            onLeftTap = onLeftTap,
                            onRightTap = onRightTap
                        )
                    }
                }
            }
        }

        // 底部常驻页码与进度栏（高度恒定，不随菜单显示隐藏变化）
        if (uiState.settings.pageTurnMode != PageTurnMode.SCROLL) {
            PageIndicatorBar(
                currentPageInChapter = uiState.currentPageInChapter,
                totalPagesInChapter = uiState.totalPagesInChapter,
                theme = theme
            )
        }
    }
}

@Composable
private fun MiniHeaderBar(
    chapterTitle: String,
    progressPercent: Int,
    theme: ReaderThemeType,
    onSearchClick: () -> Unit,
    onBackToShelf: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 24.dp)
            .wrapContentHeight()
            .padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .defaultMinSize(minHeight = 26.dp)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onBackToShelf() }
                    .padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回书架",
                    tint = theme.secondaryTextColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "书架",
                    fontSize = 11.sp,
                    color = theme.secondaryTextColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "·",
                fontSize = 12.sp,
                color = theme.secondaryTextColor.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Text(
                text = chapterTitle,
                fontSize = 11.sp,
                color = theme.secondaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .defaultMinSize(minHeight = 26.dp)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onSearchClick() }
                    .padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索本书",
                    tint = theme.secondaryTextColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "搜索",
                    fontSize = 11.sp,
                    color = theme.secondaryTextColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "全书 $progressPercent%",
                fontSize = 11.sp,
                color = theme.secondaryTextColor
            )
        }
    }
}

@Composable
private fun ReaderPersistentHeader(
    bookTitle: String = "",
    chapterTitle: String = "",
    currentPageInChapter: Int,
    totalPagesInChapter: Int,
    theme: ReaderThemeType,
    modifier: Modifier = Modifier
) {
    val displayTitle = when {
        bookTitle.isNotBlank() && chapterTitle.isNotBlank() -> "$bookTitle · ${com.example.util.normalizeChapterTitle(chapterTitle, 1)}"
        bookTitle.isNotBlank() -> bookTitle
        else -> com.example.util.normalizeChapterTitle(chapterTitle.ifBlank { "未识别章节" }, 1)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 24.dp)
            .wrapContentHeight()
            .padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$displayTitle ($currentPageInChapter/$totalPagesInChapter)",
            fontSize = 12.sp,
            color = theme.textColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
private fun PageIndicatorBar(
    currentPageInChapter: Int,
    totalPagesInChapter: Int,
    theme: ReaderThemeType,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 18.dp)
            .wrapContentHeight()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "本篇 $currentPageInChapter / $totalPagesInChapter 页",
            fontSize = 10.5.sp,
            color = theme.textColor,
            fontWeight = FontWeight.Medium
        )
        val percent = if (totalPagesInChapter > 0) {
            ((currentPageInChapter.toFloat() / totalPagesInChapter) * 100).toInt()
        } else 0
        Text(
            text = "$percent%",
            fontSize = 10.5.sp,
            color = theme.textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ContinuousScrollContentView(
    uiState: ReaderUiState,
    onCenterTap: () -> Unit,
    onChapterVisible: (Int) -> Unit = {}
) {
    val theme = uiState.settings.theme
    val chapters = uiState.chapters
    val listState = rememberLazyListState()

    // Scroll to initial chapter when first loading
    LaunchedEffect(uiState.currentChapterIndex) {
        val targetItem = (uiState.currentChapterIndex + 1).coerceIn(0, chapters.size)
        if (uiState.currentChapterIndex in chapters.indices && listState.firstVisibleItemIndex != targetItem) {
            listState.scrollToItem(targetItem)
        }
    }

    // Observe scroll to synchronize current chapter index
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { firstIndex ->
                if (firstIndex > 0) {
                    val chapIdx = (firstIndex - 1).coerceIn(0, (chapters.size - 1).coerceAtLeast(0))
                    onChapterVisible(chapIdx)
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(onCenterTap) {
                detectTapGestures(
                    onTap = { onCenterTap() }
                )
            }
    ) {
            // First item: Book Cover & Info Header
            item(key = "book_header") {
                BookCoverInfoHeader(book = uiState.book, theme = theme)
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(
                count = chapters.size,
                key = { index -> chapters[index].id }
            ) { index ->
                val chapter = chapters[index]

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    // Chapter Title Header
                    val displayTitle = com.example.util.normalizeChapterTitle(chapter.title, index + 1)
                    Surface(
                        color = if (theme.isDark) Color(0x22FFFFFF) else Color(0x18000000),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = buildHighlightedAnnotatedString(
                                text = displayTitle,
                                keyword = uiState.highlightKeyword,
                                textColor = theme.textColor,
                                isDark = theme.isDark
                            ),
                            fontSize = (uiState.settings.fontSize + 2).sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textColor,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }

                    // Chapter Text Body
                    val paragraphs = remember(chapter.content) {
                        chapter.content.trim().split(Regex("\r?\n+"))
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                            .map { p ->
                                if (p.startsWith("　　") || p.startsWith("    ")) p.trimStart()
                                else p
                            }
                    }

                    paragraphs.forEach { p ->
                        Text(
                            text = buildHighlightedAnnotatedString(
                                text = "　　$p",
                                keyword = uiState.highlightKeyword,
                                textColor = theme.textColor,
                                isDark = theme.isDark
                            ),
                            fontSize = uiState.settings.fontSize.sp,
                            color = theme.textColor,
                            fontFamily = uiState.settings.fontType.fontFamily,
                            fontWeight = if (uiState.settings.isBold) FontWeight.Bold else FontWeight.Normal,
                            lineHeight = (uiState.settings.fontSize * uiState.settings.lineSpacingMultiplier).sp,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(if (theme.isDark) Color(0x22FFFFFF) else Color(0x15000000))
                    )
                }
            }

            item(key = "book_end") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "— 全书完 —",
                        fontSize = 14.sp,
                        color = theme.secondaryTextColor
                    )
                }
            }
        }
}

// Global highlight builder for all reader text elements with rich formatting support
fun buildHighlightedAnnotatedString(
    text: String,
    keyword: String,
    textColor: Color,
    isDark: Boolean,
    baseFontSizeSp: Float = 18f,
    underlines: List<String> = emptyList(),
    selectedRange: IntRange? = null
): AnnotatedString {
    return com.example.util.EpubRichTextFormatter.formatRichText(
        content = text,
        baseFontSizeSp = baseFontSizeSp,
        textColor = textColor,
        isDark = isDark,
        highlightKeyword = keyword,
        underlines = underlines,
        selectedRange = selectedRange
    )
}

// Original Layout EPUB WebView Reader (renders raw XHTML + CSS, full screen with multi-column pagination)
@Composable
private fun EpubWebViewReader(
    chapterHtml: String,
    chapterIndex: Int,
    currentPageInChapter: Int,
    theme: ReaderThemeType,
    fontSize: Int,
    lineSpacingMultiplier: Float,
    onCenterTap: () -> Unit,
    onLeftTap: () -> Unit,
    onRightTap: () -> Unit,
    onWebViewCreated: (WebView) -> Unit = {},
    onPageCountUpdated: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Extract base URL and clean HTML from EPUB_META if present
    val (cleanedHtml, baseUrl) = remember(chapterHtml) {
        val metaMatch = Regex("""<!--\s*EPUB_META:\s*relPath=([^;]+);hash=([^\s]+)\s*-->""").find(chapterHtml)
        if (metaMatch != null) {
            val relPath = metaMatch.groupValues[1]
            val hash = metaMatch.groupValues[2]
            val cleaned = chapterHtml.replace(metaMatch.value, "").trim()
            val chapterFile = java.io.File(context.filesDir, "epub_extracted/$hash/$relPath")
            val base = if (chapterFile.exists()) {
                val parent = chapterFile.parentFile
                if (parent != null) "file://${parent.absolutePath}/" else "file://${context.filesDir.absolutePath}/"
            } else {
                val baseDir = java.io.File(context.filesDir, "epub_extracted/$hash/")
                "file://${baseDir.absolutePath}/"
            }
            Pair(cleaned, base)
        } else {
            Pair(chapterHtml, "file://${context.filesDir.absolutePath}/")
        }
    }

    val hexColor = remember(theme) {
        val c = theme.textColor
        String.format("#%02x%02x%02x", (c.red * 255).toInt(), (c.green * 255).toInt(), (c.blue * 255).toInt())
    }

    val processedHtml = remember(cleanedHtml, hexColor, fontSize, lineSpacingMultiplier, theme) {
        val nightFilter = if (theme == ReaderThemeType.NIGHT) {
            "img, svg, figure, image { opacity: 0.82 !important; filter: brightness(0.85) contrast(1.05) !important; }"
        } else ""

        val injectionStyle = """
            <style id="epub-engine-injected-style">
                html {
                    margin: 0 !important;
                    padding: 0 !important;
                    width: 100vw !important;
                    height: 100vh !important;
                    overflow: hidden !important;
                    box-sizing: border-box !important;
                    -webkit-user-select: none;
                    user-select: none;
                    background-color: transparent !important;
                }
                body {
                    margin: 0 !important;
                    padding: 0 16px !important;
                    width: 100vw !important;
                    height: 100vh !important;
                    box-sizing: border-box !important;
                    overflow: hidden !important;
                    background-color: transparent !important;
                    color: $hexColor !important;
                    font-size: ${fontSize}px !important;
                    line-height: $lineSpacingMultiplier !important;
                    word-break: break-word !important;
                    overflow-wrap: break-word !important;
                    -webkit-column-width: 100vw !important;
                    column-width: 100vw !important;
                    -webkit-column-gap: 32px !important;
                    column-gap: 32px !important;
                    -webkit-column-fill: auto !important;
                    column-fill: auto !important;
                }
                
                /* Relative typography: headers remain prominent and bold */
                h1 { font-size: 1.6em !important; font-weight: bold !important; line-height: 1.3 !important; }
                h2 { font-size: 1.35em !important; font-weight: bold !important; line-height: 1.35 !important; }
                h3 { font-size: 1.2em !important; font-weight: bold !important; line-height: 1.4 !important; }
                h4 { font-size: 1.1em !important; font-weight: bold !important; }
                b, strong { font-weight: bold !important; }
                i, em { font-style: italic !important; }

                /* Prevent image splitting across pages and ensure responsive display */
                img, svg, figure, .illustration, image {
                    max-width: 100% !important;
                    max-height: 72vh !important;
                    height: auto !important;
                    object-fit: contain !important;
                    display: block !important;
                    margin: 8px auto !important;
                    page-break-inside: avoid !important;
                    break-inside: avoid !important;
                }

                p, div, span, li, blockquote, a, table {
                    color: inherit;
                }
                a {
                    text-decoration: none;
                }
                $nightFilter
            </style>
            <script id="epub-engine-injected-script">
                window.__calcPagination = function() {
                    try {
                        var width = window.innerWidth || document.documentElement.clientWidth || 1;
                        var scrollW = Math.max(
                            document.body ? document.body.scrollWidth : 0,
                            document.documentElement ? document.documentElement.scrollWidth : 0
                        );
                        var totalPages = Math.max(1, Math.round(scrollW / width));
                        if (window.AndroidBridge && window.AndroidBridge.onPageCountUpdated) {
                            window.AndroidBridge.onPageCountUpdated(totalPages);
                        }
                        return totalPages;
                    } catch(e) {
                        return 1;
                    }
                };

                window.__scrollToPage = function(pageIndex) {
                    try {
                        var width = window.innerWidth || document.documentElement.clientWidth || 1;
                        var targetX = Math.max(0, pageIndex) * width;
                        window.scrollTo({
                            left: targetX,
                            top: 0,
                            behavior: 'instant'
                        });
                    } catch(e) {}
                };

                window.__applyReaderSettings = function(fontSize, lineSpacing, textColorHex) {
                    try {
                        var dyn = document.getElementById('epub-reader-dynamic-style');
                        if (!dyn) {
                            dyn = document.createElement('style');
                            dyn.id = 'epub-reader-dynamic-style';
                            document.head.appendChild(dyn);
                        }
                        dyn.textContent = "body { color: " + textColorHex + " !important; font-size: " + fontSize + "px !important; line-height: " + lineSpacing + " !important; } p, div, span, li, blockquote, a { color: inherit; }";
                        window.__calcPagination();
                    } catch(e) {}
                };

                window.addEventListener('DOMContentLoaded', function() { window.__calcPagination(); });
                window.addEventListener('load', function() { window.__calcPagination(); });
                setTimeout(window.__calcPagination, 100);
                setTimeout(window.__calcPagination, 300);
            </script>
        """.trimIndent()

        val htmlWithMeta = if (!cleanedHtml.contains("<meta charset", ignoreCase = true)) {
            val metaTag = """<meta charset="utf-8"/><meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>"""
            if (cleanedHtml.contains("<head>", ignoreCase = true)) {
                cleanedHtml.replace(Regex("(?i)<head>"), "<head>$metaTag")
            } else if (cleanedHtml.contains("<head ", ignoreCase = true)) {
                cleanedHtml.replace(Regex("(?i)<head[^>]*>"), "$0$metaTag")
            } else {
                cleanedHtml
            }
        } else {
            cleanedHtml
        }

        if (htmlWithMeta.contains("</head>", ignoreCase = true)) {
            htmlWithMeta.replace(Regex("(?i)</head>"), "$injectionStyle</head>")
        } else if (htmlWithMeta.contains("<head>", ignoreCase = true)) {
            htmlWithMeta.replace(Regex("(?i)<head>"), "<head>$injectionStyle")
        } else if (htmlWithMeta.contains("<html>", ignoreCase = true) || htmlWithMeta.contains("<html ", ignoreCase = true)) {
            htmlWithMeta.replace(Regex("(?i)<html[^>]*>"), "$0<head>$injectionStyle</head>")
        } else {
            """<!DOCTYPE html><html><head><meta charset="utf-8"/><meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>$injectionStyle</head><body>$htmlWithMeta</body></html>"""
        }
    }

    // Keep track of what was loaded into the WebView to prevent unnecessary reloads
    val lastLoadedContentKey = remember { arrayOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(onCenterTap, onLeftTap, onRightTap) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val startX = down.position.x
                    val startY = down.position.y
                    var totalDragX = 0f
                    var totalDragY = 0f
                    var isDrag = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            val width = size.width.toFloat()
                            if (!isDrag) {
                                val tapX = change.position.x
                                when {
                                    tapX < width * 0.30f -> onLeftTap()
                                    tapX > width * 0.70f -> onRightTap()
                                    else -> onCenterTap()
                                }
                            } else {
                                if (totalDragX < -35f && kotlin.math.abs(totalDragX) > kotlin.math.abs(totalDragY)) {
                                    onRightTap()
                                } else if (totalDragX > 35f && kotlin.math.abs(totalDragX) > kotlin.math.abs(totalDragY)) {
                                    onLeftTap()
                                }
                            }
                            change.consume()
                            break
                        } else {
                            totalDragX = change.position.x - startX
                            totalDragY = change.position.y - startY
                            if (kotlin.math.abs(totalDragX) > 16f || kotlin.math.abs(totalDragY) > 16f) {
                                isDrag = true
                            }
                        }
                    }
                }
            }
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    overScrollMode = WebView.OVER_SCROLL_NEVER

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        allowFileAccess = true
                        allowContentAccess = true
                        allowFileAccessFromFileURLs = true
                        allowUniversalAccessFromFileURLs = true
                        defaultFontSize = fontSize
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }

                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onPageCountUpdated(totalPages: Int) {
                            post {
                                onPageCountUpdated(totalPages)
                            }
                        }
                    }, "AndroidBridge")

                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                            val uri = request?.url ?: return null
                            if (uri.scheme.equals("file", ignoreCase = true)) {
                                val path = uri.path ?: ""
                                var file = java.io.File(path)
                                if (!file.exists() || !file.isFile) {
                                    val name = path.substringAfterLast('/')
                                    val extractedRoot = java.io.File(ctx.filesDir, "epub_extracted")
                                    if (extractedRoot.exists()) {
                                        val found = extractedRoot.walkTopDown().maxDepth(6).find { it.isFile && it.name.equals(name, ignoreCase = true) }
                                        if (found != null) {
                                            file = found
                                        }
                                    }
                                }
                                if (file.exists() && file.isFile) {
                                    val ext = file.extension.lowercase()
                                    val mimeType = when (ext) {
                                        "css" -> "text/css"
                                        "jpg", "jpeg" -> "image/jpeg"
                                        "png" -> "image/png"
                                        "gif" -> "image/gif"
                                        "webp" -> "image/webp"
                                        "svg" -> "image/svg+xml"
                                        "ttf" -> "font/ttf"
                                        "otf" -> "font/otf"
                                        "woff" -> "font/woff"
                                        "woff2" -> "font/woff2"
                                        "js" -> "application/javascript"
                                        "html", "xhtml" -> "text/html"
                                        else -> "application/octet-stream"
                                    }
                                    try {
                                        return WebResourceResponse(mimeType, "UTF-8", java.io.FileInputStream(file))
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            return super.shouldInterceptRequest(view, request)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.evaluateJavascript("window.__calcPagination && window.__calcPagination();", null)
                            view?.evaluateJavascript("window.__scrollToPage && window.__scrollToPage(${currentPageInChapter - 1});", null)
                        }
                    }
                    onWebViewCreated(this)
                }
            },
            update = { webView ->
                onWebViewCreated(webView)
                val contentKey = "$chapterIndex:${processedHtml.hashCode()}:$baseUrl"
                if (lastLoadedContentKey[0] != contentKey) {
                    lastLoadedContentKey[0] = contentKey
                    webView.loadDataWithBaseURL(
                        baseUrl,
                        processedHtml,
                        "text/html",
                        "UTF-8",
                        null
                    )
                } else {
                    // Fast dynamic update without reloading page
                    webView.evaluateJavascript(
                        "window.__applyReaderSettings && window.__applyReaderSettings($fontSize, $lineSpacingMultiplier, '$hexColor');",
                        null
                    )
                    webView.evaluateJavascript(
                        "window.__scrollToPage && window.__scrollToPage(${currentPageInChapter - 1});",
                        null
                    )
                }
            }
        )
    }
}

private fun findSentenceBounds(text: String, charIndex: Int): IntRange {
    if (text.isBlank() || charIndex !in text.indices) return 0..0
    val sentenceDelimiters = setOf('。', '！', '？', '；', '\n', '.', '!', '?', ';')
    var start = charIndex
    while (start > 0 && text[start - 1] !in sentenceDelimiters) {
        start--
    }
    var end = charIndex
    while (end < text.length - 1 && text[end] !in sentenceDelimiters) {
        end++
    }
    if (end < text.length && text[end] in sentenceDelimiters && text[end] != '\n') {
        end++
    }
    val cleanStart = start.coerceIn(0, text.length - 1)
    val cleanEnd = (end - 1).coerceAtLeast(cleanStart)
    return cleanStart..cleanEnd
}

private fun expandToFullSentence(text: String, currentRange: IntRange): IntRange {
    if (text.isBlank() || currentRange.first !in text.indices) return currentRange
    val sentenceDelimiters = setOf('。', '！', '？', '；', '\n', '.', '!', '?', ';')
    var start = currentRange.first
    while (start > 0 && text[start - 1] !in sentenceDelimiters) {
        start--
    }
    var end = currentRange.last
    while (end < text.length - 1 && text[end] !in sentenceDelimiters) {
        end++
    }
    val cleanStart = start.coerceIn(0, (text.length - 1).coerceAtLeast(0))
    val cleanEnd = end.coerceIn(cleanStart, (text.length - 1).coerceAtLeast(0))
    return cleanStart..cleanEnd
}

@Composable
private fun SelectionActionToolbar(
    selectedText: String,
    onCopy: () -> Unit,
    onUnderline: () -> Unit,
    onSaveNote: () -> Unit,
    onAddThought: () -> Unit = {},
    onExpandSentence: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        // Apple-style smart selection tip capsule
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xD81E1E1E),
            border = BorderStroke(0.6.dp, Color(0x33FFFFFF)),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFFFFD166),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "已选 ${selectedText.length} 字 · 长按/画两道小竖选中 · 拖动手柄精准微调",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Main Apple-style floating action callout
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = StitchTokens.SurfaceContainerHighest,
            border = BorderStroke(1.dp, StitchTokens.Border),
            shadowElevation = 14.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 整句扩展 (Apple-style 1-tap Smart Expansion)
                FilledTonalButton(
                    onClick = onExpandSentence,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = StitchTokens.Primary.copy(alpha = 0.16f),
                        contentColor = StitchTokens.Primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "整句",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "整句",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = StitchTokens.AppFontFamily
                    )
                }

                VerticalDivider(
                    modifier = Modifier.height(16.dp).width(0.8.dp),
                    color = StitchTokens.BorderDivider
                )

                // 复制
                TextButton(
                    onClick = onCopy,
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "复制",
                        tint = StitchTokens.Primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "复制",
                        color = StitchTokens.Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = StitchTokens.AppFontFamily
                    )
                }

                VerticalDivider(
                    modifier = Modifier.height(16.dp).width(0.8.dp),
                    color = StitchTokens.BorderDivider
                )

                // 画线
                TextButton(
                    onClick = onUnderline,
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "画线",
                        tint = StitchTokens.Primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "画线",
                        color = StitchTokens.Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = StitchTokens.AppFontFamily
                    )
                }

                VerticalDivider(
                    modifier = Modifier.height(16.dp).width(0.8.dp),
                    color = StitchTokens.BorderDivider
                )

                // 存入笔记 (一键速存，人性化直存)
                Button(
                    onClick = onSaveNote,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StitchTokens.Primary,
                        contentColor = StitchTokens.OnPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 11.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "存入笔记",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "存入笔记",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = StitchTokens.AppFontFamily
                    )
                }

                VerticalDivider(
                    modifier = Modifier.height(16.dp).width(0.8.dp),
                    color = StitchTokens.BorderDivider
                )

                // 想法
                TextButton(
                    onClick = onAddThought,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = "想法",
                        tint = StitchTokens.Primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "想法",
                        color = StitchTokens.Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = StitchTokens.AppFontFamily
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = StitchTokens.Outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private enum class SelectionHandleType {
    NONE, START, END, PENDING_FIRST
}

@Composable
private fun AppleTextSelectionLayer(
    rawContent: String,
    layoutResult: TextLayoutResult?,
    isSelectionActive: Boolean,
    selectedRange: IntRange?,
    pendingFirstMarkOffset: Int?,
    activeDragHandle: SelectionHandleType,
    dragTouchPos: Offset,
    activeStrokePoints: List<Offset>,
    onClearPendingFirstMark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "markerPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // 用户要求：保留画两个小竖选择，竖的颜色不要红色，要深绿
    val handleDeepGreen = Color(0xFF1B5E20) // Deep forest green
    val handlePulseGreen = Color(0xFF2E7D32)

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Drawing Canvas for Ink Trails, Vertical Lines and Selection Handles
        Canvas(modifier = Modifier.fillMaxSize()) {
            // (a) Active Ink Pen Stroke Trail
            if (activeStrokePoints.size > 1) {
                val path = Path()
                path.moveTo(activeStrokePoints.first().x, activeStrokePoints.first().y)
                for (p in activeStrokePoints.drop(1)) {
                    path.lineTo(p.x, p.y)
                }
                drawPath(
                    path = path,
                    color = handleDeepGreen.copy(alpha = 0.85f),
                    style = Stroke(
                        width = 3.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            val lr = layoutResult
            if (lr != null && rawContent.isNotEmpty()) {
                // (b) Pending First Mark (首个竖线光标 + 顶部圆点手柄 + 呼吸光晕)
                if (pendingFirstMarkOffset != null && !isSelectionActive) {
                    val mOff = pendingFirstMarkOffset.coerceIn(0, (rawContent.length - 1).coerceAtLeast(0))
                    val markBox = lr.getBoundingBox(mOff)
                    val markX = markBox.left
                    val markTop = markBox.top
                    val markBottom = markBox.bottom
                    val knobCenter = Offset(markX, markTop - 2.5.dp.toPx())
                    val knobRadius = if (activeDragHandle == SelectionHandleType.PENDING_FIRST) 9.dp.toPx() else 6.5.dp.toPx()

                    // Glowing Pulse Halo
                    drawCircle(
                        color = handlePulseGreen.copy(alpha = pulseAlpha * 0.35f),
                        radius = 15.dp.toPx(),
                        center = knobCenter
                    )

                    // Vertical Bar | exactly in front of the first character (深绿色)
                    drawLine(
                        color = handleDeepGreen,
                        start = Offset(markX, markTop - 2.dp.toPx()),
                        end = Offset(markX, markBottom + 2.dp.toPx()),
                        strokeWidth = 2.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Top Handle Pin Knob (Deep Green teardrop handle with white core)
                    drawCircle(
                        color = handleDeepGreen,
                        radius = knobRadius,
                        center = knobCenter
                    )
                    drawCircle(
                        color = Color.White,
                        radius = knobRadius * 0.45f,
                        center = knobCenter
                    )
                    drawCircle(
                        color = Color.White,
                        radius = knobRadius,
                        center = knobCenter,
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }

                // (c) Two Vertical Lines & Selection Handles when selection is active
                if (isSelectionActive && selectedRange != null) {
                    val sOff = selectedRange.first.coerceIn(0, (rawContent.length - 1).coerceAtLeast(0))
                    val eOff = selectedRange.last.coerceIn(0, (rawContent.length - 1).coerceAtLeast(0))
                    val startBox = lr.getBoundingBox(sOff)
                    val endBox = lr.getBoundingBox(eOff)

                    val startX = startBox.left
                    val startTop = startBox.top
                    val startBottom = startBox.bottom

                    val endX = endBox.right
                    val endTop = endBox.top
                    val endBottom = endBox.bottom

                    // Start Handle (Vertical bar at left of first char + Top Pin Knob)
                    val startKnobRadius = if (activeDragHandle == SelectionHandleType.START) 9.dp.toPx() else 6.5.dp.toPx()
                    val startKnobCenter = Offset(startX, startTop - 2.5.dp.toPx())

                    drawLine(
                        color = handleDeepGreen,
                        start = Offset(startX, startTop - 2.dp.toPx()),
                        end = Offset(startX, startBottom + 2.dp.toPx()),
                        strokeWidth = 2.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = handleDeepGreen,
                        radius = startKnobRadius,
                        center = startKnobCenter
                    )
                    drawCircle(
                        color = Color.White,
                        radius = startKnobRadius * 0.45f,
                        center = startKnobCenter
                    )
                    drawCircle(
                        color = Color.White,
                        radius = startKnobRadius,
                        center = startKnobCenter,
                        style = Stroke(width = 1.2.dp.toPx())
                    )

                    // End Handle (Vertical bar at right of last char + Bottom Pin Knob)
                    // Exactly behind the last character: no extra character sitting outside the line!
                    val endKnobRadius = if (activeDragHandle == SelectionHandleType.END) 9.dp.toPx() else 6.5.dp.toPx()
                    val endKnobCenter = Offset(endX, endBottom + 2.5.dp.toPx())

                    drawLine(
                        color = handleDeepGreen,
                        start = Offset(endX, endTop - 2.dp.toPx()),
                        end = Offset(endX, endBottom + 2.dp.toPx()),
                        strokeWidth = 2.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = handleDeepGreen,
                        radius = endKnobRadius,
                        center = endKnobCenter
                    )
                    drawCircle(
                        color = Color.White,
                        radius = endKnobRadius * 0.45f,
                        center = endKnobCenter
                    )
                    drawCircle(
                        color = Color.White,
                        radius = endKnobRadius,
                        center = endKnobCenter,
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }
            }
        }

        // 2. Floating Hint Capsule at Top (当画完第一道竖线时出现，引导画第二道竖线)
        AnimatedVisibility(
            visible = pendingFirstMarkOffset != null && !isSelectionActive,
            enter = fadeIn(tween(160)) + slideInVertically(tween(180)) { -it },
            exit = fadeOut(tween(140)) + slideOutVertically(tween(160)) { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, handleDeepGreen.copy(alpha = 0.40f)),
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = handleDeepGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "已标注开头 ｜ 在结尾字后画第二道小竖即可选中",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "· 随时拖动微调",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = handleDeepGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    IconButton(
                        onClick = onClearPendingFirstMark,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "取消",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // 3. Real-time Hover Bubble while Dragging Handles
        if (activeDragHandle != SelectionHandleType.NONE) {
            val selectedCount = if (selectedRange != null) {
                (selectedRange.last - selectedRange.first + 1).coerceAtLeast(1)
            } else 1

            val tipText = when (activeDragHandle) {
                SelectionHandleType.START -> "已选 $selectedCount 字 · 调整开头"
                SelectionHandleType.END -> "已选 $selectedCount 字 · 调整结尾"
                SelectionHandleType.PENDING_FIRST -> "微调开头竖线"
                else -> "滑动微调"
            }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (dragTouchPos.x - 65.dp.toPx()).toInt().coerceIn(16, 800),
                            y = (dragTouchPos.y - 65.dp.toPx()).toInt().coerceAtLeast(12)
                        )
                    }
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xF21C1C1E),
                    border = BorderStroke(0.8.dp, Color(0x55FFFFFF)),
                    shadowElevation = 12.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = tipText,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "精准微调",
                            color = Color(0xFF81C784),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Resolves precise character target offset when dragging selection handles.
 * Uses SelectionEngine.findLineForY with exact half line-spacing boundary matching.
 * This guarantees you can drag to line 1, 2, 3 smoothly without jumping over lines!
 */
private fun getTargetOffsetForHandleDrag(
    lr: TextLayoutResult,
    rawContent: String,
    touchPos: Offset,
    handleType: SelectionHandleType,
    curRange: IntRange
): Int {
    val isEnd = handleType == SelectionHandleType.END
    return SelectionEngine.resolveExactCharOffset(
        lr = lr,
        content = rawContent,
        touchPos = touchPos,
        isEndHandle = isEnd,
        applyFingerOffset = true,
        fingerOffsetY = 38f
    )
}

/**
 * Handles Apple-style vertical stroke gestures:
 * 1. If not selected: two vertical lines define start (front of 1st char) and end (behind last char).
 * 2. If already selected: drawing a vertical line instantly expands or shrinks the selection.
 */
private fun processVerticalStrokeSelection(
    lr: TextLayoutResult,
    rawContent: String,
    midPoint: Offset,
    isSelectionActive: Boolean,
    curRange: IntRange?,
    pendingFirstMark: Int?,
    pendingFirstTime: Long,
    onSelect: (String, IntRange) -> Unit,
    onSetPendingMark: (Int) -> Unit,
    onHaptic: () -> Unit
) {
    if (rawContent.isEmpty()) return
    val cursor = lr.getOffsetForPosition(midPoint).coerceIn(0, rawContent.length)

    if (isSelectionActive && curRange != null) {
        // Already selected: directly adjust selection boundaries without dragging!
        // "如果增加或减少选择的字，不用拖动，直接画小竖线就行"
        val sCursor = curRange.first
        val eCursor = curRange.last + 1
        val midCursor = (sCursor + eCursor) / 2

        val newRange: IntRange = when {
            cursor > eCursor -> {
                // Expand end forwards
                val newEnd = (cursor - 1).coerceIn(curRange.first, rawContent.length - 1)
                curRange.first..newEnd
            }
            cursor < sCursor -> {
                // Expand start backwards
                val newStart = cursor.coerceIn(0, curRange.last)
                newStart..curRange.last
            }
            cursor > midCursor -> {
                // Inside selection, closer to end -> shrink end
                val newEnd = (cursor - 1).coerceAtLeast(curRange.first)
                curRange.first..newEnd
            }
            else -> {
                // Inside selection, closer to start -> shrink start
                val newStart = cursor.coerceAtMost(curRange.last)
                newStart..curRange.last
            }
        }
        val text = rawContent.substring(
            newRange.first,
            (newRange.last + 1).coerceAtMost(rawContent.length)
        ).trim()
        if (text.isNotBlank()) {
            onSelect(text, newRange)
            onHaptic()
        }
    } else if (pendingFirstMark == null || System.currentTimeMillis() - pendingFirstTime > 15000L) {
        // First vertical line placed in front of first character!
        onSetPendingMark(cursor.coerceIn(0, rawContent.length - 1))
        onHaptic()
    } else {
        // Second vertical line placed! Select text between first and second vertical lines!
        val c1 = pendingFirstMark
        val c2 = cursor
        val cStart = minOf(c1, c2)
        val cEnd = maxOf(c1, c2)
        val startChar = cStart.coerceIn(0, rawContent.length - 1)
        val endChar = if (cEnd > cStart) {
            (cEnd - 1).coerceIn(startChar, rawContent.length - 1)
        } else {
            startChar
        }
        val newRange = startChar..endChar
        val selected = rawContent.substring(
            newRange.first,
            (newRange.last + 1).coerceAtMost(rawContent.length)
        ).trim()
        if (selected.isNotBlank()) {
            onSelect(selected, newRange)
            onHaptic()
        }
    }
}

@Composable
private fun InstantPageTurnContentView(
    rawContent: String,
    annotatedContent: androidx.compose.ui.text.AnnotatedString,
    theme: ReaderThemeType,
    fontSize: Int,
    lineSpacingMultiplier: Float,
    effectiveLineHeightSp: Float = 0f,
    fontType: com.example.ui.theme.ReaderFontType = com.example.ui.theme.ReaderFontType.SERIF,
    isBold: Boolean = false,
    isSelectionActive: Boolean = false,
    selectedRange: IntRange? = null,
    onTextSelected: (String, IntRange) -> Unit = { _, _ -> },
    onClearSelection: () -> Unit = {},
    onCenterTap: () -> Unit,
    onLeftTap: () -> Unit,
    onRightTap: () -> Unit
) {
    val lineH = if (effectiveLineHeightSp > 0f) effectiveLineHeightSp.sp else (fontSize * lineSpacingMultiplier).sp
    val readerTextStyle = LocalTextStyle.current.copy(
        fontSize = fontSize.sp,
        color = theme.textColor,
        fontFamily = fontType.fontFamily,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        lineHeight = lineH,
        letterSpacing = 0.5.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Proportional,
            trim = LineHeightStyle.Trim.Both
        ),
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    )

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var activeStrokePoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var pendingFirstMarkOffset by remember { mutableStateOf<Int?>(null) }
    var pendingFirstMarkTime by remember { mutableStateOf(0L) }
    var activeDragHandle by remember { mutableStateOf(SelectionHandleType.NONE) }
    var dragTouchPos by remember { mutableStateOf(Offset.Zero) }

    val currentSelectedRange by rememberUpdatedState(selectedRange)
    val currentIsSelectionActive by rememberUpdatedState(isSelectionActive)
    val currentRawContent by rememberUpdatedState(rawContent)
    val currentLayoutResult by rememberUpdatedState(layoutResult)
    val currentOnTextSelected by rememberUpdatedState(onTextSelected)
    val currentOnClearSelection by rememberUpdatedState(onClearSelection)
    val currentOnLeftTap by rememberUpdatedState(onLeftTap)
    val currentOnRightTap by rememberUpdatedState(onRightTap)
    val currentOnCenterTap by rememberUpdatedState(onCenterTap)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reader_instant_content_view")
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val startX = down.position.x
                    val startY = down.position.y
                    val startTime = System.currentTimeMillis()
                    val stroke = mutableListOf(down.position)
                    var isDrag = false
                    var totalDragX = 0f
                    var totalDragY = 0f

                    // 1. Check Handle Hit Test (前后可随意拖动红色的竖线与选择手柄，高灵敏度大触控区域)
                    val lr = currentLayoutResult
                    val touchX = down.position.x
                    val touchY = down.position.y
                    val handleHitRadius = 64.dp.toPx()
                    val handleHitHoriz = 52.dp.toPx()
                    val handleHitVertMargin = 45.dp.toPx()

                    var hitHandle = SelectionHandleType.NONE
                    if (currentIsSelectionActive && currentSelectedRange != null && lr != null && currentRawContent.isNotEmpty()) {
                        val sOff = currentSelectedRange!!.first.coerceIn(0, currentRawContent.length - 1)
                        val eOff = currentSelectedRange!!.last.coerceIn(0, currentRawContent.length - 1)
                        val startBox = lr.getBoundingBox(sOff)
                        val endBox = lr.getBoundingBox(eOff)

                        val distStartKnob = kotlin.math.hypot(touchX - startBox.left, touchY - (startBox.top - 2.5.dp.toPx()))
                        val distEndKnob = kotlin.math.hypot(touchX - endBox.right, touchY - (endBox.bottom + 2.5.dp.toPx()))

                        val nearStartLine = kotlin.math.abs(touchX - startBox.left) <= handleHitHoriz &&
                                touchY in (startBox.top - handleHitVertMargin)..(startBox.bottom + handleHitVertMargin)
                        val nearEndLine = kotlin.math.abs(touchX - endBox.right) <= handleHitHoriz &&
                                touchY in (endBox.top - handleHitVertMargin)..(endBox.bottom + handleHitVertMargin)

                        if (distStartKnob <= handleHitRadius || nearStartLine) {
                            hitHandle = SelectionHandleType.START
                        } else if (distEndKnob <= handleHitRadius || nearEndLine) {
                            hitHandle = SelectionHandleType.END
                        }
                    } else if (pendingFirstMarkOffset != null && lr != null && currentRawContent.isNotEmpty()) {
                        val mOff = pendingFirstMarkOffset!!.coerceIn(0, currentRawContent.length - 1)
                        val markBox = lr.getBoundingBox(mOff)
                        val distMarkKnob = kotlin.math.hypot(touchX - markBox.left, touchY - (markBox.top - 2.5.dp.toPx()))
                        val nearMarkLine = kotlin.math.abs(touchX - markBox.left) <= handleHitHoriz &&
                                touchY in (markBox.top - handleHitVertMargin)..(markBox.bottom + handleHitVertMargin)

                        if (distMarkKnob <= handleHitRadius || nearMarkLine) {
                            hitHandle = SelectionHandleType.PENDING_FIRST
                        }
                    }

                    if (hitHandle != SelectionHandleType.NONE) {
                        activeDragHandle = hitHandle
                        dragTouchPos = down.position
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        down.consume()
                    } else {
                        activeDragHandle = SelectionHandleType.NONE
                    }

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) {
                            val totalTime = System.currentTimeMillis() - startTime
                            val width = size.width.toFloat()
                            val endPos = change.position
                            val dist = kotlin.math.hypot(endPos.x - startX, endPos.y - startY)
                            val absX = kotlin.math.abs(totalDragX)
                            val absY = kotlin.math.abs(totalDragY)

                            activeStrokePoints = emptyList()

                            if (activeDragHandle != SelectionHandleType.NONE) {
                                activeDragHandle = SelectionHandleType.NONE
                                change.consume()
                                break
                            }

                            // Quick tap to clear selection or pending mark
                            if (dist < 18f && totalTime < 280L) {
                                if (currentIsSelectionActive) {
                                    currentOnClearSelection()
                                    pendingFirstMarkOffset = null
                                    change.consume()
                                    break
                                } else if (pendingFirstMarkOffset != null) {
                                    pendingFirstMarkOffset = null
                                    change.consume()
                                    break
                                }
                            }

                            // 1. Two vertical lines recognition ("在第一个字前画一竖线，最后一个字后画一竖线即可选中")
                            // 并且："如果增加或减少选择的字，不用拖动，直接画小竖线就行"
                            val isVerticalStroke = (dist in (10.dp.toPx())..(300.dp.toPx()) && absY >= absX * 0.65f && totalTime in 40L..950L)
                            if (isVerticalStroke && !isDrag && currentLayoutResult != null && currentRawContent.isNotEmpty()) {
                                val currentLr = currentLayoutResult!!
                                val midPoint = Offset((startX + endPos.x) / 2f, (startY + endPos.y) / 2f)
                                processVerticalStrokeSelection(
                                    lr = currentLr,
                                    rawContent = currentRawContent,
                                    midPoint = midPoint,
                                    isSelectionActive = currentIsSelectionActive,
                                    curRange = currentSelectedRange,
                                    pendingFirstMark = pendingFirstMarkOffset,
                                    pendingFirstTime = pendingFirstMarkTime,
                                    onSelect = { text, range ->
                                        pendingFirstMarkOffset = null
                                        currentOnTextSelected(text, range)
                                    },
                                    onSetPendingMark = { mark ->
                                        pendingFirstMarkOffset = mark
                                        pendingFirstMarkTime = System.currentTimeMillis()
                                    },
                                    onHaptic = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                                change.consume()
                                break
                            }

                            // 2. Circle gesture check ("用笔画一个圈，把这段文字选择了")
                            if (stroke.size >= 7) {
                                val startPt = stroke.first()
                                val endPt = stroke.last()
                                val closedDist = kotlin.math.hypot(startPt.x - endPt.x, startPt.y - endPt.y)
                                val pathLen = stroke.zipWithNext { a, b ->
                                    kotlin.math.hypot(a.x - b.x, a.y - b.y)
                                }.sum()
                                if (pathLen > 70f && (closedDist < 120f || closedDist < pathLen * 0.38f)) {
                                    val minX = stroke.minOf { it.x }
                                    val maxX = stroke.maxOf { it.x }
                                    val minY = stroke.minOf { it.y }
                                    val maxY = stroke.maxOf { it.y }
                                    val currentLr = currentLayoutResult
                                    if (currentLr != null && currentRawContent.isNotBlank()) {
                                        val o1 = currentLr.getOffsetForPosition(Offset(minX, minY))
                                        val o2 = currentLr.getOffsetForPosition(Offset(maxX, maxY))
                                        val s = kotlin.math.min(o1, o2).coerceIn(0, currentRawContent.length - 1)
                                        val e = kotlin.math.max(o1, o2).coerceIn(0, currentRawContent.length - 1)
                                        val selected = currentRawContent.substring(
                                            s,
                                            (e + 1).coerceAtMost(currentRawContent.length)
                                        ).trim()
                                        if (selected.isNotBlank()) {
                                            pendingFirstMarkOffset = null
                                            currentOnTextSelected(selected, s..e)
                                            change.consume()
                                            break
                                        }
                                    }
                                }
                            }

                            // 3. Underline / drag gesture ("用手指当笔直接画横线划过文字")
                            if (!isDrag && totalTime in 180L..900L && absX > 35f && absX > absY * 1.3f && stroke.size >= 4) {
                                val currentLr = currentLayoutResult
                                if (currentLr != null && currentRawContent.isNotBlank()) {
                                    val o1 = currentLr.getOffsetForPosition(Offset(startX, startY))
                                    val o2 = currentLr.getOffsetForPosition(endPos)
                                    val s = kotlin.math.min(o1, o2).coerceIn(0, currentRawContent.length - 1)
                                    val e = kotlin.math.max(o1, o2).coerceIn(0, currentRawContent.length - 1)
                                    val selected = currentRawContent.substring(
                                        s,
                                        (e + 1).coerceAtMost(currentRawContent.length)
                                    ).trim()
                                    if (selected.isNotBlank()) {
                                        pendingFirstMarkOffset = null
                                        currentOnTextSelected(selected, s..e)
                                        change.consume()
                                        break
                                    }
                                }
                            }

                            // 4. Long-press (>350ms, low displacement)
                            if (totalTime >= 350L && dist < 20f) {
                                val currentLr = currentLayoutResult
                                if (currentLr != null && currentRawContent.isNotBlank()) {
                                    val off = currentLr.getOffsetForPosition(down.position).coerceIn(0, currentRawContent.length - 1)
                                    val sentenceRange = findSentenceBounds(currentRawContent, off)
                                    val selected = currentRawContent.substring(
                                        sentenceRange.first,
                                        (sentenceRange.last + 1).coerceAtMost(currentRawContent.length)
                                    ).trim()
                                    if (selected.isNotBlank()) {
                                        pendingFirstMarkOffset = null
                                        currentOnTextSelected(selected, sentenceRange)
                                        change.consume()
                                        break
                                    }
                                }
                            }

                            // 5. Horizontal Page Turn Swipe
                            if (isDrag) {
                                if (totalDragX < -25f) {
                                    currentOnRightTap() // Swipe left -> next page
                                } else if (totalDragX > 25f) {
                                    currentOnLeftTap() // Swipe right -> prev page
                                }
                            } else {
                                // 6. Standard Instant Tap
                                when {
                                    startX < width * 0.35f -> currentOnLeftTap()
                                    startX > width * 0.65f -> currentOnRightTap()
                                    else -> currentOnCenterTap()
                                }
                            }
                            change.consume()
                            break
                        } else {
                            // Pointer is active and moving (实时极速拖动手柄调整选区)
                            if (activeDragHandle != SelectionHandleType.NONE) {
                                dragTouchPos = change.position
                                val currentLr = currentLayoutResult
                                if (currentLr != null && currentRawContent.isNotEmpty()) {
                                    val curRange = currentSelectedRange ?: (0..0)

                                    when (activeDragHandle) {
                                        SelectionHandleType.START -> {
                                            val newStart = getTargetOffsetForHandleDrag(
                                                currentLr, currentRawContent, change.position, SelectionHandleType.START, curRange
                                            )
                                            if (newStart != curRange.first) {
                                                val newRange = newStart..curRange.last
                                                val text = currentRawContent.substring(
                                                    newRange.first,
                                                    (newRange.last + 1).coerceAtMost(currentRawContent.length)
                                                ).trim()
                                                if (text.isNotBlank()) {
                                                    currentOnTextSelected(text, newRange)
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                            }
                                        }
                                        SelectionHandleType.END -> {
                                            val newEnd = getTargetOffsetForHandleDrag(
                                                currentLr, currentRawContent, change.position, SelectionHandleType.END, curRange
                                            )
                                            if (newEnd != curRange.last) {
                                                val newRange = curRange.first..newEnd
                                                val text = currentRawContent.substring(
                                                    newRange.first,
                                                    (newRange.last + 1).coerceAtMost(currentRawContent.length)
                                                ).trim()
                                                if (text.isNotBlank()) {
                                                    currentOnTextSelected(text, newRange)
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                            }
                                        }
                                        SelectionHandleType.PENDING_FIRST -> {
                                            val newMark = getTargetOffsetForHandleDrag(
                                                currentLr, currentRawContent, change.position, SelectionHandleType.PENDING_FIRST, curRange
                                            )
                                            if (newMark != pendingFirstMarkOffset) {
                                                pendingFirstMarkOffset = newMark
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                                change.consume()
                            } else {
                                totalDragX = change.position.x - startX
                                totalDragY = change.position.y - startY
                                val absX = kotlin.math.abs(totalDragX)
                                val absY = kotlin.math.abs(totalDragY)
                                stroke.add(change.position)
                                if (stroke.size > 2) {
                                    activeStrokePoints = stroke.toList()
                                }
                                if (absX > 22.dp.toPx() && absX > absY * 1.5f) {
                                    isDrag = true
                                    change.consume()
                                }
                            }
                        }
                    }
                }
            }
    ) {
        ReaderPageContentRenderer(
            rawText = rawContent,
            annotatedText = annotatedContent,
            style = readerTextStyle,
            onTextLayout = { layoutResult = it },
            modifier = Modifier.fillMaxSize()
        )

        // Apple-style intelligent selection layer
        AppleTextSelectionLayer(
            rawContent = rawContent,
            layoutResult = layoutResult,
            isSelectionActive = isSelectionActive,
            selectedRange = selectedRange,
            pendingFirstMarkOffset = pendingFirstMarkOffset,
            activeDragHandle = activeDragHandle,
            dragTouchPos = dragTouchPos,
            activeStrokePoints = activeStrokePoints,
            onClearPendingFirstMark = { pendingFirstMarkOffset = null }
        )
    }
}

/**
 * Universal Reader Page Renderer
 * Renders either pure styled text or rich EPUB illustrations with text!
 */
@Composable
private fun ReaderPageContentRenderer(
    rawText: String,
    annotatedText: androidx.compose.ui.text.AnnotatedString,
    style: TextStyle,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val imgMatch = remember(rawText) {
        Regex("""\[IMG:([^\]]+)\]""").find(rawText)
    }

    if (imgMatch != null) {
        val imagePath = imgMatch.groupValues[1]
        val resolvedModel = remember(imagePath) {
            val clean = if (imagePath.startsWith("file://")) imagePath.removePrefix("file://") else imagePath
            val directFile = java.io.File(clean)
            if (directFile.exists() && directFile.length() > 0) {
                directFile
            } else {
                val filename = clean.substringAfterLast('/')
                val decodedFilename = try { java.net.URLDecoder.decode(filename, "UTF-8") } catch (_: Exception) { filename }
                val extractedRoot = java.io.File(context.filesDir, "epub_extracted")
                val found = if (extractedRoot.exists()) {
                    extractedRoot.walkTopDown().firstOrNull {
                        it.isFile && (it.name.equals(filename, ignoreCase = true) || it.name.equals(decodedFilename, ignoreCase = true))
                    }
                } else null
                found ?: clean
            }
        }
        val textWithoutImg = remember(rawText) {
            rawText.replace(imgMatch.value, "").trim()
        }

        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (textWithoutImg.isBlank()) Arrangement.Center else Arrangement.Top
        ) {
            if (textWithoutImg.isNotBlank()) {
                val cleanAnnotated = remember(textWithoutImg) {
                    androidx.compose.ui.text.buildAnnotatedString {
                        append(textWithoutImg)
                    }
                }
                Text(
                    text = cleanAnnotated,
                    style = style,
                    onTextLayout = onTextLayout,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            AsyncImage(
                model = resolvedModel,
                contentDescription = "书籍插图",
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (textWithoutImg.isBlank()) Modifier.fillMaxHeight(0.92f)
                        else Modifier.heightIn(max = 300.dp)
                    )
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Fit
            )
        }
    } else {
        Text(
            text = annotatedText,
            style = style,
            onTextLayout = onTextLayout,
            modifier = modifier.fillMaxSize()
        )
    }
}

@Composable
private fun SimulationPageTurnContentView(
    uiState: ReaderUiState,
    rawContent: String,
    annotatedContent: androidx.compose.ui.text.AnnotatedString,
    isSelectionActive: Boolean = false,
    selectedRange: IntRange? = null,
    onTextSelected: (String, IntRange) -> Unit = { _, _ -> },
    onClearSelection: () -> Unit = {},
    onCenterTap: () -> Unit,
    onLeftTap: () -> Unit,
    onRightTap: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val theme = uiState.settings.theme
    val curlAnim = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var isTurningForward by remember { mutableStateOf(true) }
    var simLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var activeStrokePoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var pendingFirstMarkOffset by remember { mutableStateOf<Int?>(null) }
    var pendingFirstMarkTime by remember { mutableStateOf(0L) }
    var activeDragHandle by remember { mutableStateOf(SelectionHandleType.NONE) }
    var dragTouchPos by remember { mutableStateOf(Offset.Zero) }

    val currentSelectedRange by rememberUpdatedState(selectedRange)
    val currentIsSelectionActive by rememberUpdatedState(isSelectionActive)
    val currentRawContent by rememberUpdatedState(rawContent)
    val currentLayoutResult by rememberUpdatedState(simLayoutResult)
    val currentOnTextSelected by rememberUpdatedState(onTextSelected)
    val currentOnClearSelection by rememberUpdatedState(onClearSelection)
    val currentOnLeftTap by rememberUpdatedState(onLeftTap)
    val currentOnRightTap by rememberUpdatedState(onRightTap)
    val currentOnCenterTap by rememberUpdatedState(onCenterTap)

    val nextPageText = if (uiState.currentPageIndex < uiState.pagesContent.size - 1) {
        uiState.pagesContent.getOrNull(uiState.currentPageIndex + 1)
    } else null

    val prevPageText = if (uiState.currentPageIndex > 0) {
        uiState.pagesContent.getOrNull(uiState.currentPageIndex - 1)
    } else null

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reader_simulation_content_view")
            .pointerInput(uiState.currentPageIndex) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val startX = down.position.x
                    val startY = down.position.y
                    val startTime = System.currentTimeMillis()
                    val stroke = mutableListOf(down.position)
                    var totalDragX = 0f
                    var totalDragY = 0f
                    val dragThresholdPx = 28f

                    // 1. Check Handle Hit Test (前后可随意拖动红色的竖线与选择手柄，高灵敏度大触控区域)
                    val lr = currentLayoutResult
                    val touchX = down.position.x
                    val touchY = down.position.y
                    val handleHitRadius = 64.dp.toPx()
                    val handleHitHoriz = 52.dp.toPx()
                    val handleHitVertMargin = 45.dp.toPx()

                    var hitHandle = SelectionHandleType.NONE
                    if (currentIsSelectionActive && currentSelectedRange != null && lr != null && currentRawContent.isNotEmpty()) {
                        val sOff = currentSelectedRange!!.first.coerceIn(0, currentRawContent.length - 1)
                        val eOff = currentSelectedRange!!.last.coerceIn(0, currentRawContent.length - 1)
                        val startBox = lr.getBoundingBox(sOff)
                        val endBox = lr.getBoundingBox(eOff)

                        val distStartKnob = kotlin.math.hypot(touchX - startBox.left, touchY - (startBox.top - 2.5.dp.toPx()))
                        val distEndKnob = kotlin.math.hypot(touchX - endBox.right, touchY - (endBox.bottom + 2.5.dp.toPx()))

                        val nearStartLine = kotlin.math.abs(touchX - startBox.left) <= handleHitHoriz &&
                                touchY in (startBox.top - handleHitVertMargin)..(startBox.bottom + handleHitVertMargin)
                        val nearEndLine = kotlin.math.abs(touchX - endBox.right) <= handleHitHoriz &&
                                touchY in (endBox.top - handleHitVertMargin)..(endBox.bottom + handleHitVertMargin)

                        if (distStartKnob <= handleHitRadius || nearStartLine) {
                            hitHandle = SelectionHandleType.START
                        } else if (distEndKnob <= handleHitRadius || nearEndLine) {
                            hitHandle = SelectionHandleType.END
                        }
                    } else if (pendingFirstMarkOffset != null && lr != null && currentRawContent.isNotEmpty()) {
                        val mOff = pendingFirstMarkOffset!!.coerceIn(0, currentRawContent.length - 1)
                        val markBox = lr.getBoundingBox(mOff)
                        val distMarkKnob = kotlin.math.hypot(touchX - markBox.left, touchY - (markBox.top - 2.5.dp.toPx()))
                        val nearMarkLine = kotlin.math.abs(touchX - markBox.left) <= handleHitHoriz &&
                                touchY in (markBox.top - handleHitVertMargin)..(markBox.bottom + handleHitVertMargin)

                        if (distMarkKnob <= handleHitRadius || nearMarkLine) {
                            hitHandle = SelectionHandleType.PENDING_FIRST
                        }
                    }

                    if (hitHandle != SelectionHandleType.NONE) {
                        activeDragHandle = hitHandle
                        dragTouchPos = down.position
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        down.consume()
                    } else {
                        activeDragHandle = SelectionHandleType.NONE
                    }

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) {
                            val totalTime = System.currentTimeMillis() - startTime
                            val endPos = change.position
                            val dist = kotlin.math.hypot(endPos.x - startX, endPos.y - startY)
                            val width = size.width.toFloat()
                            val absX = kotlin.math.abs(totalDragX)
                            val absY = kotlin.math.abs(totalDragY)

                            activeStrokePoints = emptyList()

                            if (activeDragHandle != SelectionHandleType.NONE) {
                                activeDragHandle = SelectionHandleType.NONE
                                change.consume()
                                break
                            }

                            // Quick tap to clear selection or pending mark
                            if (dist < 18f && totalTime < 280L) {
                                if (currentIsSelectionActive) {
                                    currentOnClearSelection()
                                    pendingFirstMarkOffset = null
                                    change.consume()
                                    break
                                } else if (pendingFirstMarkOffset != null) {
                                    pendingFirstMarkOffset = null
                                    change.consume()
                                    break
                                }
                            }

                            // 1. Two vertical lines recognition ("在第一个字前画一竖线，最后一个字后画一竖线即可选中")
                            // 并且："如果增加或减少选择的字，不用拖动，直接画小竖线就行"
                            val isVerticalStroke = (dist in (10.dp.toPx())..(300.dp.toPx()) && absY >= absX * 0.65f && totalTime in 40L..950L)
                            if (isVerticalStroke && absX < dragThresholdPx && currentLayoutResult != null && currentRawContent.isNotEmpty()) {
                                val currentLr = currentLayoutResult!!
                                val midPoint = Offset((startX + endPos.x) / 2f, (startY + endPos.y) / 2f)
                                processVerticalStrokeSelection(
                                    lr = currentLr,
                                    rawContent = currentRawContent,
                                    midPoint = midPoint,
                                    isSelectionActive = currentIsSelectionActive,
                                    curRange = currentSelectedRange,
                                    pendingFirstMark = pendingFirstMarkOffset,
                                    pendingFirstTime = pendingFirstMarkTime,
                                    onSelect = { text, range ->
                                        pendingFirstMarkOffset = null
                                        currentOnTextSelected(text, range)
                                    },
                                    onSetPendingMark = { mark ->
                                        pendingFirstMarkOffset = mark
                                        pendingFirstMarkTime = System.currentTimeMillis()
                                    },
                                    onHaptic = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                                change.consume()
                                break
                            }

                            // Long press selection (>350ms, low displacement)
                            if (totalTime >= 350L && dist < 20f) {
                                val currentLr = currentLayoutResult
                                if (currentLr != null && currentRawContent.isNotBlank()) {
                                    val off = currentLr.getOffsetForPosition(down.position).coerceIn(0, currentRawContent.length - 1)
                                    val sentenceRange = findSentenceBounds(currentRawContent, off)
                                    val selected = currentRawContent.substring(
                                        sentenceRange.first,
                                        (sentenceRange.last + 1).coerceAtMost(currentRawContent.length)
                                    ).trim()
                                    if (selected.isNotBlank()) {
                                        pendingFirstMarkOffset = null
                                        currentOnTextSelected(selected, sentenceRange)
                                        change.consume()
                                        break
                                    }
                                }
                            }

                            if (absX > dragThresholdPx && absX > absY) {
                                if (curlAnim.value > 0.10f) {
                                    coroutineScope.launch {
                                        curlAnim.animateTo(1f, animationSpec = tween(140, easing = FastOutSlowInEasing))
                                        if (isTurningForward) currentOnRightTap() else currentOnLeftTap()
                                        curlAnim.snapTo(0f)
                                    }
                                } else {
                                    coroutineScope.launch {
                                        curlAnim.animateTo(0f, animationSpec = tween(100, easing = FastOutSlowInEasing))
                                    }
                                }
                            } else {
                                // Tap: 100% instant and responsive click
                                when {
                                    startX < width * 0.35f -> {
                                        isTurningForward = false
                                        coroutineScope.launch {
                                            curlAnim.snapTo(0f)
                                            curlAnim.animateTo(1f, animationSpec = tween(140, easing = FastOutSlowInEasing))
                                            currentOnLeftTap()
                                            curlAnim.snapTo(0f)
                                        }
                                    }
                                    startX > width * 0.65f -> {
                                        isTurningForward = true
                                        coroutineScope.launch {
                                            curlAnim.snapTo(0f)
                                            curlAnim.animateTo(1f, animationSpec = tween(140, easing = FastOutSlowInEasing))
                                            currentOnRightTap()
                                            curlAnim.snapTo(0f)
                                        }
                                    }
                                    else -> currentOnCenterTap()
                                }
                            }
                            change.consume()
                            break
                        } else {
                            // Pointer is active and moving (实时极速拖动手柄调整选区)
                            if (activeDragHandle != SelectionHandleType.NONE) {
                                dragTouchPos = change.position
                                val currentLr = currentLayoutResult
                                if (currentLr != null && currentRawContent.isNotEmpty()) {
                                    val curRange = currentSelectedRange ?: (0..0)

                                    when (activeDragHandle) {
                                        SelectionHandleType.START -> {
                                            val newStart = getTargetOffsetForHandleDrag(
                                                currentLr, currentRawContent, change.position, SelectionHandleType.START, curRange
                                            )
                                            if (newStart != curRange.first) {
                                                val newRange = newStart..curRange.last
                                                val text = currentRawContent.substring(
                                                    newRange.first,
                                                    (newRange.last + 1).coerceAtMost(currentRawContent.length)
                                                ).trim()
                                                if (text.isNotBlank()) {
                                                    currentOnTextSelected(text, newRange)
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                            }
                                        }
                                        SelectionHandleType.END -> {
                                            val newEnd = getTargetOffsetForHandleDrag(
                                                currentLr, currentRawContent, change.position, SelectionHandleType.END, curRange
                                            )
                                            if (newEnd != curRange.last) {
                                                val newRange = curRange.first..newEnd
                                                val text = currentRawContent.substring(
                                                    newRange.first,
                                                    (newRange.last + 1).coerceAtMost(currentRawContent.length)
                                                ).trim()
                                                if (text.isNotBlank()) {
                                                    currentOnTextSelected(text, newRange)
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                            }
                                        }
                                        SelectionHandleType.PENDING_FIRST -> {
                                            val newMark = getTargetOffsetForHandleDrag(
                                                currentLr, currentRawContent, change.position, SelectionHandleType.PENDING_FIRST, curRange
                                            )
                                            if (newMark != pendingFirstMarkOffset) {
                                                pendingFirstMarkOffset = newMark
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                                change.consume()
                            } else {
                                totalDragX = change.position.x - startX
                                totalDragY = change.position.y - startY
                                val absX = kotlin.math.abs(totalDragX)
                                val absY = kotlin.math.abs(totalDragY)
                                stroke.add(change.position)
                                if (stroke.size > 2) {
                                    activeStrokePoints = stroke.toList()
                                }
                                if (absX > 16f && absX > absY) {
                                    if (totalDragX < 0) {
                                        isTurningForward = true
                                        coroutineScope.launch {
                                            curlAnim.snapTo((-totalDragX / 280f).coerceIn(0f, 1f))
                                        }
                                    } else {
                                        isTurningForward = false
                                        coroutineScope.launch {
                                            curlAnim.snapTo((totalDragX / 280f).coerceIn(0f, 1f))
                                        }
                                    }
                                    change.consume()
                                }
                            }
                        }
                    }
                }
            }
    ) {
        val curlProgress = curlAnim.value
        val isDark = theme.isDark
        val lineH = if (uiState.effectiveLineHeightSp > 0f) uiState.effectiveLineHeightSp.sp else (uiState.settings.fontSize * uiState.settings.lineSpacingMultiplier).sp
        val simReaderTextStyle = LocalTextStyle.current.copy(
            fontSize = uiState.settings.fontSize.sp,
            color = theme.textColor,
            fontFamily = uiState.settings.fontType.fontFamily,
            fontWeight = if (uiState.settings.isBold) FontWeight.Bold else FontWeight.Normal,
            lineHeight = lineH,
            letterSpacing = 0.5.sp,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Proportional,
                trim = LineHeightStyle.Trim.Both
            ),
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            )
        )

        // 1. Underneath Layer (Revealed page as top page curls away)
        if (curlProgress > 0.001f) {
            val targetUnderneathIdx = if (isTurningForward) uiState.currentPageIndex + 1 else uiState.currentPageIndex - 1
            if (isTurningForward && nextPageText != null) {
                val nextAnnotated = remember(nextPageText, uiState.highlightKeyword, theme, uiState.settings.fontSize) {
                    buildHighlightedAnnotatedString(nextPageText, uiState.highlightKeyword, theme.textColor, isDark, uiState.settings.fontSize.toFloat())
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    ReaderPageContentRenderer(
                        rawText = nextPageText,
                        annotatedText = nextAnnotated,
                        style = simReaderTextStyle,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Soft shadow falling from curled page
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                val foldX = size.width * (1f - curlProgress)
                                val shadowW = 85.dp.toPx()
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            if (isDark) Color(0x77000000) else Color(0x44000000),
                                            Color.Transparent
                                        ),
                                        startX = (foldX - shadowW * 0.75f).coerceAtLeast(0f),
                                        endX = foldX + shadowW * 0.35f
                                    )
                                )
                            }
                    )
                }
            } else if (!isTurningForward && prevPageText != null) {
                val prevAnnotated = remember(prevPageText, uiState.highlightKeyword, theme, uiState.settings.fontSize) {
                    buildHighlightedAnnotatedString(prevPageText, uiState.highlightKeyword, theme.textColor, isDark, uiState.settings.fontSize.toFloat())
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    ReaderPageContentRenderer(
                        rawText = prevPageText,
                        annotatedText = prevAnnotated,
                        style = simReaderTextStyle,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // 2. Main Page with 3D Page Curl Dynamics
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (curlProgress > 0f) {
                        val angle = if (isTurningForward) -curlProgress * 62f else (1f - curlProgress) * 62f
                        rotationY = angle
                        cameraDistance = 22f * density
                        transformOrigin = if (isTurningForward) {
                            androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                        } else {
                            androidx.compose.ui.graphics.TransformOrigin(1f, 0.5f)
                        }
                        alpha = (1f - curlProgress * 0.18f).coerceIn(0f, 1f)
                    }
                }
        ) {
            ReaderPageContentRenderer(
                rawText = rawContent,
                annotatedText = annotatedContent,
                style = simReaderTextStyle,
                onTextLayout = { simLayoutResult = it },
                modifier = Modifier.fillMaxSize()
            )

            // Apple-style intelligent selection layer
            AppleTextSelectionLayer(
                rawContent = rawContent,
                layoutResult = simLayoutResult,
                isSelectionActive = isSelectionActive,
                selectedRange = selectedRange,
                pendingFirstMarkOffset = pendingFirstMarkOffset,
                activeDragHandle = activeDragHandle,
                dragTouchPos = dragTouchPos,
                activeStrokePoints = activeStrokePoints,
                onClearPendingFirstMark = { pendingFirstMarkOffset = null }
            )
        }

        // 3. Crease, Curl Spine Gradient & Specular Highlight
        if (curlProgress > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val foldX = if (isTurningForward) size.width * (1f - curlProgress) else size.width * curlProgress
                        val curlWidth = 50.dp.toPx()

                        // Specular Light
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    if (isDark) Color(0x30FFFFFF) else Color(0x50FFFFFF),
                                    if (isDark) Color(0x48000000) else Color(0x30000000),
                                    Color.Transparent
                                ),
                                startX = foldX - curlWidth * 0.5f,
                                endX = foldX + curlWidth * 0.5f
                            )
                        )
                    }
            )
        }
    }
}

@Composable
private fun BookCoverInfoHeader(
    book: com.example.data.entity.BookEntity?,
    theme: ReaderThemeType
) {
    if (book == null) return

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (theme.isDark) Color(0x22FFFFFF) else Color(0x18000000),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book Icon Emblem
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = OrangePrimary,
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = "封面",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "作者：${book.author} · 格式：${book.format}",
                    fontSize = 12.sp,
                    color = theme.secondaryTextColor
                )
                if (book.intro.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = book.intro,
                        fontSize = 11.sp,
                        color = theme.secondaryTextColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderTopBar(
    bookTitle: String,
    chapterTitle: String?,
    isBookmarked: Boolean,
    isDark: Boolean,
    onBack: () -> Unit,
    onOpenToc: () -> Unit,
    onOpenSearch: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    // 极简精致阅读顶栏配色，与书籍封面及米白色主题高度协调
    val barBg = if (isDark) Color(0xF81A1A1C) else Color(0xF8F0ECE4)
    val contentColor = if (isDark) Color(0xFFF2EFEA) else Color(0xFF2C2A28)
    val secondaryColor = if (isDark) Color(0xAAFFFFFF) else Color(0xFF756F66)
    val dividerColor = if (isDark) Color(0x22FFFFFF) else Color(0x18000000)
    val activeBookmarkColor = if (isDark) Color(0xFFE2C48D) else Color(0xFF9E6B28)

    Surface(
        color = barBg,
        shadowElevation = 0.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = bookTitle,
                            color = contentColor,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = StitchTokens.AppFontFamily
                        )
                        Text(
                            text = chapterTitle?.ifBlank { null } ?: "正文",
                            color = secondaryColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = StitchTokens.AppFontFamily
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("reader_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回书架",
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    // In-Book Search
                    IconButton(
                        onClick = onOpenSearch,
                        modifier = Modifier.testTag("in_book_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "书内搜索",
                            tint = contentColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Bookmark
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.testTag("toggle_bookmark_button")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "书签",
                            tint = if (isBookmarked) activeBookmarkColor else contentColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // TOC
                    IconButton(
                        onClick = onOpenToc,
                        modifier = Modifier.testTag("toc_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = "目录",
                            tint = contentColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
        }
    }
}

// 极简精致阅读底栏（与封面米白协调，极细进度线条，保留全部5大功能按钮）
@Composable
private fun ReaderBottomBar(
    uiState: ReaderUiState,
    onPrevPage: () -> Unit = {},
    onNextPage: () -> Unit = {},
    onProgressChange: (Float) -> Unit,
    onToggleNight: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenToc: () -> Unit,
    onOpenAmbiance: () -> Unit,
    onOpenNote: () -> Unit = {}
) {
    val currentChapterTitle = uiState.currentChapter?.title ?: "正文"
    val isDark = uiState.settings.theme == ReaderThemeType.NIGHT
    val barBg = if (isDark) Color(0xF81A1A1C) else Color(0xF8F0ECE4)
    val textMain = if (isDark) Color(0xFFF2EFEA) else Color(0xFF2C2A28)
    val textSub = if (isDark) Color(0xAAFFFFFF) else Color(0xFF756F66)
    val iconTint = if (isDark) Color(0xFFF2EFEA) else Color(0xFF332F2A)
    val dividerColor = if (isDark) Color(0x22FFFFFF) else Color(0x18000000)

    Surface(
        color = barBg,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(thickness = 0.5.dp, color = dividerColor)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 8.dp)
            ) {
                // Chapter & Quick Flip Controls & Progress Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Previous Page button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onPrevPage() }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "上一页",
                            tint = iconTint,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "上一页",
                            fontSize = 11.5.sp,
                            color = textMain,
                            fontWeight = FontWeight.Medium,
                            fontFamily = StitchTokens.AppFontFamily
                        )
                    }

                    Text(
                        text = "${uiState.currentPageInChapter}/${uiState.totalPagesInChapter}页 · ${(uiState.chapterProgress * 100).toInt()}%",
                        fontSize = 11.5.sp,
                        color = textSub,
                        fontWeight = FontWeight.Normal,
                        fontFamily = StitchTokens.AppFontFamily
                    )

                    // Quick Next Page button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onNextPage() }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "下一页",
                            fontSize = 11.5.sp,
                            color = textMain,
                            fontWeight = FontWeight.Medium,
                            fontFamily = StitchTokens.AppFontFamily
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "下一页",
                            tint = iconTint,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 极细进度条（Apple Books / 微信读书极简一根细线风格，支持精准滑动与点击拖曳）
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                onProgressChange(fraction)
                            }
                        }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, _ ->
                                val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                onProgressChange(fraction)
                            }
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    val progressFraction = uiState.chapterProgress.coerceIn(0f, 1f)
                    val activeTrackWidth = maxWidth * progressFraction

                    // Inactive Hairline Background Track (极细 1.5dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.5.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x33FFFFFF) else Color(0x22000000))
                    )

                    // Active Hairline Progress Track (极细 1.5dp)
                    Box(
                        modifier = Modifier
                            .width(activeTrackWidth)
                            .height(1.5.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFFE2DFD8) else Color(0xFF382E25))
                    )

                    // Delicate Minimalist Circular Thumb
                    Box(
                        modifier = Modifier
                            .padding(start = (activeTrackWidth - 3.5.dp).coerceAtLeast(0.dp))
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFFFBF8F4) else Color(0xFF382E25))
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Quick Buttons Row (目录 | 笔记 | 氛围 | 夜间/日间 | 设置 - 严格保留五大功能按钮)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. 目录
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenToc() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = "目录",
                            tint = iconTint,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "目录", fontSize = 12.sp, color = textMain, fontWeight = FontWeight.Medium)
                    }

                    // 2. 笔记
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenNote() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notes,
                            contentDescription = "笔记",
                            tint = iconTint,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "笔记", fontSize = 12.sp, color = textMain, fontWeight = FontWeight.Medium)
                    }

                    // 3. 氛围
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenAmbiance() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Window,
                            contentDescription = "氛围",
                            tint = iconTint,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "氛围", fontSize = 12.sp, color = textMain, fontWeight = FontWeight.Medium)
                    }

                    // 4. 夜间/日间
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleNight() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.settings.theme == ReaderThemeType.NIGHT) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "夜间模式",
                            tint = iconTint,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.settings.theme == ReaderThemeType.NIGHT) "日间" else "夜间",
                            fontSize = 12.sp,
                            color = textMain,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 5. 设置
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenSettings() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "设置",
                            tint = iconTint,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "设置", fontSize = 12.sp, color = textMain, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

// TOC Modal Drawer with 3 Tabs: 目录 | 书签 | 笔记
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TocModalDrawer(
    uiState: ReaderUiState,
    onDismiss: () -> Unit,
    onSelectChapter: (Int) -> Unit,
    onSelectBookmark: (BookmarkEntity) -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    onDeleteNote: (Long) -> Unit
) {
    val theme = uiState.settings.theme
    val isDark = theme.isDark
    val drawerBg = theme.backgroundColor
    val textMain = theme.textColor // 目录文字及读数严格与正文文字颜色完全一致
    val textSub = textMain // 目录文字及读数均采用正文统一色彩，告别浅色看不清
    val activeBg = textMain.copy(alpha = 0.12f)
    val inactiveBg = if (isDark) Color(0x18FFFFFF) else Color(0x08000000)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("目录 (${uiState.chapters.size})", "书签 (${uiState.bookmarks.size})", "笔记 (${uiState.notes.size})")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = drawerBg,
        modifier = Modifier.fillMaxHeight(0.96f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = drawerBg,
                contentColor = textMain,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = textMain,
                        height = 2.5.dp
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.5.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) textMain else textSub,
                                fontFamily = StitchTokens.AppFontFamily
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    var chapterQuery by remember { mutableStateOf("") }
                    val filteredChapters = remember(uiState.chapters, chapterQuery) {
                        if (chapterQuery.isBlank()) {
                            uiState.chapters
                        } else {
                            uiState.chapters.filter { it.title.contains(chapterQuery, ignoreCase = true) }
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Directory Search Field
                        OutlinedTextField(
                            value = chapterQuery,
                            onValueChange = { chapterQuery = it },
                            placeholder = { Text("搜索章节目录...", fontSize = 13.sp, color = textSub) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = textMain,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (chapterQuery.isNotBlank()) {
                                    IconButton(onClick = { chapterQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "清空",
                                            tint = textMain,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain,
                                focusedBorderColor = textMain,
                                unfocusedBorderColor = textMain.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        // Filtered Chapters Count
                        if (chapterQuery.isNotBlank()) {
                            Text(
                                text = "检索到 ${filteredChapters.size} / ${uiState.chapters.size} 章",
                                fontSize = 11.5.sp,
                                color = textMain,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                            )
                        }

                        // Chapters List
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(filteredChapters) { _, chapter ->
                                val realIndex = uiState.chapters.indexOf(chapter)
                                val isCurrent = realIndex == uiState.currentChapterIndex
                                val cleanTitle = com.example.util.normalizeChapterTitle(chapter.title, realIndex + 1)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCurrent) activeBg else inactiveBg,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectChapter(realIndex) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "${realIndex + 1}.",
                                                fontSize = 13.sp,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                color = textMain,
                                                fontFamily = StitchTokens.AppFontFamily
                                            )
                                            Text(
                                                text = cleanTitle,
                                                fontSize = 14.sp,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                color = textMain,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontFamily = StitchTokens.AppFontFamily
                                            )
                                        }
                                        if (isCurrent) {
                                            Text(
                                                text = "当前阅读 (${uiState.currentPageInChapter}/${uiState.totalPagesInChapter}页)",
                                                color = textMain,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = StitchTokens.AppFontFamily
                                            )
                                        } else if (chapter.wordCount > 0) {
                                            Text(
                                                text = "${chapter.wordCount}字",
                                                color = textMain,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = StitchTokens.AppFontFamily
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Bookmarks List
                    if (uiState.bookmarks.isEmpty()) {
                        EmptyTabContent(message = "暂无书签记录")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.bookmarks, key = { it.id }) { bookmark ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = inactiveBg,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectBookmark(bookmark) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = null,
                                            tint = textMain,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            val bChapterTitle = com.example.util.normalizeChapterTitle(bookmark.chapterTitle, 1)
                                            Text(
                                                text = bChapterTitle,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                color = textMain,
                                                fontFamily = StitchTokens.AppFontFamily
                                            )
                                            Text(
                                                text = bookmark.excerpt,
                                                fontSize = 12.sp,
                                                color = textSub,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                fontFamily = StitchTokens.AppFontFamily
                                            )
                                        }
                                        IconButton(onClick = { onDeleteBookmark(bookmark.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "删除书签",
                                                tint = textSub,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Reading Notes List
                    if (uiState.notes.isEmpty()) {
                        EmptyTabContent(message = "暂无阅读笔记与名句摘抄")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.notes, key = { it.id }) { note ->
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = inactiveBg),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val nChapterTitle = com.example.util.normalizeChapterTitle(note.chapterTitle, 1)
                                            Text(
                                                text = nChapterTitle,
                                                fontSize = 12.5.sp,
                                                color = textMain,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = StitchTokens.AppFontFamily
                                            )
                                            IconButton(
                                                onClick = { onDeleteNote(note.id) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "删除",
                                                    tint = textSub,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "“${note.quoteText}”",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textMain,
                                            fontFamily = StitchTokens.AppFontFamily
                                        )
                                        if (note.noteContent.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "心得：${note.noteContent}",
                                                fontSize = 12.sp,
                                                color = textSub,
                                                fontFamily = StitchTokens.AppFontFamily
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTabContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderSettingsBottomSheet(
    settings: ReaderSettings,
    onDismiss: () -> Unit,
    onThemeSelect: (ReaderThemeType) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onLineSpacingChange: (Float) -> Unit,
    onPageModeChange: (PageTurnMode) -> Unit,
    onToggleShowPageIndicatorAlways: (Boolean) -> Unit = {},
    onFontTypeChange: (com.example.ui.theme.ReaderFontType) -> Unit = {},
    onToggleBold: () -> Unit = {},
    onEncodingChange: (String) -> Unit = {}
) {
    val isDark = StitchTokens.IsDark
    val sheetBg = if (isDark) Color(0xFF1E1E22) else Color(0xFFFAF7F2)
    val textMain = if (isDark) Color(0xFFF2EFEA) else Color(0xFF2C2A28)
    val textSub = if (isDark) Color(0xAAFFFFFF) else Color(0xFF756F66)
    val btnNormalBg = if (isDark) Color(0xFF2C2C32) else Color(0xFFECE7DE)
    val btnNormalText = if (isDark) Color.White else Color(0xFF2C2A28)
    val btnSelectedBg = if (isDark) Color(0xFFE2DFD8) else Color(0xFF382E25)
    val btnSelectedText = if (isDark) Color(0xFF141414) else Color(0xFFFAF7F2)

    var showEncodingDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "阅读偏好与排版",
                    color = textMain,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = StitchTokens.AppFontFamily
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0x33FFFFFF) else Color(0x18000000),
                    modifier = Modifier.clickable { showEncodingDialog = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "文本编码: ${settings.encoding}",
                            fontSize = 11.5.sp,
                            color = textMain,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "修改编码",
                            tint = textSub,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. 精选自然纸张（宣纸水墨、掌阅羊皮、苹果纸质、竹青护眼、电子水墨、暗夜极简）
            Text(text = "精选护眼纸张", color = textSub, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ReaderThemeType.values().forEach { themeType ->
                    val isSelected = settings.theme == themeType
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onThemeSelect(themeType) }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = themeType.backgroundColor,
                            shadowElevation = if (isSelected) 3.dp else 1.dp,
                            border = if (isSelected) BorderStroke(2.dp, btnSelectedBg) else BorderStroke(0.8.dp, Color(0x22000000)),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        ) {
                            if (isSelected) {
                                Box(contentAlignment = Alignment.Center) {
                                    Surface(
                                        shape = CircleShape,
                                        color = btnSelectedBg,
                                        modifier = Modifier.size(10.dp)
                                    ) {}
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = themeType.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) btnSelectedBg else textSub,
                            fontFamily = StitchTokens.AppFontFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. 自定义精选字体与加粗
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "精选字体样式", color = textSub, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                // 字体加粗开关
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleBold() }
                        .background(if (settings.isBold) btnSelectedBg else btnNormalBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "B",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (settings.isBold) btnSelectedText else btnNormalText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (settings.isBold) "已加粗" else "字体加粗",
                        fontSize = 11.5.sp,
                        fontWeight = if (settings.isBold) FontWeight.Bold else FontWeight.Normal,
                        color = if (settings.isBold) btnSelectedText else btnNormalText
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val fontRows = com.example.ui.theme.ReaderFontType.values().toList().chunked(3)
                for (rowFonts in fontRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (fType in rowFonts) {
                            val isSelected = settings.fontType == fType
                            Button(
                                onClick = { onFontTypeChange(fType) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) btnSelectedBg else btnNormalBg
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 7.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = fType.displayName,
                                    color = if (isSelected) btnSelectedText else btnNormalText,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontFamily = fType.fontFamily,
                                    maxLines = 1
                                )
                            }
                        }
                        if (rowFonts.size < 3) {
                            repeat(3 - rowFonts.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Font Size
            Text(text = "字号大小 (${settings.fontSize} sp)", color = textSub, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onFontSizeChange(settings.fontSize - 2) },
                    enabled = settings.fontSize > 14,
                    colors = ButtonDefaults.buttonColors(containerColor = btnNormalBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "A - 缩小", color = btnNormalText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = { onFontSizeChange(settings.fontSize + 2) },
                    enabled = settings.fontSize < 30,
                    colors = ButtonDefaults.buttonColors(containerColor = btnNormalBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "A + 放大", color = btnNormalText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Line Spacing
            Text(text = "行间距", color = textSub, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("紧凑", 1.3f),
                    Pair("标准", 1.6f),
                    Pair("宽松", 2.0f)
                ).forEach { (label, multiplier) ->
                    val isSelected = (settings.lineSpacingMultiplier - multiplier).let { it > -0.05f && it < 0.05f }
                    Button(
                        onClick = { onLineSpacingChange(multiplier) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) btnSelectedBg else btnNormalBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) btnSelectedText else btnNormalText,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Page Turn Mode
            Text(text = "翻页模式", color = textSub, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PageTurnMode.values().forEach { mode ->
                    val isSelected = settings.pageTurnMode == mode
                    Button(
                        onClick = { onPageModeChange(mode) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) btnSelectedBg else btnNormalBg
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = mode.displayName,
                            color = if (isSelected) btnSelectedText else btnNormalText,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Always Show Page Indicator in Immersive Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "沉浸模式常驻页码", color = textMain, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = "全屏隐藏工具栏时依然在底部保留简明页码",
                        color = textSub,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = settings.showPageIndicatorAlways,
                    onCheckedChange = { onToggleShowPageIndicatorAlways(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = if (isDark) Color.White else Color(0xFFFAF7F2),
                        checkedTrackColor = btnSelectedBg,
                        uncheckedThumbColor = if (isDark) Color(0xFFAAAAAA) else Color(0xFF756F66),
                        uncheckedTrackColor = btnNormalBg
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Manual Encoding Selection Dialog
    if (showEncodingDialog) {
        AlertDialog(
            onDismissRequest = { showEncodingDialog = false },
            title = {
                Text(
                    text = "更改文本字符编码",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMain
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "当前排名前三读书软件（Apple Books、微信读书、静读天下）标准支持：若文本出现生僻字乱码，可手动切换编码即刻重新解析。",
                        fontSize = 12.sp,
                        color = textSub,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    com.example.util.CharsetDetector.SUPPORTED_ENCODINGS.forEach { enc ->
                        val isSelected = settings.encoding.equals(enc.code, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) btnSelectedBg else btnNormalBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onEncodingChange(enc.code)
                                    showEncodingDialog = false
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = enc.label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) btnSelectedText else textMain
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = "使用中",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color(0xFFA5D6A7) else Color(0xFF2E7D32)
                                        )
                                    }
                                }
                                Text(
                                    text = enc.description,
                                    fontSize = 11.sp,
                                    color = if (isSelected) btnSelectedText.copy(alpha = 0.8f) else textSub
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEncodingDialog = false }) {
                    Text(text = "关闭", color = textMain, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = sheetBg
        )
    }
}

// Top In-Book Search Bar (Dynamic palette matching book theme and typography)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderTopSearchBar(
    uiState: ReaderUiState,
    onClose: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSelectResult: (SearchResultSnippet, String) -> Unit
) {
    val theme = uiState.settings.theme
    var queryText by remember { mutableStateOf(uiState.inBookSearchQuery) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        color = if (theme.isDark) Color(0xF816181B) else Color(0xFAF7F2EA),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
        ) {
            // Search Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "关闭搜索",
                        tint = theme.textColor
                    )
                }

                OutlinedTextField(
                    value = queryText,
                    onValueChange = {
                        queryText = it
                        onQueryChange(it)
                    },
                    placeholder = {
                        Text(
                            text = "在《${uiState.book?.title ?: "本书"}》内检索正文...",
                            color = theme.secondaryTextColor.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = theme.textColor,
                        fontSize = 14.sp
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = theme.textColor.copy(alpha = 0.75f),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (queryText.isNotBlank()) {
                            IconButton(onClick = {
                                queryText = ""
                                onQueryChange("")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "清空",
                                    tint = theme.secondaryTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.textColor,
                        unfocusedBorderColor = theme.textColor.copy(alpha = 0.35f),
                        focusedContainerColor = if (theme.isDark) Color(0x22FFFFFF) else Color(0x0C000000),
                        unfocusedContainerColor = if (theme.isDark) Color(0x14FFFFFF) else Color(0x06000000),
                        cursorColor = theme.textColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                )
            }

            // Recent Search History when input is blank (Keeps last 2 search records)
            if (queryText.isBlank() && uiState.searchHistory.isNotEmpty()) {
                Surface(
                    color = if (theme.isDark) Color(0xFF1E2127) else Color(0xFFEDE5D6),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "最近搜索",
                            fontSize = 12.sp,
                            color = theme.secondaryTextColor,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            uiState.searchHistory.take(2).forEach { historyKeyword ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (theme.isDark) Color(0xFF282C35) else Color(0xFFE2D9C8),
                                    border = BorderStroke(0.6.dp, theme.textColor.copy(alpha = 0.25f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            queryText = historyKeyword
                                            onQueryChange(historyKeyword)
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = theme.secondaryTextColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = historyKeyword,
                                            fontSize = (uiState.settings.fontSize - 3).coerceAtLeast(13).sp,
                                            color = theme.textColor,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Results List Dropdown Container (Right below the top search bar)
            if (queryText.isNotBlank()) {
                Surface(
                    color = if (theme.isDark) Color(0xFF1E2127) else Color(0xFFEDE5D6),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        if (uiState.inBookSearchResults.isNotEmpty()) {
                            Text(
                                text = "共找到 ${uiState.inBookSearchResults.size} 处匹配内容 (点击直达正文)",
                                fontSize = 12.sp,
                                color = if (theme.isDark) Color(0xFFA5D6A7) else Color(0xFF2E7D32),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(uiState.inBookSearchResults) { snippet ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (theme.isDark) Color(0xFF282C35) else Color(0xFFE2D9C8),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSelectResult(snippet, queryText) }
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = snippet.chapterTitle,
                                                    fontSize = (uiState.settings.fontSize).coerceAtLeast(15).sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = theme.textColor,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                Text(
                                                    text = "第 ${snippet.matchIndexInChapter + 1} 处",
                                                    fontSize = 11.sp,
                                                    color = theme.secondaryTextColor
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            HighlightSnippetText(
                                                fullText = snippet.snippetText,
                                                query = queryText,
                                                isDark = theme.isDark,
                                                fontSize = (uiState.settings.fontSize - 2).coerceAtLeast(14).sp,
                                                fontWeight = FontWeight.Normal,
                                                defaultColor = theme.textColor,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "未在本书中找到匹配项",
                                fontSize = 12.sp,
                                color = theme.secondaryTextColor,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HighlightSnippetText(
    fullText: String,
    query: String,
    isDark: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight = FontWeight.Normal,
    defaultColor: Color = TextPrimary,
    maxLines: Int = Int.MAX_VALUE
) {
    val annotatedString = remember(fullText, query, isDark) {
        buildAnnotatedString {
            if (query.isBlank()) {
                append(fullText)
                return@buildAnnotatedString
            }
            var currentIndex = 0
            val lowerText = fullText.lowercase()
            val lowerQuery = query.lowercase()

            while (currentIndex < fullText.length) {
                val matchIndex = lowerText.indexOf(lowerQuery, currentIndex)
                if (matchIndex < 0) {
                    append(fullText.substring(currentIndex))
                    break
                }
                if (matchIndex > currentIndex) {
                    append(fullText.substring(currentIndex, matchIndex))
                }
                withStyle(
                    style = SpanStyle(
                        color = if (isDark) Color(0xFFA5D6A7) else Color(0xFF1B5E20),
                        background = if (isDark) Color(0x662E7D32) else Color(0x444CAF50),
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(fullText.substring(matchIndex, matchIndex + query.length))
                }
                currentIndex = matchIndex + query.length
            }
        }
    }

    Text(
        text = annotatedString,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = defaultColor,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

// Add Note Dialog
@Composable
private fun AddNoteDialog(
    quoteText: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var noteInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StitchTokens.SurfaceContainer,
        titleContentColor = StitchTokens.Primary,
        textContentColor = StitchTokens.OnSurface,
        title = {
            Text(
                text = "添加阅读摘抄与心得",
                fontWeight = FontWeight.Bold,
                fontFamily = StitchTokens.AppFontFamily,
                fontSize = 16.5.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (quoteText.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StitchTokens.SurfaceContainerHigh,
                        border = BorderStroke(0.6.dp, StitchTokens.Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "“$quoteText”",
                            fontSize = 13.sp,
                            fontFamily = StitchTokens.AppFontFamily,
                            color = StitchTokens.Primary,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    placeholder = {
                        Text(
                            text = "写下你的感悟与想法...",
                            color = StitchTokens.Outline,
                            fontFamily = StitchTokens.AppFontFamily
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StitchTokens.Primary,
                        unfocusedBorderColor = StitchTokens.Border,
                        focusedTextColor = StitchTokens.OnSurface,
                        unfocusedTextColor = StitchTokens.OnSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(noteInput, "#FFFF5A00") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StitchTokens.Primary,
                    contentColor = StitchTokens.OnPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "保存笔记",
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = StitchTokens.AppFontFamily
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "取消",
                    color = StitchTokens.Outline,
                    fontFamily = StitchTokens.AppFontFamily
                )
            }
        }
    )
}
