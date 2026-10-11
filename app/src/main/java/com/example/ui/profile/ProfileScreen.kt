package com.example.ui.profile

import android.widget.Toast
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Speed
import com.example.util.AppLanguage
import com.example.util.appString
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.ambiance.AmbianceManager
import com.example.ui.ambiance.AmbiancePickerSheet
import com.example.ui.theme.AppThemeManager
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.StitchTokens
import com.example.ui.viewmodel.BookshelfViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    bookshelfViewModel: BookshelfViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by bookshelfViewModel.uiState.collectAsStateWithLifecycle()
    val currentScene by AmbianceManager.currentScene.collectAsState()
    val currentThemePreset by AppThemeManager.currentTheme.collectAsStateWithLifecycle()
    val totalBooks = uiState.books.size
    val totalRead = uiState.books.count { it.currentProgress > 0.05f }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showAmbianceSheet by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StitchTokens.Background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ================= 1. PROFILE HEADER =================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(StitchTokens.SurfaceContainerLowest)
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(StitchTokens.SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_stitch_logo),
                            contentDescription = "Logo",
                            tint = StitchTokens.Primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appString("profile_user_title"),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchTokens.Primary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = appString("profile_user_subtitle"),
                            fontSize = 12.5.sp,
                            color = StitchTokens.Outline
                        )
                    }
                }
            }

            // ================= 2. READING STATS ROW =================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(StitchTokens.SurfaceContainerLowest)
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatMetric(label = appString("stat_total_books"), value = "$totalBooks ${appString("stat_books_unit")}")
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(28.dp)
                            .background(StitchTokens.BorderDivider)
                    )
                    StatMetric(label = appString("stat_read_books"), value = "$totalRead ${appString("stat_books_unit")}")
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(28.dp)
                            .background(StitchTokens.BorderDivider)
                    )
                    StatMetric(label = appString("stat_focus_days"), value = appString("stat_days_unit"))
                }
            }

            // ================= 3. SETTINGS ITEMS (THEME, AMBIANCE & SYSTEM) =================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(StitchTokens.SurfaceContainerLowest)
                ) {
                    // Global Theme Selection Entry
                    SettingTile(
                        icon = Icons.Filled.ColorLens,
                        title = appString("setting_theme_title"),
                        subtitle = "${currentThemePreset.title} · ${currentThemePreset.projectTag}",
                        onClick = { showThemeDialog = true }
                    )

                    HorizontalDivider(color = StitchTokens.BorderDivider, thickness = 0.5.dp)

                    // Global App Language Setting (Supports Chinese, Korean, English, Traditional)
                    val currentLangName = when (uiState.appLanguage) {
                        AppLanguage.ZH_CN -> "简体中文 (Chinese)"
                        AppLanguage.KO -> "한국어 (韩文)"
                        AppLanguage.EN -> "English (英文)"
                        AppLanguage.ZH_TW -> "繁體中文"
                        AppLanguage.SYSTEM -> "跟随系统 (System)"
                    }
                    SettingTile(
                        icon = Icons.Filled.Language,
                        title = appString("app_language_title"),
                        subtitle = "当前：$currentLangName · 自由切换全应用语言",
                        onClick = { showLanguageDialog = true }
                    )

                    HorizontalDivider(color = StitchTokens.BorderDivider, thickness = 0.5.dp)

                    // Global App Font Size Setting (Elderly / visually impaired readers care)
                    val currentScaleDesc = when {
                        uiState.appFontScale >= 1.40f -> "超大关怀版 (1.45x) · 视力不佳推荐"
                        uiState.appFontScale >= 1.25f -> "特大 (1.30x) · 适合大屏与长辈"
                        uiState.appFontScale >= 1.10f -> "中大 (1.15x) · 舒适阅读"
                        else -> "标准 (1.0x) · 默认字号"
                    }
                    SettingTile(
                        icon = Icons.Filled.FormatSize,
                        title = appString("app_font_size_title"),
                        subtitle = "当前：$currentScaleDesc",
                        onClick = { showFontSizeDialog = true }
                    )

                    HorizontalDivider(color = StitchTokens.BorderDivider, thickness = 0.5.dp)

                    // Reading Ambiance Scene Setting Entry
                    SettingTile(
                        icon = Icons.Filled.AutoAwesome,
                        title = appString("setting_ambiance_title"),
                        subtitle = "当前：${currentScene.title} · ${currentScene.subtitle}",
                        onClick = { showAmbianceSheet = true }
                    )

                    HorizontalDivider(color = StitchTokens.BorderDivider, thickness = 0.5.dp)

                    SettingTile(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        title = appString("setting_engine_title"),
                        subtitle = appString("setting_engine_sub"),
                        onClick = {
                            Toast.makeText(context, "当前已应用黑白极简排版引擎", Toast.LENGTH_SHORT).show()
                        }
                    )

                    HorizontalDivider(color = StitchTokens.BorderDivider, thickness = 0.5.dp)

                    SettingTile(
                        icon = Icons.Filled.Speed,
                        title = appString("setting_search_title"),
                        subtitle = appString("setting_search_sub"),
                        onClick = {
                            Toast.makeText(context, "长文段落匹配及错别字容错模糊搜索运行正常", Toast.LENGTH_SHORT).show()
                        }
                    )

                    HorizontalDivider(color = StitchTokens.BorderDivider, thickness = 0.5.dp)

                    SettingTile(
                        icon = Icons.Filled.Info,
                        title = "关于软件",
                        subtitle = "基于 Google Stitch 设计项目 16867460215153345449 重制",
                        onClick = {
                            Toast.makeText(context, "Google Stitch 设计系统 16867460215153345449", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Theme Selection Dialog
        if (showThemeDialog) {
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
                            val isSelected = preset == currentThemePreset
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = preset.title,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontFamily = StitchTokens.AppFontFamily,
                                                fontSize = 13.5.sp,
                                                color = StitchTokens.Primary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = StitchTokens.SurfaceContainerHigh,
                                                modifier = Modifier.padding(1.dp)
                                            ) {
                                                Text(
                                                    text = preset.projectTag,
                                                    fontSize = 10.sp,
                                                    fontFamily = StitchTokens.AppFontFamily,
                                                    color = StitchTokens.Outline,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))

                                        Text(
                                            text = preset.description,
                                            fontSize = 11.5.sp,
                                            fontFamily = StitchTokens.AppFontFamily,
                                            color = StitchTokens.Outline,
                                            lineHeight = 15.sp
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Color Preview Swatches (Background, Cover, Accent)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // Shelf background swatch
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(preset.colors.background)
                                                    .border(0.5.dp, StitchTokens.Border, CircleShape)
                                            )
                                            // Cover background swatch
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(preset.colors.coverBackground)
                                                    .border(0.5.dp, preset.colors.coverBorder, RoundedCornerShape(3.dp))
                                            )
                                            // Primary accent swatch
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(preset.colors.accent)
                                            )
                                            Text(
                                                text = "底色 / 封面 / 高光",
                                                fontSize = 10.sp,
                                                fontFamily = StitchTokens.AppFontFamily,
                                                color = StitchTokens.Outline
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = StitchTokens.Primary,
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "已应用",
                                                    tint = StitchTokens.OnPrimary,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text(appString("dialog_done"), color = StitchTokens.Primary, fontFamily = StitchTokens.AppFontFamily)
                    }
                }
            )
        }

        // Global Language Dialog
        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                containerColor = StitchTokens.SurfaceContainerLow,
                titleContentColor = StitchTokens.Primary,
                textContentColor = StitchTokens.OnSurface,
                title = {
                    Text(
                        text = appString("app_language_title"),
                        fontWeight = FontWeight.Bold,
                        fontFamily = StitchTokens.AppFontFamily,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            AppLanguage.ZH_CN to "简体中文 (Chinese)",
                            AppLanguage.KO to "한국어 (韩文)",
                            AppLanguage.EN to "English (英文)",
                            AppLanguage.ZH_TW to "繁體中文 (Traditional Chinese)",
                            AppLanguage.SYSTEM to "跟随系统 (System Default)"
                        ).forEach { (lang, label) ->
                            val isSelected = uiState.appLanguage == lang
                            Card(
                                onClick = {
                                    bookshelfViewModel.setAppLanguage(lang)
                                    showLanguageDialog = false
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
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) StitchTokens.Primary else StitchTokens.OnSurface,
                                        fontSize = 14.sp
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = StitchTokens.Primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text(appString("dialog_done"), color = StitchTokens.Primary, fontFamily = StitchTokens.AppFontFamily)
                    }
                }
            )
        }

        // Global Font Size Dialog
        if (showFontSizeDialog) {
            AlertDialog(
                onDismissRequest = { showFontSizeDialog = false },
                containerColor = StitchTokens.SurfaceContainerLow,
                titleContentColor = StitchTokens.Primary,
                textContentColor = StitchTokens.OnSurface,
                title = {
                    Text(
                        text = appString("app_font_size_title"),
                        fontWeight = FontWeight.Bold,
                        fontFamily = StitchTokens.AppFontFamily,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            1.0f to ("标准 (1.0x)" to "默认适中尺寸，清晰美观"),
                            1.15f to ("中大 (1.15x)" to "适度放大，字迹舒展自然"),
                            1.30f to ("特大 (1.30x)" to "大字清晰，长久阅读不易疲劳"),
                            1.45f to ("超大关怀版 (1.45x)" to "关怀视力不佳读者，醒目清晰超大字")
                        ).forEach { (scale, pair) ->
                            val (title, desc) = pair
                            val isSelected = kotlin.math.abs(uiState.appFontScale - scale) < 0.05f
                            Card(
                                onClick = {
                                    bookshelfViewModel.setAppFontScale(scale)
                                    showFontSizeDialog = false
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
                                            text = title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) StitchTokens.Primary else StitchTokens.OnSurface,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = desc,
                                            color = StitchTokens.Outline,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = StitchTokens.Primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFontSizeDialog = false }) {
                        Text(appString("dialog_done"), color = StitchTokens.Primary, fontFamily = StitchTokens.AppFontFamily)
                    }
                }
            )
        }

        // Ambiance Scene Picker Bottom Sheet directly opened from Profile
        if (showAmbianceSheet) {
            AmbiancePickerSheet(
                onDismissRequest = { showAmbianceSheet = false }
            )
        }
    }
}

@Composable
private fun StatMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = StitchTokens.Primary
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.5.sp,
            color = StitchTokens.Outline
        )
    }
}

@Composable
private fun SettingTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = StitchTokens.Outline,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = StitchTokens.Primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = StitchTokens.Outline
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = StitchTokens.OutlineVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}
