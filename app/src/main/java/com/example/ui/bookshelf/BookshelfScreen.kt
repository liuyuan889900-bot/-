package com.example.ui.bookshelf

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.ui.viewmodel.BookshelfSortMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.BookEntity
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.filled.ViewColumn
import com.example.ui.ambiance.AmbiancePickerSheet
import com.example.ui.components.drawWonderlandRealisticFoliage
import com.example.ui.components.drawOasisRealisticBackground
import com.example.ui.components.drawOasisForegroundLeavesAndMangoes
import com.example.ui.viewmodel.BookshelfViewMode
import com.example.ui.components.BookCover
import com.example.ui.components.drawBotanicalFoliage
import com.example.ui.theme.AppThemeManager
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.StitchTokens
import com.example.ui.viewmodel.BookshelfViewModel
import com.example.util.BookParser
import com.example.util.cleanBookTitle

enum class BookshelfCategoryTab(val stringKey: String) {
    ALL("category_all"),
    RECENT("category_default"),
    SONGBOOK("category_hymn")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfScreen(
    viewModel: BookshelfViewModel,
    onBookClick: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSearchWithQuery: (String) -> Unit = { onNavigateToSearch() },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 批量多选文件导入 (TXT, EPUB, DOCX)
    val multiFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            viewModel.importMultipleUris(uris)
        }
    }

    // 整个文件夹扫描导入
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        treeUri?.let {
            viewModel.importFolderTree(it)
        }
    }

    // 单个文件导入兼容器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val fileName = BookParser.getFileName(context, it)
            viewModel.importFileUri(it, fileName)
            Toast.makeText(context, "正在导入: $fileName", Toast.LENGTH_SHORT).show()
        }
    }

    var selectedTab by remember { mutableStateOf(BookshelfCategoryTab.ALL) }
    var isPureListView by rememberSaveable { mutableStateOf(false) }
    var searchInputText by remember { mutableStateOf("") }
    var bookToManage by remember { mutableStateOf<BookEntity?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAmbianceSheet by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showManualReorderDialog by remember { mutableStateOf(false) }

    // Filter books based on category capsule tab
    // Rule: "歌本" category automatically includes any book with "歌" in title
    val displayedBooks = remember(selectedTab, uiState.books) {
        when (selectedTab) {
            BookshelfCategoryTab.ALL -> uiState.books
            BookshelfCategoryTab.RECENT -> {
                uiState.books.filter { it.lastReadTime > 0 }
                    .sortedByDescending { it.lastReadTime }
                    .ifEmpty { uiState.books.take(3) }
            }
            BookshelfCategoryTab.SONGBOOK -> {
                uiState.books.filter { 
                    it.title.contains("歌") ||
                    it.category == "歌本" || 
                    it.category.contains("歌") 
                }
            }
        }
    }

    val showDotGrid = StitchTokens.ShowDotGridBackground
    val dotColor = StitchTokens.DotColor
    val showBotanical = StitchTokens.ShowBotanicalBackground
    val botanicalColor = StitchTokens.BotanicalColor
    val showWonderland = StitchTokens.ShowWonderlandBackground || StitchTokens.ShowOasisBackground
    val primaryColor = StitchTokens.Primary
    val oasisAccent = StitchTokens.Accent

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StitchTokens.Background)
            .drawBehind {
                if (showWonderland) {
                    drawOasisRealisticBackground(
                        primaryColor = primaryColor,
                        accentColor = oasisAccent
                    )
                } else if (showBotanical && botanicalColor != Color.Transparent) {
                    drawBotanicalFoliage(botanicalColor)
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ================= 1. TOP BAR: "书架" TITLE + SEARCH CAPSULE =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 16.dp, top = 10.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fixed page title "书架" in bold serif
                Text(
                    text = "书架",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = StitchTokens.AppFontFamily,
                    color = StitchTokens.Primary,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Search Input Box (Pill Container) located directly to the right of "书架"
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 38.dp)
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(percent = 50))
                        .clickable { onNavigateToSearch() },
                    color = StitchTokens.SearchBarBackground,
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 38.dp)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "搜索",
                            tint = StitchTokens.SearchBarPlaceholder,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (searchInputText.isEmpty()) com.example.util.appString("search_books_hint") else searchInputText,
                            color = if (searchInputText.isEmpty()) StitchTokens.SearchBarPlaceholder else StitchTokens.OnSurface,
                            fontSize = 12.5.sp,
                            fontFamily = StitchTokens.AppFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Theme Switcher (Pitch Dark / Paper Cream / Forest Sage Green)
                IconButton(
                    onClick = { showThemeDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = "外观主题",
                        tint = StitchTokens.Primary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Ambiance Scene (Window Rain 4S / Rain / Snow / Stars)
                IconButton(
                    onClick = { showAmbianceSheet = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = "场景氛围",
                        tint = StitchTokens.Primary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Import Book Icon Button
                IconButton(
                    onClick = { showImportDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "导入书籍",
                        tint = StitchTokens.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ================= 2. SEGMENTED CONTROLLER (全部 / 最近 / 歌本) + VIEW MODE TOGGLE =================
            // Light capsule trough + dark highlighted selected pill, Serif font
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = StitchTokens.TabTrackBackground,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 32.dp)
                        .wrapContentHeight()
                ) {
                    Row(
                        modifier = Modifier
                            .wrapContentHeight()
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BookshelfCategoryTab.entries.forEach { tab ->
                            val isSelected = tab == selectedTab
                            val pillBg = if (isSelected) StitchTokens.TabSelectedPillBackground else Color.Transparent
                            val textColor = if (isSelected) StitchTokens.TabSelectedTextColor else StitchTokens.TabUnselectedTextColor

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(pillBg)
                                    .clickable { selectedTab = tab }
                                    .defaultMinSize(minHeight = 28.dp)
                                    .wrapContentHeight()
                                    .padding(horizontal = 14.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = com.example.util.appString(tab.stringKey),
                                    color = textColor,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontFamily = StitchTokens.AppFontFamily
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // ================= VIEW MODE & SORT CONTROLS =================
                // 1. 书架排序与排列整理按钮 (用户可以自己排列，可以按名称，也可以手动排列)
                IconButton(
                    onClick = { showSortDialog = true },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("btn_bookshelf_sort")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "书架排序与整理",
                        tint = StitchTokens.Secondary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // 2. 只保留最右边这一个模式切换按钮，点一下就循环切换一个模式 (平铺 -> 列表 -> 书脊)
                IconButton(
                    onClick = { viewModel.cycleViewMode() },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("btn_switch_bookshelf_view_mode")
                ) {
                    Icon(
                        imageVector = when (uiState.viewMode) {
                            BookshelfViewMode.GRID -> Icons.Filled.GridView
                            BookshelfViewMode.LIST -> Icons.AutoMirrored.Filled.ViewList
                            BookshelfViewMode.SPINE -> Icons.Filled.ViewColumn
                        },
                        contentDescription = "循环切换书架模式",
                        tint = StitchTokens.Secondary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ================= 3. BOOK LISTING (平铺模式 / 列表模式 / 书脊模式) =================
            if (displayedBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MenuBook,
                            contentDescription = null,
                            tint = StitchTokens.OutlineVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (selectedTab == BookshelfCategoryTab.SONGBOOK) "暂无歌本书籍" else "书架暂无书籍",
                            color = StitchTokens.Outline,
                            fontFamily = StitchTokens.AppFontFamily,
                            fontSize = 13.5.sp
                        )
                    }
                }
            } else when (uiState.viewMode) {
                BookshelfViewMode.LIST -> {
                    // Windows File Explorer-like Pure Minimalist List Mode
                    // 极简，什么都没有，只有书名。一行一行的每行书名下面有一条极细的进度条要美观好看，有艺术感。
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("bookshelf_pure_list"),
                        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 80.dp)
                    ) {
                        items(displayedBooks, key = { it.id }) { book ->
                            BookPureListItem(
                                book = book,
                                onClick = { onBookClick(book.id) },
                                onLongClick = { bookToManage = book }
                            )
                        }
                    }
                }
                BookshelfViewMode.SPINE -> {
                    // Realistic Bookshelf Spine Mode:
                    // 真实书架书脊外观，高度与平铺封面相同，每行4本书，书名大且全，底端极细进度条，统一典雅书脊色彩，实木书架厚板
                    val shelfChunks = remember(displayedBooks) { displayedBooks.chunked(4) }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("bookshelf_spine_list"),
                        contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        items(shelfChunks, key = { chunk -> chunk.first().id }) { chunk ->
                            BookSpineShelfRow(
                                books = chunk,
                                onBookClick = onBookClick,
                                onBookLongClick = { bookToManage = it }
                            )
                        }
                    }
                }
                BookshelfViewMode.GRID -> {
                    // 默认平铺封面网格模式
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("bookshelf_grid"),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(displayedBooks, key = { it.id }) { book ->
                            BookGridItem(
                                book = book,
                                onClick = { onBookClick(book.id) },
                                onLongClick = { bookToManage = book }
                            )
                        }
                    }
                }
            }
        }

        // ================= OASIS FOREGROUND LAYER (树叶、芒果果实呈现在封面正前方) =================
        if (StitchTokens.ShowOasisBackground) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                drawOasisForegroundLeavesAndMangoes(
                    primaryColor = primaryColor,
                    accentColor = oasisAccent
                )
            }
        }

        // ================= 4. BOOK ACTION MODAL DIALOG =================
        bookToManage?.let { actionBook ->
            AlertDialog(
                onDismissRequest = { bookToManage = null },
                containerColor = StitchTokens.SurfaceContainer,
                titleContentColor = StitchTokens.Primary,
                textContentColor = StitchTokens.OnSurface,
                title = {
                    Text(
                        text = "《${cleanBookTitle(actionBook.title)}》",
                        fontWeight = FontWeight.Bold,
                        fontFamily = StitchTokens.AppFontFamily,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "作者：${actionBook.author.ifBlank { "佚名" }} · 格式：${actionBook.format}",
                            fontSize = 13.sp,
                            fontFamily = StitchTokens.AppFontFamily,
                            color = StitchTokens.Outline
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.toggleFavorite(actionBook.id)
                                    bookToManage = null
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (actionBook.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = null,
                                tint = if (actionBook.isFavorite) Color(0xFFE53935) else StitchTokens.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (actionBook.isFavorite) "取消收藏" else "加入收藏",
                                color = StitchTokens.Primary,
                                fontFamily = StitchTokens.AppFontFamily,
                                fontSize = 14.sp
                            )
                        }

                        // 手动排序快捷操作
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.moveBookToTop(actionBook.id)
                                    bookToManage = null
                                    Toast.makeText(context, "已将《${cleanBookTitle(actionBook.title)}》置顶到书架最前", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerticalAlignTop,
                                contentDescription = null,
                                tint = StitchTokens.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "置顶到书架最前",
                                color = StitchTokens.Primary,
                                fontFamily = StitchTokens.AppFontFamily,
                                fontSize = 14.sp
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.moveBookUp(actionBook.id)
                                    bookToManage = null
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowUpward,
                                contentDescription = null,
                                tint = StitchTokens.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "向前移动一位",
                                color = StitchTokens.Primary,
                                fontFamily = StitchTokens.AppFontFamily,
                                fontSize = 14.sp
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.moveBookDown(actionBook.id)
                                    bookToManage = null
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDownward,
                                contentDescription = null,
                                tint = StitchTokens.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "向后移动一位",
                                color = StitchTokens.Primary,
                                fontFamily = StitchTokens.AppFontFamily,
                                fontSize = 14.sp
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.deleteBook(actionBook.id)
                                    bookToManage = null
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "从书架移除",
                                color = Color(0xFFEF5350),
                                fontFamily = StitchTokens.AppFontFamily,
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { bookToManage = null }) {
                        Text("关闭", color = StitchTokens.Primary, fontFamily = StitchTokens.AppFontFamily)
                    }
                }
            )
        }

        // ================= 5. IMPORT BOOK MODAL DIALOG (批量与文件夹导入) =================
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                containerColor = StitchTokens.SurfaceContainer,
                titleContentColor = StitchTokens.Primary,
                textContentColor = StitchTokens.OnSurface,
                title = {
                    Text("导入书籍", fontWeight = FontWeight.Bold, fontFamily = StitchTokens.AppFontFamily, fontSize = 17.sp)
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "支持从本地导入 TXT / EPUB / DOCX 格式电子书：",
                            fontSize = 13.sp,
                            fontFamily = StitchTokens.AppFontFamily,
                            color = StitchTokens.Outline
                        )

                        // 选项 1：批量选择多本电子书
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    showImportDialog = false
                                    multiFilePickerLauncher.launch(
                                        arrayOf(
                                            "text/plain",
                                            "application/epub+zip",
                                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                            "application/msword",
                                            "application/octet-stream",
                                            "*/*"
                                        )
                                    )
                                },
                            color = StitchTokens.SurfaceContainerLow,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Outlined.LibraryBooks, contentDescription = null, tint = StitchTokens.Primary)
                                Column {
                                    Text("批量选择电子书 (支持多选)", color = StitchTokens.Primary, fontWeight = FontWeight.SemiBold, fontFamily = StitchTokens.AppFontFamily, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("一次勾选一本或多本 TXT / EPUB / DOCX", color = StitchTokens.Outline, fontFamily = StitchTokens.AppFontFamily, fontSize = 11.5.sp)
                                }
                            }
                        }

                        // 选项 2：导入整个文件夹
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    showImportDialog = false
                                    folderPickerLauncher.launch(null)
                                },
                            color = StitchTokens.SurfaceContainerLow,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Outlined.Folder, contentDescription = null, tint = StitchTokens.Primary)
                                Column {
                                    Text("导入整个文件夹", color = StitchTokens.Primary, fontWeight = FontWeight.SemiBold, fontFamily = StitchTokens.AppFontFamily, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("选取文件夹，自动扫描内部所有电子书并导入", color = StitchTokens.Outline, fontFamily = StitchTokens.AppFontFamily, fontSize = 11.5.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("取消", color = StitchTokens.Outline, fontFamily = StitchTokens.AppFontFamily)
                    }
                }
            )
        }

        // ================= 批量导入进度弹窗 =================
        val batchProgress = uiState.batchImportProgress
        if (batchProgress.isImporting || batchProgress.isScanning) {
            AlertDialog(
                onDismissRequest = { /* 正在导入中不可随意取消以免写坏数据库 */ },
                containerColor = StitchTokens.SurfaceContainer,
                titleContentColor = StitchTokens.Primary,
                textContentColor = StitchTokens.OnSurface,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (batchProgress.isScanning) Icons.Outlined.Folder else Icons.Outlined.LibraryBooks,
                            contentDescription = null,
                            tint = StitchTokens.Primary
                        )
                        Text(
                            text = if (batchProgress.isScanning) "正在扫描文件夹..." else "正在批量导入书籍",
                            fontWeight = FontWeight.Bold,
                            fontFamily = StitchTokens.AppFontFamily,
                            fontSize = 17.sp
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = batchProgress.currentBookName,
                            fontSize = 13.5.sp,
                            fontFamily = StitchTokens.AppFontFamily,
                            color = StitchTokens.Primary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!batchProgress.isScanning && batchProgress.total > 0) {
                            val fraction = (batchProgress.current.toFloat() / batchProgress.total.toFloat()).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = StitchTokens.Primary,
                                trackColor = StitchTokens.SurfaceContainerHigh
                            )
                            Text(
                                text = "导入进度：${batchProgress.current} / ${batchProgress.total}",
                                fontSize = 12.sp,
                                fontFamily = StitchTokens.AppFontFamily,
                                color = StitchTokens.Outline
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = StitchTokens.Primary,
                                trackColor = StitchTokens.SurfaceContainerHigh
                            )
                        }
                    }
                },
                confirmButton = {}
            )
        }

        // 批量导入完成提示
        if (batchProgress.resultMessage != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissBatchImportProgress() },
                containerColor = StitchTokens.SurfaceContainer,
                titleContentColor = StitchTokens.Primary,
                textContentColor = StitchTokens.OnSurface,
                title = {
                    Text("导入结果", fontWeight = FontWeight.Bold, fontFamily = StitchTokens.AppFontFamily, fontSize = 17.sp)
                },
                text = {
                    Text(
                        text = batchProgress.resultMessage ?: "",
                        fontFamily = StitchTokens.AppFontFamily,
                        fontSize = 14.sp,
                        color = StitchTokens.Primary
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissBatchImportProgress() }) {
                        Text("确定", color = StitchTokens.Primary, fontFamily = StitchTokens.AppFontFamily)
                    }
                }
            )
        }

        // ================= 书架排序与排列弹窗 =================
        if (showSortDialog) {
            AlertDialog(
                onDismissRequest = { showSortDialog = false },
                containerColor = StitchTokens.SurfaceContainer,
                titleContentColor = StitchTokens.Primary,
                textContentColor = StitchTokens.OnSurface,
                title = {
                    Text(
                        text = "书架排序与排列",
                        fontWeight = FontWeight.Bold,
                        fontFamily = StitchTokens.AppFontFamily,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BookshelfSortMode.entries.forEach { mode ->
                            val isSelected = uiState.sortMode == mode
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.setSortMode(mode)
                                        if (mode != BookshelfSortMode.MANUAL) {
                                            showSortDialog = false
                                        }
                                    },
                                color = if (isSelected) StitchTokens.SurfaceContainerHigh else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                viewModel.setSortMode(mode)
                                                if (mode != BookshelfSortMode.MANUAL) {
                                                    showSortDialog = false
                                                }
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = StitchTokens.Primary
                                            )
                                        )
                                        Text(
                                            text = mode.displayName,
                                            fontFamily = StitchTokens.AppFontFamily,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = StitchTokens.Primary
                                        )
                                    }
                                    if (isSelected && mode == BookshelfSortMode.MANUAL) {
                                        TextButton(
                                            onClick = {
                                                showSortDialog = false
                                                showManualReorderDialog = true
                                            }
                                        ) {
                                            Text(
                                                text = "整理顺序 →",
                                                fontFamily = StitchTokens.AppFontFamily,
                                                fontSize = 12.5.sp,
                                                color = StitchTokens.Primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSortDialog = false }) {
                        Text("完成", color = StitchTokens.Primary, fontFamily = StitchTokens.AppFontFamily)
                    }
                }
            )
        }

        // ================= 手动整理书籍顺序弹窗 =================
        if (showManualReorderDialog) {
            val booksToReorder = uiState.books
            AlertDialog(
                onDismissRequest = { showManualReorderDialog = false },
                containerColor = StitchTokens.SurfaceContainer,
                titleContentColor = StitchTokens.Primary,
                textContentColor = StitchTokens.OnSurface,
                title = {
                    Column {
                        Text("手动整理书籍顺序", fontWeight = FontWeight.Bold, fontFamily = StitchTokens.AppFontFamily, fontSize = 17.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("点击右侧按钮可置顶、上移、下移或置底", color = StitchTokens.Outline, fontFamily = StitchTokens.AppFontFamily, fontSize = 11.5.sp)
                    }
                },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                    ) {
                        if (booksToReorder.isEmpty()) {
                            Text("书架暂无书籍", color = StitchTokens.Outline, modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(booksToReorder, key = { it.id }) { book ->
                                    val index = booksToReorder.indexOf(book)
                                    val isFirst = index == 0
                                    val isLast = index == booksToReorder.size - 1
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = StitchTokens.SurfaceContainerLow,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "#${index + 1}",
                                                    fontFamily = StitchTokens.AppFontFamily,
                                                    fontSize = 11.sp,
                                                    color = StitchTokens.Outline,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "《${cleanBookTitle(book.title)}》",
                                                    fontFamily = StitchTokens.AppFontFamily,
                                                    fontSize = 13.sp,
                                                    color = StitchTokens.Primary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(
                                                    onClick = { viewModel.moveBookToTop(book.id) },
                                                    enabled = !isFirst,
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.VerticalAlignTop,
                                                        contentDescription = "置顶",
                                                        tint = if (!isFirst) StitchTokens.Primary else StitchTokens.OutlineVariant,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { viewModel.moveBookUp(book.id) },
                                                    enabled = !isFirst,
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.ArrowUpward,
                                                        contentDescription = "上移",
                                                        tint = if (!isFirst) StitchTokens.Primary else StitchTokens.OutlineVariant,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { viewModel.moveBookDown(book.id) },
                                                    enabled = !isLast,
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.ArrowDownward,
                                                        contentDescription = "下移",
                                                        tint = if (!isLast) StitchTokens.Primary else StitchTokens.OutlineVariant,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showManualReorderDialog = false }) {
                        Text("完成", color = StitchTokens.Primary, fontFamily = StitchTokens.AppFontFamily)
                    }
                }
            )
        }

        // ================= 6. THEME SELECTION MODAL DIALOG =================
        if (showThemeDialog) {
            val currentPreset = AppThemeManager.currentTheme.collectAsStateWithLifecycle().value
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                containerColor = StitchTokens.SurfaceContainerLow,
                titleContentColor = StitchTokens.Primary,
                textContentColor = StitchTokens.OnSurface,
                title = {
                    Text(
                        text = "选择全局主题外观",
                        fontWeight = FontWeight.Bold,
                        fontFamily = StitchTokens.AppFontFamily,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppThemePreset.entries.forEach { preset ->
                            val isSelected = preset == currentPreset
                            Card(
                                onClick = {
                                    AppThemeManager.setTheme(context, preset)
                                    Toast.makeText(context, "已切换为：${preset.title}", Toast.LENGTH_SHORT).show()
                                    showThemeDialog = false
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) StitchTokens.SurfaceContainerHigh else StitchTokens.SurfaceContainerLowest
                                ),
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 0.8.dp,
                                    if (isSelected) StitchTokens.Primary else StitchTokens.Border
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = preset.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = StitchTokens.Primary,
                                            fontSize = 14.5.sp,
                                            fontFamily = StitchTokens.AppFontFamily
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = preset.description,
                                            fontSize = 11.5.sp,
                                            color = StitchTokens.Secondary,
                                            fontFamily = StitchTokens.AppFontFamily
                                        )
                                    }
                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = StitchTokens.Primary,
                                            modifier = Modifier.size(10.dp)
                                        ) {}
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text("关闭", color = StitchTokens.Outline, fontFamily = StitchTokens.AppFontFamily)
                    }
                }
            )
        }

        // ================= 7. AMBIANCE PICKER BOTTOM SHEET =================
        if (showAmbianceSheet) {
            AmbiancePickerSheet(
                onDismissRequest = { showAmbianceSheet = false }
            )
        }
    }
}

