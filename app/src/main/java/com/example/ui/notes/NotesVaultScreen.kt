package com.example.ui.notes

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.BookEntity
import com.example.data.entity.ReadingNoteEntity
import com.example.ui.theme.StitchTokens

data class VaultQuote(
    val id: String,
    val category: String,
    val categoryEn: String,
    val quote: String,
    val bookTitle: String,
    val author: String,
    val note: String,
    val date: String,
    val tag: String,
    var isStarred: Boolean = true,
    val bookId: Long = 1L
)

@Composable
fun NotesVaultScreen(
    userNotes: List<ReadingNoteEntity>,
    books: List<BookEntity>,
    onOpenBook: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("全部") }
    var shareCardQuote by remember { mutableStateOf<VaultQuote?>(null) }

    // Seed Stitch's iconic philosophical curated notes alongside user's notes
    var curatedQuotes by remember {
        mutableStateOf(
            listOf(
                VaultQuote(
                    id = "q1",
                    category = "哲学",
                    categoryEn = "PHILOSOPHY · 哲学",
                    quote = "“每一个不曾起舞的日子，都是对生命的辜负。”",
                    bookTitle = "查拉图斯特拉如是说",
                    author = "尼采",
                    note = "夜读至此，猛然惊醒。身体的安静与精神的起舞并不矛盾，真正的秩序来自于内在自律。",
                    date = "2024.10.14",
                    tag = "特别珍藏",
                    isStarred = true
                ),
                VaultQuote(
                    id = "q2",
                    category = "文学",
                    categoryEn = "LITERATURE · 文学",
                    quote = "“满地都是六便士，他却抬头看见了月亮。”",
                    bookTitle = "月亮与六便士",
                    author = "毛姆",
                    note = "理想与现实的永恒拉扯，唯有执念能穿透迷雾，照亮灵魂深处。",
                    date = "2024.10.08",
                    tag = "经典摘录",
                    isStarred = true
                ),
                VaultQuote(
                    id = "q3",
                    category = "哲学",
                    categoryEn = "STOICISM · 斯多葛",
                    quote = "“你有力量控制你的思想，而不是外部事件。意识到这一点，你就会找到力量。”",
                    bookTitle = "沉思录",
                    author = "马可·奥勒留",
                    note = "在纷扰繁复的信息洪流中守住内心的静谧之隅。",
                    date = "2024.09.28",
                    tag = "每日心法",
                    isStarred = false
                ),
                VaultQuote(
                    id = "q4",
                    category = "随想",
                    categoryEn = "COSMOS · 理论物理",
                    quote = "“时间不是客观均匀流淌的河流，它因引力而变形，因观察而凝固。”",
                    bookTitle = "时间的秩序",
                    author = "卡洛·罗威利",
                    note = "重塑了我对过往、当下与未来的直觉认知。",
                    date = "2024.09.15",
                    tag = "深度随想",
                    isStarred = true
                )
            )
        )
    }

    // Merge Room database userNotes as VaultQuotes
    val allDisplayQuotes = remember(curatedQuotes, userNotes) {
        val userVaultQuotes = userNotes.map { note ->
            VaultQuote(
                id = "user_${note.id}",
                category = "手记",
                categoryEn = "NOTE · 读书手记",
                quote = "“${note.quoteText}”",
                bookTitle = note.bookTitle,
                author = "读者笔录",
                note = note.noteContent.ifEmpty { "记录于 ${note.chapterTitle}" },
                date = "最新记录",
                tag = "用户笔记",
                isStarred = true,
                bookId = note.bookId
            )
        }
        curatedQuotes + userVaultQuotes
    }

    val categories = listOf("全部 (${allDisplayQuotes.size})", "哲学", "文学", "随想", "手记")

    val filteredQuotes = remember(selectedCategory, allDisplayQuotes) {
        if (selectedCategory.startsWith("全部")) {
            allDisplayQuotes
        } else {
            allDisplayQuotes.filter { it.category == selectedCategory }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StitchTokens.Background)
            .padding(horizontal = StitchTokens.MarginMobile),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 0: Thought Archive Summary Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = StitchTokens.SurfaceContainerLow,
                border = BorderStroke(1.dp, StitchTokens.Border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "THOUGHT ARCHIVE · 思想沉淀",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.4.sp,
                            color = StitchTokens.Outline,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(StitchTokens.Primary)
                            )
                            Text(
                                text = "SYNCED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StitchTokens.Primary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "已收录",
                            fontSize = 13.sp,
                            color = StitchTokens.OnSurface
                        )
                        Text(
                            text = "${allDisplayQuotes.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchTokens.Primary
                        )
                        Text(
                            text = "条高光 · 持续思考",
                            fontSize = 13.sp,
                            color = StitchTokens.OnSurface
                        )
                        Text(
                            text = "142",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchTokens.Primary
                        )
                        Text(
                            text = "天",
                            fontSize = 13.sp,
                            color = StitchTokens.OnSurface
                        )
                    }
                }
            }
        }

        // Category Filter Tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = (cat == selectedCategory) || (cat.startsWith("全部") && selectedCategory.startsWith("全部"))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) StitchTokens.Primary else StitchTokens.SurfaceContainer,
                        border = BorderStroke(1.dp, if (isSelected) StitchTokens.Primary else StitchTokens.Border),
                        modifier = Modifier
                            .clickable { selectedCategory = cat }
                            .testTag("filter_pill_${cat}")
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) StitchTokens.OnPrimary else StitchTokens.OnSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Section 1: Note & Highlight Cards
        items(filteredQuotes, key = { it.id }) { quoteItem ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StitchTokens.SurfaceContainerLowest),
                border = BorderStroke(1.dp, StitchTokens.Border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category & Star
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(StitchTokens.Primary)
                            )
                            Text(
                                text = quoteItem.categoryEn,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                color = StitchTokens.Outline,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = {
                                curatedQuotes = curatedQuotes.map {
                                    if (it.id == quoteItem.id) it.copy(isStarred = !it.isStarred) else it
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (quoteItem.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "收藏状态",
                                tint = if (quoteItem.isStarred) StitchTokens.GoldAccent else StitchTokens.Outline,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Main Quote in Serif
                    Text(
                        text = quoteItem.quote,
                        fontSize = 16.sp,
                        lineHeight = 26.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        color = StitchTokens.Primary
                    )

                    // Book source tag
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = StitchTokens.SurfaceContainerLow,
                        border = BorderStroke(0.5.dp, StitchTokens.Border),
                        modifier = Modifier.clickable {
                            val matched = books.firstOrNull { it.title.contains(quoteItem.bookTitle) }
                                ?: books.firstOrNull()
                            if (matched != null) onOpenBook(matched.id)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = StitchTokens.Outline,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "《${quoteItem.bookTitle}》· ${quoteItem.author}",
                                fontSize = 11.sp,
                                color = StitchTokens.OnSurfaceVariant
                            )
                        }
                    }

                    // Reading note
                    if (quoteItem.note.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StitchTokens.SurfaceContainerLowest,
                            border = BorderStroke(0.5.dp, StitchTokens.SurfaceContainerHighest),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = StitchTokens.Outline,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "手记：${quoteItem.note}",
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = StitchTokens.OnSurfaceVariant
                                )
                            }
                        }
                    }

                    // Bottom info and actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${quoteItem.date} · ${quoteItem.tag}",
                            fontSize = 10.sp,
                            color = StitchTokens.Outline
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Copy button
                            IconButton(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Quote", "${quoteItem.quote}\n——《${quoteItem.bookTitle}》")
                                    cm.setPrimaryClip(clip)
                                    Toast.makeText(context, "已复制摘录至剪贴板", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "复制",
                                    tint = StitchTokens.Outline,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // Share Card button
                            IconButton(
                                onClick = { shareCardQuote = quoteItem },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "导出卡片",
                                    tint = StitchTokens.Outline,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Share Card Modal Dialog
    if (shareCardQuote != null) {
        val q = shareCardQuote!!
        Dialog(onDismissRequest = { shareCardQuote = null }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = StitchTokens.SurfaceContainerLowest,
                border = BorderStroke(1.dp, StitchTokens.PrimaryFixedDim),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INK & MONO · 精选摘录",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = StitchTokens.Outline
                        )
                        IconButton(
                            onClick = { shareCardQuote = null },
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StitchTokens.SurfaceContainerLow, RoundedCornerShape(12.dp))
                            .border(1.dp, StitchTokens.Border, RoundedCornerShape(12.dp))
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                text = q.quote,
                                fontSize = 17.sp,
                                lineHeight = 28.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Normal,
                                color = StitchTokens.Primary
                            )
                            HorizontalDivider(color = StitchTokens.Border, thickness = 0.5.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "《${q.bookTitle}》",
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Serif,
                                    color = StitchTokens.Primary
                                )
                                Text(
                                    text = q.author,
                                    fontSize = 12.sp,
                                    color = StitchTokens.Outline
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Share Card", "${q.quote}\n——《${q.bookTitle}》· ${q.author}\n[来自 读丛书·极简静读]")
                            cm.setPrimaryClip(clip)
                            Toast.makeText(context, "已生成高质感书摘并复制分享文本！", Toast.LENGTH_SHORT).show()
                            shareCardQuote = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StitchTokens.Primary,
                            contentColor = StitchTokens.OnPrimary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(text = "分享精美卡片", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
