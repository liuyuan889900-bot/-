package com.example.ui.reader

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.TextLayoutResult
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Ultra-precise text selection engine.
 * 
 * 1. Line Hit Detection: Maps touch Y to the unique text line using line bounding boxes
 *    with half line-spacing tolerance. Supports upward reverse dragging and exact single-line stopping.
 *    (解决滑到第二行停不住直接跳到第三行的问题：采用精确行框与严密的半行距区间匹配，绝不跨行乱跳).
 * 2. Character Hit Detection: Clamps to line start and visible end without skipping characters.
 * 3. Finger Offset: Offsets handle drag Y upwards by 36px so fingers do not obscure text.
 * 4. Word / Sentence Bounds for standard Android-style Long Press text selection.
 */
object SelectionEngine {

    /**
     * Finds the matching text line index based on Y coordinate with half-line-spacing boundary.
     * Prevents skipping lines when dragging vertically.
     */
    fun findLineForY(lr: TextLayoutResult, targetY: Float): Int {
        val count = lr.lineCount
        if (count <= 1) return 0

        var bestLine = 0
        var minDist = Float.MAX_VALUE

        for (i in 0 until count) {
            val lineTop = lr.getLineTop(i)
            val lineBottom = lr.getLineBottom(i)
            val prevBottom = if (i > 0) lr.getLineBottom(i - 1) else (lineTop - 20f)
            val nextTop = if (i < count - 1) lr.getLineTop(i + 1) else (lineBottom + 20f)

            // Line box with half line-spacing boundary
            val boundTop = (prevBottom + lineTop) / 2f
            val boundBottom = (lineBottom + nextTop) / 2f

            if (targetY in boundTop..boundBottom) {
                return i
            }

            val lineCenter = (lineTop + lineBottom) / 2f
            val dist = abs(targetY - lineCenter)
            if (dist < minDist) {
                minDist = dist
                bestLine = i
            }
        }
        return bestLine.coerceIn(0, count - 1)
    }

    /**
     * Resolves precise character target offset for touches or dragging selection handles.
     * Accurately lands on individual characters and single lines.
     */
    fun resolveExactCharOffset(
        lr: TextLayoutResult,
        content: String,
        touchPos: Offset,
        isEndHandle: Boolean = false,
        applyFingerOffset: Boolean = false,
        fingerOffsetY: Float = 36f
    ): Int {
        val contentLength = content.length
        if (contentLength <= 0 || lr.lineCount == 0) return 0

        val effectiveY = if (applyFingerOffset) touchPos.y - fingerOffsetY else touchPos.y
        val effectiveX = touchPos.x

        // 1. Find exact text line by half line-spacing boundary
        val line = findLineForY(lr, effectiveY)
        val lineStart = lr.getLineStart(line).coerceIn(0, contentLength - 1)
        val lineVisibleEnd = lr.getLineEnd(line, visibleEnd = true).coerceIn(lineStart, contentLength)
        val lineLastCharIndex = (lineVisibleEnd - 1).coerceAtLeast(lineStart)

        val lineLeft = lr.getLineLeft(line)
        val lineRight = lr.getLineRight(line)

        // 2. Fast boundary snapping for line edges
        if (effectiveX <= lineLeft + 4f) {
            return lineStart
        }
        if (effectiveX >= lineRight - 4f) {
            return if (isEndHandle) lineLastCharIndex else lineStart
        }

        // 3. Character-level hit test inside the line
        val lineCenterY = (lr.getLineTop(line) + lr.getLineBottom(line)) / 2f
        val offset = lr.getOffsetForPosition(Offset(effectiveX, lineCenterY))

        return if (isEndHandle) {
            val c = offset.coerceIn(lineStart, lineVisibleEnd)
            if (c > lineLastCharIndex) {
                lineLastCharIndex
            } else {
                val box = lr.getBoundingBox(c.coerceIn(0, contentLength - 1))
                if (effectiveX >= box.left + box.width * 0.35f) {
                    c
                } else {
                    (c - 1).coerceAtLeast(lineStart)
                }
            }
        } else {
            val c = offset.coerceIn(lineStart, lineLastCharIndex)
            val box = lr.getBoundingBox(c.coerceIn(0, contentLength - 1))
            if (effectiveX <= box.left + box.width * 0.65f) {
                c
            } else {
                (c + 1).coerceAtMost(lineLastCharIndex)
            }
        }
    }

    /**
     * Resolves precise word / short phrase bounds at charIndex for standard long-press selection.
     */
    fun findWordOrPunctuationBounds(text: String, charIndex: Int): IntRange {
        if (text.isBlank() || charIndex !in text.indices) return 0..0

        val punctuation = setOf(
            '。', '，', '！', '？', '；', '：', '“', '”', '‘', '’', '（', '）',
            '【', '】', '《', '》', '、', '\n', '\r', ' ', '\t',
            '.', ',', '!', '?', ';', ':', '"', '\'', '(', ')', '[', ']'
        )

        val char = text[charIndex]
        if (char in punctuation) {
            return charIndex..charIndex
        }

        // Expand left
        var start = charIndex
        while (start > 0 && text[start - 1] !in punctuation && (charIndex - start) < 6) {
            start--
        }

        // Expand right
        var end = charIndex
        while (end < text.length - 1 && text[end + 1] !in punctuation && (end - charIndex) < 6) {
            end++
        }

        return start.coerceIn(0, text.length - 1)..end.coerceIn(0, text.length - 1)
    }
}