/**
 * 3-Column Bookshelf Item:
 * 1. Morandi / Minimalist Book Cover
 * 2. Exterior title wrapped in 《...》 with smaller muted serif font
 */
@Composable
private fun BookGridItem(
    book: BookEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Book Cover Card
        BookCover(
            book = book,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Outside Title in brackets (e.g. 《查拉图斯特拉如...》)
        Text(
            text = "《${cleanBookTitle(book.title)}》",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = StitchTokens.Secondary,
            fontSize = 11.5.sp,
            fontFamily = StitchTokens.AppFontFamily,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Windows File Explorer style Pure List Item:
 * 极简，什么都没有，只有书名。一行一行的每行书名下面有一条极细的进度条要美观好看，有艺术感。
 */
@Composable
private fun BookPureListItem(
    book: BookEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val cleanTitle = remember(book.title) { cleanBookTitle(book.title) }
    val progress = book.currentProgress.coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        // Minimalist: ONLY book title
        Text(
            text = "《$cleanTitle》",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = StitchTokens.AppFontFamily,
            color = StitchTokens.Primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Progress line track acting as the subtle divider between books (no separate HorizontalDivider needed)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.8.dp)
                .clip(RoundedCornerShape(0.4.dp))
                .background(if (StitchTokens.IsDark) Color(0x14FFFFFF) else Color(0x10000000))
        ) {
            if (progress > 0.002f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(StitchTokens.CoverProgress)
                )
            }
        }
    }
}

