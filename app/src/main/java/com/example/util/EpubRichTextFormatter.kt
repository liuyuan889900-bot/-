package com.example.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import android.text.Spanned
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.graphics.Typeface

object EpubRichTextFormatter {

    /**
     * Checks whether the content contains HTML or XHTML markup tags.
     */
    fun hasHtmlMarkup(text: String): Boolean {
        return text.contains("<p", ignoreCase = true) ||
                text.contains("<h1", ignoreCase = true) ||
                text.contains("<h2", ignoreCase = true) ||
                text.contains("<h3", ignoreCase = true) ||
                text.contains("<h4", ignoreCase = true) ||
                text.contains("<h5", ignoreCase = true) ||
                text.contains("<h6", ignoreCase = true) ||
                text.contains("<div", ignoreCase = true) ||
                text.contains("<b", ignoreCase = true) ||
                text.contains("<strong", ignoreCase = true) ||
                text.contains("<i", ignoreCase = true) ||
                text.contains("<em", ignoreCase = true) ||
                text.contains("<u", ignoreCase = true) ||
                text.contains("<span", ignoreCase = true) ||
                text.contains("<blockquote", ignoreCase = true)
    }

    /**
     * Strips HTML tags and unescapes HTML entities, preserving image references as [IMG:...] markers.
     */
    fun stripHtml(html: String): String {
        var processed = html
            .replace(Regex("""<img[^>]+src=["']([^"']+)["'][^>]*>""", RegexOption.IGNORE_CASE)) { "\n\n[IMG:${it.groupValues[1]}]\n\n" }
            .replace(Regex("""<image[^>]+(?:xlink:href|href)=["']([^"']+)["'][^>]*>""", RegexOption.IGNORE_CASE)) { "\n\n[IMG:${it.groupValues[1]}]\n\n" }
        if (!hasHtmlMarkup(processed) && !processed.contains("<br", ignoreCase = true)) {
            return CharsetDetector.healAndSanitize(processed)
        }
        val stripped = processed
            .replace(Regex("""<!--[\s\S]*?-->"""), "")
            .replace(Regex("""<script[^>]*>[\s\S]*?</script>""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""<style[^>]*>[\s\S]*?</style>""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""<head[^>]*>[\s\S]*?</head>""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""<(p|div|tr|li|h[1-6]|section|article|blockquote|dd|dt)[^>]*>""", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("""<[^>]+>"""), "")
            .unescapeHtml()
            .trim()
        return CharsetDetector.healAndSanitize(stripped)
    }

    /**
     * Normalizes paragraph text extracted from EPUB HTML.
     * Removes artificial line breaks within paragraphs that originate from raw HTML formatting,
     * seamlessly joining CJK text and converting non-CJK internal newlines into single spaces.
     * Prevents broken lines, premature cutoffs, and half-empty lines during reader pagination.
     */
    fun normalizeParagraphText(rawBlock: String): String {
        if (rawBlock.startsWith("[IMG:") && rawBlock.endsWith("]")) return rawBlock
        
        // Remove soft newlines between CJK characters:
        var text = rawBlock.replace(Regex("""([\u4e00-\u9fa5\u3000-\u303f\uff00-\uffef])\s*[\r\n]+\s*([\u4e00-\u9fa5\u3000-\u303f\uff00-\uffef])"""), "$1$2")
        // Remove soft newlines between CJK and English/numbers:
        text = text.replace(Regex("""([\u4e00-\u9fa5])\s*[\r\n]+\s*([a-zA-Z0-9])"""), "$1 $2")
        text = text.replace(Regex("""([a-zA-Z0-9])\s*[\r\n]+\s*([\u4e00-\u9fa5])"""), "$1 $2")
        // Convert any remaining single newlines into a space:
        text = text.replace(Regex("""\r?\n"""), " ")
        // Collapse multiple spaces into one space:
        text = text.replace(Regex("""[ \t]+"""), " ")
        return text.trim()
    }

    /**
     * Converts rich HTML content into a Jetpack Compose AnnotatedString,
     * preserving semantic bold, italics, underline, strikethrough, and proportional
     * heading sizes (h1: 1.38x, h2: 1.25x, h3: 1.15x) relative to [baseFontSizeSp].
     *
     * Colors and themes adapt strictly to [textColor] and [isDark], while [highlightKeyword]
     * is highlighted with a gold/amber span.
     */
    fun formatRichText(
        content: String,
        baseFontSizeSp: Float,
        textColor: Color,
        isDark: Boolean,
        highlightKeyword: String = "",
        underlines: List<String> = emptyList(),
        selectedRange: IntRange? = null
    ): AnnotatedString {
        // Strip [IMG:...] token before formatting text so raw tag text is never printed as plain text
        val textContent = content.replace(Regex("""\[IMG:[^\]]+\]"""), "").trim()
        val cleanKeyword = highlightKeyword.trim()

        if (!hasHtmlMarkup(textContent)) {
            // Standard plain text formatting
            return buildAnnotatedString {
                append(textContent)
                applyKeywordHighlights(this, textContent, cleanKeyword, isDark)
                applyUnderlines(this, textContent, underlines, isDark)
                applySelectionHighlight(this, textContent, selectedRange, isDark)
            }
        }

        // Convert HTML with pre-processing to preserve semantic headings and blocks
        val preprocessedHtml = preprocessHtmlForSpanned(textContent)

        val spanned: Spanned = try {
            HtmlCompat.fromHtml(
                preprocessedHtml,
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
        } catch (_: Exception) {
            return buildAnnotatedString {
                val plain = stripHtml(content)
                append(plain)
                applyKeywordHighlights(this, plain, cleanKeyword, isDark)
                applyUnderlines(this, plain, underlines, isDark)
                applySelectionHighlight(this, plain, selectedRange, isDark)
            }
        }

        val plainText = spanned.toString()

        return buildAnnotatedString {
            append(plainText)

            // Map Spanned spans to Compose SpanStyles
            val spans = spanned.getSpans(0, spanned.length, Any::class.java)
            for (span in spans) {
                val start = spanned.getSpanStart(span).coerceIn(0, plainText.length)
                val end = spanned.getSpanEnd(span).coerceIn(0, plainText.length)
                if (start >= end) continue

                when (span) {
                    is StyleSpan -> {
                        when (span.style) {
                            Typeface.BOLD -> {
                                addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                            }
                            Typeface.ITALIC -> {
                                addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                            }
                            Typeface.BOLD_ITALIC -> {
                                addStyle(
                                    SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
                                    start,
                                    end
                                )
                            }
                        }
                    }
                    is RelativeSizeSpan -> {
                        // Relative size for headings: scale proportionally to baseFontSizeSp
                        val scaledSp = (baseFontSizeSp * span.sizeChange).sp
                        addStyle(
                            SpanStyle(
                                fontSize = scaledSp,
                                fontWeight = if (span.sizeChange > 1.05f) FontWeight.Bold else FontWeight.Normal
                            ),
                            start,
                            end
                        )
                    }
                    is UnderlineSpan -> {
                        addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                    }
                    is StrikethroughSpan -> {
                        addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, end)
                    }
                    is ForegroundColorSpan -> {
                        // If span specifies custom color, we preserve slight tint or adapt to dark mode
                        val c = Color(span.foregroundColor)
                        if (c.alpha > 0.1f) {
                            addStyle(SpanStyle(color = if (isDark) c.copy(alpha = 0.9f) else c), start, end)
                        }
                    }
                }
            }

            // Highlighting for search keyword, underlines, and user selection
            applyKeywordHighlights(this, plainText, cleanKeyword, isDark)
            applyUnderlines(this, plainText, underlines, isDark)
            applySelectionHighlight(this, plainText, selectedRange, isDark)
        }
    }

    private fun preprocessHtmlForSpanned(html: String): String {
        var processed = html
            .replace(Regex("""<!--[\s\S]*?-->"""), "")
            .replace(Regex("""<script[^>]*>[\s\S]*?</script>""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""<style[^>]*>[\s\S]*?</style>""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""<head[^>]*>[\s\S]*?</head>""", RegexOption.IGNORE_CASE), "")

        // Normalize heading tags with relative font sizes if needed
        processed = processed.replace(Regex("""<h1[^>]*>([\s\S]*?)</h1>""", RegexOption.IGNORE_CASE)) {
            "<p><b><font size=\"6\">${it.groupValues[1]}</font></b></p>"
        }
        processed = processed.replace(Regex("""<h2[^>]*>([\s\S]*?)</h2>""", RegexOption.IGNORE_CASE)) {
            "<p><b><font size=\"5\">${it.groupValues[1]}</font></b></p>"
        }
        processed = processed.replace(Regex("""<h3[^>]*>([\s\S]*?)</h3>""", RegexOption.IGNORE_CASE)) {
            "<p><b><font size=\"4\">${it.groupValues[1]}</font></b></p>"
        }
        processed = processed.replace(Regex("""<h[4-6][^>]*>([\s\S]*?)</h[4-6]>""", RegexOption.IGNORE_CASE)) {
            "<p><b>${it.groupValues[1]}</b></p>"
        }

        // Support inline styles: font-weight: bold -> <b>
        processed = processed.replace(Regex("""<span[^>]*style=["'][^"']*(?:font-weight:\s*(?:bold|[6-9]00))[^"']*["'][^>]*>([\s\S]*?)</span>""", RegexOption.IGNORE_CASE)) {
            "<b>${it.groupValues[1]}</b>"
        }
        // Support inline styles: font-style: italic -> <i>
        processed = processed.replace(Regex("""<span[^>]*style=["'][^"']*(?:font-style:\s*italic)[^"']*["'][^>]*>([\s\S]*?)</span>""", RegexOption.IGNORE_CASE)) {
            "<i>${it.groupValues[1]}</i>"
        }

        return processed
    }

    private fun applyKeywordHighlights(
        builder: AnnotatedString.Builder,
        text: String,
        keyword: String,
        isDark: Boolean
    ) {
        if (keyword.isBlank()) return
        val lowerText = text.lowercase()
        val lowerKey = keyword.lowercase()
        var startIndex = 0

        val highlightSpan = SpanStyle(
            background = if (isDark) Color(0xFFFFB300).copy(alpha = 0.60f) else Color(0xFFFFC107).copy(alpha = 0.70f),
            color = if (isDark) Color(0xFF1E1400) else Color(0xFF2E1700),
            fontWeight = FontWeight.ExtraBold
        )

        while (startIndex < text.length) {
            val index = lowerText.indexOf(lowerKey, startIndex)
            if (index == -1) break
            builder.addStyle(
                style = highlightSpan,
                start = index,
                end = index + keyword.length
            )
            startIndex = index + keyword.length
        }
    }

    private fun applyUnderlines(
        builder: AnnotatedString.Builder,
        text: String,
        underlines: List<String>,
        isDark: Boolean
    ) {
        if (underlines.isEmpty()) return
        val underlineSpan = SpanStyle(
            textDecoration = TextDecoration.Underline,
            color = if (isDark) Color(0xFFFFD54F) else Color(0xFFD97706),
            fontWeight = FontWeight.Medium
        )
        for (u in underlines) {
            val trimmed = u.trim()
            if (trimmed.length < 2) continue
            var startIndex = 0
            while (startIndex < text.length) {
                val idx = text.indexOf(trimmed, startIndex)
                if (idx == -1) break
                builder.addStyle(underlineSpan, idx, idx + trimmed.length)
                startIndex = idx + trimmed.length
            }
        }
    }

    private fun applySelectionHighlight(
        builder: AnnotatedString.Builder,
        text: String,
        selectedRange: IntRange?,
        isDark: Boolean
    ) {
        if (selectedRange == null) return
        val start = selectedRange.first.coerceIn(0, text.length)
        val end = (selectedRange.last + 1).coerceIn(0, text.length)
        if (start < end) {
            builder.addStyle(
                SpanStyle(
                    // 用户要求：选择文字的高亮颜色不要黄色要浅绿
                    background = if (isDark) Color(0x664CAF50) else Color(0x5581C784),
                    fontWeight = FontWeight.SemiBold
                ),
                start,
                end
            )
        }
    }
}
