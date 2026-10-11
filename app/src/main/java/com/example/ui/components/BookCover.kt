package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BookEntity
import com.example.ui.theme.StitchTokens

/**
 * 1:1 Screenshot Replica Book Cover
 *
 * Requirements from Screenshot:
 * - Morandi muted warm tone background (or dark block in dark theme)
 * - Rounded corners (6.dp) with subtle shadow in light theme
 * - Top-left: Category tag in all caps, letter-spaced Serif (e.g. PHIL, LIT, SCI, ESSAY, NOVEL, MODERN)
 * - Top-right: Bookmark line icon
 * - Center: Book title in Bold Serif, centered
 * - Bottom-left: Author name (shortened)
 * - Bottom-right: Country abbreviation (DE, JP, GR, IT, US, FR, UK, CZ, CN)
 */
@Composable
fun BookCover(
    book: BookEntity,
    modifier: Modifier = Modifier,
    showInternalProgress: Boolean = false
) {
    val cleanTitle = remember(book.title) {
        book.title
            .replace(Regex("""[《》〈〉（）()【】\[\]「」『』“”"''<>《》〔〕]"""), "")
            .replace(Regex("""\.(txt|epub|docx|pdf|mobi)$""", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    val displayCenterTitle = remember(cleanTitle) {
        if (cleanTitle == "查拉图斯特拉如是说") {
            "查拉图斯特拉\n如是说"
        } else {
            cleanTitle
        }
    }

    val categoryTag = remember(book) { getBookCategoryTag(book) }
    val authorShort = remember(book) { getBookShortAuthor(book) }
    val countryCode = remember(book) { getBookCountryCode(book) }

    val isDark = StitchTokens.IsDark
    val coverBgColor = remember(isDark, cleanTitle, book.id) {
        if (isDark) {
            Color(0xFF262626) // Visibly lighter than #131313 in dark mode
        } else {
            // 统一使用第六本、第七本的典雅灰紫莫兰迪色，实现全书架封面色调完全统一
            Color(0xFFD6D1DA)
        }
    }

    val shape = RoundedCornerShape(1.5.dp)

    Box(
        modifier = modifier
            .aspectRatio(0.70f) // Refined book card ratio
            .then(
                if (!isDark) {
                    Modifier.shadow(
                        elevation = 1.2.dp,
                        shape = shape,
                        spotColor = Color(0x12000000),
                        ambientColor = Color(0x06000000)
                    )
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(coverBgColor)
            .border(
                width = 0.6.dp,
                color = StitchTokens.CoverBorder,
                shape = shape
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 9.dp, end = 9.dp, top = 9.dp, bottom = 7.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ================= TOP ROW: CATEGORY TAG + BOOKMARK ICON =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryTag,
                    fontFamily = StitchTokens.AppFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.8.sp,
                    color = StitchTokens.Secondary
                )

                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = StitchTokens.Secondary,
                    modifier = Modifier.size(13.dp)
                )
            }

            // ================= CENTER: TITLE IN BOLD SERIF =================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayCenterTitle,
                    color = StitchTokens.CoverTextColor,
                    fontSize = if (displayCenterTitle.length <= 4) 14.sp else if (displayCenterTitle.length <= 8) 12.5.sp else 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = StitchTokens.AppFontFamily,
                    lineHeight = if (displayCenterTitle.length <= 4) 18.sp else 16.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ================= BOTTOM ROW: AUTHOR + COUNTRY =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = authorShort,
                    fontFamily = StitchTokens.AppFontFamily,
                    fontSize = 8.5.sp,
                    color = StitchTokens.Secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = countryCode,
                    fontFamily = StitchTokens.AppFontFamily,
                    fontSize = 8.5.sp,
                    letterSpacing = 0.5.sp,
                    color = StitchTokens.Outline
                )
            }

            Spacer(modifier = Modifier.height(5.dp))
        }

        // ================= BOTTOM EDGE: ULTRA-SLIM INTEGRATED PROGRESS LINE =================
        // 这个进度条在封面的最下边边缘，就是封面的下边缘，不用很醒目，线深深一些让用户看着协调
        val progress = book.currentProgress.coerceIn(0f, 1f)
        val coverProgressColor = StitchTokens.Primary // 边缘线深深一些，与主题主色调协调一致
        val trackBg = if (isDark) Color(0x22FFFFFF) else Color(0x18000000)

        if (progress > 0.002f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(1.2.dp)
                    .background(trackBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(coverProgressColor)
                )
            }
        }
    }
}