/**
 * Realistic Bookshelf Shelf Row (书架隔板与4本书籍书脊)
 * 像真实书架一样，显示的是每本书籍的书脊。外观要和真实书一样。每行显示四本书。
 * 下方为实木书架横板，呈现把书一本一本放在书架上的视觉质感。
 */
@Composable
private fun BookSpineShelfRow(
    books: List<BookEntity>,
    onBookClick: (Long) -> Unit,
    onBookLongClick: (BookEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Row of exactly 4 book spines standing on the shelf (uniform height 152.dp, equal width)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(152.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            for (i in 0 until 4) {
                val book = books.getOrNull(i)
                if (book != null) {
                    BookSpineItem(
                        book = book,
                        onClick = { onBookClick(book.id) },
                        onLongClick = { onBookLongClick(book) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                } else {
                    // Empty placeholder maintaining identical width for all books
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Realistic 3D Wooden Shelf Plank (实木书架横隔板)
        // 仿真书架的横木太宽了，窄一些像现在的一半那么窄（高度减半为6.5dp，高雅纤细）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            // Wood plank front surface with bevel highlight (half thickness: 6.5dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.5.dp)
                    .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = if (StitchTokens.IsDark) {
                                listOf(Color(0xFF5A3D2E), Color(0xFF3E271D), Color(0xFF281912))
                            } else {
                                listOf(Color(0xFF8D6E63), Color(0xFF6D4C41), Color(0xFF4E342E))
                            }
                        )
                    )
            ) {
                // Top bevel rim highlight reflecting library light (0.8dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.8.dp)
                        .background(if (StitchTokens.IsDark) Color(0x40FFFFFF) else Color(0x35FFFFFF))
                )
            }

            // Shelf ambient cast shadow beneath the wood plank (4dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color(0x28000000), Color.Transparent)
                        )
                    )
            )
        }
    }
}