// Morandi muted color palette strictly matched to screenshot pixel tones
// 灰紫, 浅卡其, 藏青灰, 豆沙粉, 橄榄绿, 赭石等, 冷调米白基底, 无明显暖红/暖橙倾向
private fun getMorandiCoverBg(title: String, id: Long): Color {
    val morandiPalette = listOf(
        Color(0xFFD6D1DA), // 灰紫 (Muted Lavender Gray)
        Color(0xFFD8D3C8), // 浅卡其 (Pale Khaki Oat)
        Color(0xFFC7CBD3), // 藏青灰 (Slate Navy Gray)
        Color(0xFFD8CAC9), // 豆沙粉 (Dusty Bean Paste Rose)
        Color(0xFFCBD0C7), // 橄榄绿 (Muted Olive Sage)
        Color(0xFFD4C8BC), // 赭石 (Muted Ochre Stone)
        Color(0xFFC8CED3), // 雾霾蓝 (Haze Blue Gray)
        Color(0xFFD3CEC6), // 亚麻灰 (Linen Gray)
        Color(0xFFD5D2CC)  // 冷米灰 (Cool Rice Gray)
    )
    val index = kotlin.math.abs((title.hashCode() + id.toInt())) % morandiPalette.size
    return morandiPalette[index]
}

private fun getBookCategoryTag(book: BookEntity): String {
    val t = book.title
    return when {
        t.contains("查拉图斯特拉") || t.contains("沉思录") || t.contains("悉达多") || t.contains("道德经") || t.contains("传习录") -> "PHIL"
        t.contains("金阁寺") || t.contains("局外人") -> "LIT"
        t.contains("时间") || t.contains("物理") || t.contains("科学") -> "SCI"
        t.contains("瓦尔登") || t.contains("散文") -> "ESSAY"
        t.contains("月亮与六便士") || t.contains("小说") -> "NOVEL"
        t.contains("城堡") || t.contains("变形记") -> "MODERN"
        t.contains("诗经") || t.contains("宋词") || t.contains("诗") -> "POETRY"
        book.category.isNotBlank() && book.category != "全部" -> book.category.take(4).uppercase()
        else -> "LIT"
    }
}

private fun getBookCountryCode(book: BookEntity): String {
    val t = book.title
    val a = book.author
    return when {
        t.contains("查拉图斯特拉") || t.contains("悉达多") || a.contains("尼采") || a.contains("黑塞") -> "DE"
        t.contains("金阁寺") || a.contains("三岛") || a.contains("村上") || a.contains("川端") -> "JP"
        t.contains("沉思录") || a.contains("奥勒留") -> "GR"
        t.contains("时间") || a.contains("罗威利") -> "IT"
        t.contains("瓦尔登") || a.contains("梭罗") -> "US"
        t.contains("局外人") || a.contains("加缪") -> "FR"
        t.contains("月亮与六便士") || a.contains("毛姆") -> "UK"
        t.contains("城堡") || a.contains("卡夫卡") -> "CZ"
        t.contains("道德经") || t.contains("传习录") || t.contains("诗经") || t.contains("宋词") || a.contains("老子") || a.contains("王阳明") -> "CN"
        else -> "ED"
    }
}

private fun getBookShortAuthor(book: BookEntity): String {
    val a = book.author.trim()
    return when {
        a.contains("尼采") -> "尼采"
        a.contains("三岛由纪夫") -> "三岛由纪夫"
        a.contains("奥勒留") -> "奥勒留"
        a.contains("罗威利") -> "罗威利"
        a.contains("梭罗") -> "梭罗"
        a.contains("加缪") -> "加缪"
        a.contains("毛姆") -> "毛姆"
        a.contains("黑塞") -> "黑塞"
        a.contains("卡夫卡") -> "卡夫卡"
        a.contains("老子") -> "老子"
        a.contains("王阳明") -> "王阳明"
        a.isBlank() -> "佚名"
        a.length > 5 -> a.take(4)
        else -> a
    }
}