/**
 * Realistic Book Spine Item (真实书籍书脊):
 * 1. 外观要和真实书一样，高度与现在的平铺封面高度一致(152dp)，每本书高度宽度都一样
 * 2. 统一典雅经典藏书 Oxford Slate-Navy 色调
 * 3. 3D 圆柱形曲面阴影、压纹烫金工艺横条纹
 * 4. 书名显示全：智能自适应排版（长书名采用古典双列竖排，无论多少字均完整全显）
 * 5. 进度条要极细的线在封面/书脊最下方（0.6dp 发丝细线，绝对定位在最底边，绝不遮挡书名文字）
 */
@Composable
private fun BookSpineItem(
    book: BookEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cleanTitle = remember(book.title) { cleanBookTitle(book.title) }
    val isDark = StitchTokens.IsDark

    // Uniform classic clothbound library edition color (Oxford Slate-Navy)
    val spineColor = if (isDark) Color(0xFF252E3A) else Color(0xFF333F50)
    val progress = book.currentProgress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
            .background(spineColor)
            .drawBehind {
                // 3D cylindrical spine shading (left shadow, center subtle highlight, right shadow)
                val w = size.width
                val h = size.height

                // Left curvature shadow
                drawRect(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(Color(0x40000000), Color.Transparent),
                        startX = 0f,
                        endX = w * 0.28f
                    )
                )

                // Right curvature shadow
                drawRect(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color(0x48000000)),
                        startX = w * 0.72f,
                        endX = w
                    )
                )

                // Decorative embossed gold headband (top horizontal double ribs)
                val goldColor = Color(0x88D4A359)
                drawLine(color = goldColor, start = Offset(w * 0.12f, 10f), end = Offset(w * 0.88f, 10f), strokeWidth = 1.2f)
                drawLine(color = goldColor, start = Offset(w * 0.12f, 14f), end = Offset(w * 0.88f, 14f), strokeWidth = 1.2f)

                // Decorative embossed gold tailband (bottom horizontal double ribs)
                drawLine(color = goldColor, start = Offset(w * 0.12f, h - 14f), end = Offset(w * 0.88f, h - 14f), strokeWidth = 1.2f)
                drawLine(color = goldColor, start = Offset(w * 0.12f, h - 10f), end = Offset(w * 0.88f, h - 10f), strokeWidth = 1.2f)
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top spacing below headband
            Spacer(modifier = Modifier.height(8.dp))

            // Center: Full Book Title displayed vertically with adaptive typography
            // 书名文件名全显示：短标题单列竖排大字，长标题双列典雅竖排，字迹清晰绝不遮挡
            val chars = remember(cleanTitle) { cleanTitle.toList() }
            val charCount = chars.size

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (charCount <= 8) {
                    val fontSize = if (charCount <= 4) 14.sp else if (charCount <= 6) 12.5.sp else 11.sp
                    val lineHeight = if (charCount <= 4) 17.sp else if (charCount <= 6) 15.sp else 13.sp
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        chars.forEach { ch ->
                            Text(
                                text = ch.toString(),
                                fontSize = fontSize,
                                fontWeight = FontWeight.Bold,
                                fontFamily = StitchTokens.AppFontFamily,
                                color = Color(0xFFF7F5F0), // Hot-stamped cream gold
                                lineHeight = lineHeight,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (charCount <= 22) {
                    // Traditional dual-column vertical layout for medium-to-long book titles/filenames
                    val splitIdx = (charCount + 1) / 2
                    val col1Chars = chars.take(splitIdx)
                    val col2Chars = chars.drop(splitIdx)
                    val fontSize = if (charCount <= 14) 10.sp else 8.5.sp
                    val lineHeight = if (charCount <= 14) 12.5.sp else 10.5.sp

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            col1Chars.forEach { ch ->
                                Text(
                                    text = ch.toString(),
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = StitchTokens.AppFontFamily,
                                    color = Color(0xFFF7F5F0),
                                    lineHeight = lineHeight,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            col2Chars.forEach { ch ->
                                Text(
                                    text = ch.toString(),
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = StitchTokens.AppFontFamily,
                                    color = Color(0xFFF7F5F0),
                                    lineHeight = lineHeight,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // 3-column vertical layout for extra-long book titles/filenames (full filename visible)
                    val itemsPerCol = (charCount + 2) / 3
                    val col1Chars = chars.take(itemsPerCol)
                    val col2Chars = chars.drop(itemsPerCol).take(itemsPerCol)
                    val col3Chars = chars.drop(itemsPerCol * 2)
                    val fontSize = if (charCount <= 30) 7.5.sp else 6.5.sp
                    val lineHeight = if (charCount <= 30) 9.2.sp else 8.sp

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(1.5.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            col1Chars.forEach { ch ->
                                Text(
                                    text = ch.toString(),
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = StitchTokens.AppFontFamily,
                                    color = Color(0xFFF7F5F0),
                                    lineHeight = lineHeight,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            col2Chars.forEach { ch ->
                                Text(
                                    text = ch.toString(),
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = StitchTokens.AppFontFamily,
                                    color = Color(0xFFF7F5F0),
                                    lineHeight = lineHeight,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            col3Chars.forEach { ch ->
                                Text(
                                    text = ch.toString(),
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = StitchTokens.AppFontFamily,
                                    color = Color(0xFFF7F5F0),
                                    lineHeight = lineHeight,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }

        // Bottom edge: Ultra-fine progress hairline at the absolute bottom edge of the book spine
        // 进度条要极细的线在封面儿的最下方，绝不遮挡或侵占上方书名文字区域
        if (progress > 0.002f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp, vertical = 2.dp)
                    .height(0.6.dp)
                    .clip(RoundedCornerShape(0.3.dp))
                    .background(Color(0x33FFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(Color(0xFFD4A359))
                )
            }
        }
    }
}
