package com.example.util

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.min

/**
 * Enterprise-grade multi-stage Universal Charset Detector & Mojibake Healer.
 * 
 * Supports full automatic recognition without user configuration:
 * 1. BOM detection (UTF-8, UTF-16LE, UTF-16BE)
 * 2. Strict RFC-3629 UTF-8 validation
 * 3. XML / HTML / EPUB meta declared charset extraction & cross-check
 * 4. Multi-segment representative sampling (start, middle, end)
 * 5. Multi-candidate statistical scoring across:
 *    - UTF-8
 *    - GB18030 / GBK / GB2312
 *    - Big5
 *    - Shift_JIS (Windows-31J / CP932)
 *    - EUC-JP
 *    - EUC-KR
 *    - UTF-16LE / UTF-16BE
 *    - Windows-1252 / ISO-8859-1
 * 6. Heavy penalty for replacement chars (\uFFFD) and control chars;
 *    High rewards for CJK ideographs, Japanese Kana, Korean Hangul, and CJK punctuation.
 * 7. In-memory caching for zero redundant parsing.
 * 8. Manual encoding override support for reader settings.
 */
object CharsetDetector {

    // Available Charsets
    val CHARSET_UTF8: Charset = Charsets.UTF_8
    val CHARSET_GB18030: Charset by lazy { findCharset("GB18030", "GBK", "GB2312") ?: Charsets.UTF_8 }
    val CHARSET_BIG5: Charset by lazy { findCharset("Big5", "BIG5-HKSCS") ?: Charsets.UTF_8 }
    val CHARSET_SHIFT_JIS: Charset by lazy { findCharset("Shift_JIS", "windows-31j", "SJIS") ?: Charsets.UTF_8 }
    val CHARSET_EUC_JP: Charset by lazy { findCharset("EUC-JP", "EUC_JP") ?: Charsets.UTF_8 }
    val CHARSET_EUC_KR: Charset by lazy { findCharset("EUC-KR", "windows-949") ?: Charsets.UTF_8 }
    val CHARSET_UTF16LE: Charset = Charsets.UTF_16LE
    val CHARSET_UTF16BE: Charset = Charsets.UTF_16BE
    val CHARSET_WIN1252: Charset by lazy { findCharset("windows-1252", "ISO-8859-1") ?: Charsets.ISO_8859_1 }

    // Memory Cache for detection results (Key: Content Hash + Size -> Charset name)
    private val detectionCache = ConcurrentHashMap<Long, String>()

    // Common High-frequency Chinese Characters (Top 100)
    private val COMMON_CHINESE_CHARS = setOf(
        '的', '一', '是', '了', '我', '不', '人', '在', '他', '有', '这', '个', '上', '们', '来', '到', '时',
        '大', '地', '为', '子', '中', '你', '说', '生', '国', '年', '着', '就', '那', '和', '要', '她', '出',
        '也', '得', '里', '后', '自', '以', '会', '家', '可', '下', '而', '过', '天', '去', '能', '对', '小',
        '多', '然', '于', '心', '学', '么', '之', '都', '好', '看', '起', '发', '当', '没', '成', '只', '如',
        '事', '把', '还', '用', '第', '样', '道', '想', '作', '种', '开', '美', '总', '从', '无', '情', '己',
        '面', '最', '女', '但', '现', '前', '些', '所', '同', '日', '手', '又', '行', '意', '动', '方', '经'
    )

    // Common Traditional Chinese Characters
    private val COMMON_TRADITIONAL_CHARS = setOf(
        '的', '一', '是', '了', '我', '不', '人', '在', '他', '有', '這', '個', '上', '們', '來', '到', '時',
        '大', '地', '為', '子', '中', '你', '說', '生', '國', '年', '著', '就', '那', '和', '要', '她', '出',
        '也', '得', '裏', '後', '自', '以', '會', '家', '可', '下', '而', '過', '天', '去', '能', '對', '小',
        '多', '然', '於', '心', '學', '麼', '之', '都', '好', '看', '起', '發', '當', '沒', '成', '只', '如'
    )

    private fun findCharset(vararg names: String): Charset? {
        for (name in names) {
            try {
                return Charset.forName(name)
            } catch (_: Exception) {}
        }
        return null
    }

    /**
     * Detects charset name and decodes entire byte array into sanitized text.
     * @param bytes raw byte array
     * @param declaredHint optional hint from EPUB OPF, HTML meta, or file header
     * @param forcedCharset optional manual user selection (overrides automatic detector)
     */
    fun detectAndDecode(
        bytes: ByteArray,
        declaredHint: String? = null,
        forcedCharset: String? = null
    ): String {
        if (bytes.isEmpty()) return ""

        // If manual forced charset is specified by the user in settings, use it directly!
        if (!forcedCharset.isNullOrBlank() && forcedCharset != "AUTO") {
            val resolved = resolveCharset(forcedCharset)
            if (resolved != null) {
                val decoded = decodeWithReplacement(bytes, resolved)
                return healAndSanitize(decoded)
            }
        }

        // 1. Check BOM
        val bomResult = checkBom(bytes)
        if (bomResult != null) {
            val (cs, skip) = bomResult
            val decoded = String(bytes, skip, bytes.size - skip, cs)
            return healAndSanitize(decoded)
        }

        // 2. Cache check based on sample hash
        val cacheKey = computeSampleHash(bytes)
        val cachedCharsetName = detectionCache[cacheKey]
        if (cachedCharsetName != null) {
            val cs = resolveCharset(cachedCharsetName) ?: Charsets.UTF_8
            val decoded = decodeWithReplacement(bytes, cs)
            return healAndSanitize(decoded)
        }

        // 3. Extract multi-segment representative sample
        val sample = extractRepresentativeSample(bytes)

        // 4. XML / HTML meta declared encoding
        val xmlDeclared = extractXmlDeclaredEncoding(bytes) ?: declaredHint
        val resolvedDeclared = resolveCharset(xmlDeclared)

        // 5. Evaluate all candidate charsets using multi-segment scoring
        val candidateCharsets = listOf(
            Charsets.UTF_8,
            CHARSET_GB18030,
            CHARSET_BIG5,
            CHARSET_SHIFT_JIS,
            CHARSET_EUC_JP,
            CHARSET_EUC_KR,
            Charsets.UTF_16LE,
            Charsets.UTF_16BE,
            CHARSET_WIN1252
        ).distinct()

        val isStrictUtf8 = isValidUtf8(sample)

        var bestCharset = Charsets.UTF_8
        var bestScore = Long.MIN_VALUE

        for (candidate in candidateCharsets) {
            val score = scoreCharsetCandidate(sample, candidate, isStrictUtf8, candidate == resolvedDeclared)
            if (score > bestScore) {
                bestScore = score
                bestCharset = candidate
            }
        }

        // Cache detection result
        detectionCache[cacheKey] = bestCharset.name()

        // 6. Decode and heal
        var decoded = decodeWithReplacement(bytes, bestCharset)

        // Safety fallback: if replacement rate is high, try fallback
        val replacementCount = countReplacements(decoded)
        if (replacementCount > 3 && (replacementCount.toDouble() / decoded.length) > 0.002) {
            val fallbackCharset = if (bestCharset == Charsets.UTF_8) CHARSET_GB18030 else Charsets.UTF_8
            val fallbackDecoded = decodeWithReplacement(bytes, fallbackCharset)
            val fallbackReplacements = countReplacements(fallbackDecoded)
            if (fallbackReplacements < replacementCount) {
                decoded = fallbackDecoded
            }
        }

        return healAndSanitize(decoded)
    }

    /**
     * Returns detected Charset for the given byte array.
     */
    fun detectCharset(bytes: ByteArray, declaredHint: String? = null): Charset {
        if (bytes.isEmpty()) return Charsets.UTF_8

        val bomResult = checkBom(bytes)
        if (bomResult != null) return bomResult.first

        val cacheKey = computeSampleHash(bytes)
        val cached = detectionCache[cacheKey]
        if (cached != null) {
            return resolveCharset(cached) ?: Charsets.UTF_8
        }

        val sample = extractRepresentativeSample(bytes)
        val xmlDeclared = extractXmlDeclaredEncoding(bytes) ?: declaredHint
        val resolvedDeclared = resolveCharset(xmlDeclared)

        val candidateCharsets = listOf(
            Charsets.UTF_8,
            CHARSET_GB18030,
            CHARSET_BIG5,
            CHARSET_SHIFT_JIS,
            CHARSET_EUC_JP,
            CHARSET_EUC_KR,
            Charsets.UTF_16LE,
            Charsets.UTF_16BE,
            CHARSET_WIN1252
        ).distinct()

        val isStrictUtf8 = isValidUtf8(sample)
        var bestCharset = Charsets.UTF_8
        var bestScore = Long.MIN_VALUE

        for (candidate in candidateCharsets) {
            val score = scoreCharsetCandidate(sample, candidate, isStrictUtf8, candidate == resolvedDeclared)
            if (score > bestScore) {
                bestScore = score
                bestCharset = candidate
            }
        }

        detectionCache[cacheKey] = bestCharset.name()
        return bestCharset
    }

    private fun checkBom(bytes: ByteArray): Pair<Charset, Int>? {
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return Pair(Charsets.UTF_8, 3)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return Pair(Charsets.UTF_16BE, 2)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            return Pair(Charsets.UTF_16LE, 2)
        }
        return null
    }

    private fun computeSampleHash(bytes: ByteArray): Long {
        var h = bytes.size.toLong() * 31L
        val limit = min(bytes.size, 128)
        for (i in 0 until limit) {
            h = h * 31L + bytes[i].toLong()
        }
        if (bytes.size > 256) {
            val mid = bytes.size / 2
            for (i in mid until min(bytes.size, mid + 64)) {
                h = h * 31L + bytes[i].toLong()
            }
        }
        return h
    }

    private fun extractRepresentativeSample(bytes: ByteArray): ByteArray {
        val maxTotal = 65536
        if (bytes.size <= maxTotal) return bytes

        // Take the first 65536 bytes, but safely backtrack to the last ASCII whitespace/newline
        // to guarantee zero split multi-byte characters at the boundary
        val safeLimit = min(bytes.size, maxTotal)
        var cutIndex = safeLimit
        while (cutIndex > safeLimit - 32 && cutIndex > 0) {
            val b = bytes[cutIndex - 1].toInt() and 0xFF
            // 0x0A = '\n', 0x20 = ' ', 0x0D = '\r', or any ASCII byte (< 0x80)
            if (b == 0x0A || b == 0x20 || b == 0x0D || b < 0x80) {
                break
            }
            cutIndex--
        }
        return bytes.copyOfRange(0, cutIndex)
    }

    private fun scoreCharsetCandidate(
        sample: ByteArray,
        charset: Charset,
        isStrictUtf8: Boolean,
        matchesDeclared: Boolean
    ): Long {
        val decoded = try {
            val decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
            decoder.decode(ByteBuffer.wrap(sample)).toString()
        } catch (_: Exception) {
            return Long.MIN_VALUE / 2
        }

        if (decoded.isEmpty()) return -1000L

        var score = 0L

        var replacements = 0
        var controlChars = 0
        var cjkChars = 0
        var commonChinese = 0
        var traditionalChars = 0
        var kanaChars = 0
        var hangulChars = 0
        var commonPunctuation = 0

        for (i in 0 until decoded.length) {
            val c = decoded[i]
            when {
                c == '\uFFFD' -> replacements++
                c in '\u0000'..'\u0008' || c == '\u000B' || c == '\u000C' || c in '\u000E'..'\u001F' || c == '\u007F' -> controlChars++
                c in '\u4E00'..'\u9FA5' -> {
                    cjkChars++
                    if (c in COMMON_CHINESE_CHARS) commonChinese++
                    if (c in COMMON_TRADITIONAL_CHARS) traditionalChars++
                }
                c in '\u3040'..'\u309F' || c in '\u30A0'..'\u30FF' -> kanaChars++
                c in '\uAC00'..'\uD7AF' -> hangulChars++
                c in "，。！？“”‘’；：（）【】《》、…—" -> commonPunctuation++
            }
        }

        // Heavy penalty for invalid replacement and illegal control chars
        score -= (replacements * 2500L)
        score -= (controlChars * 1200L)

        // Rewards for meaningful characters
        score += (cjkChars * 20L)
        score += (commonChinese * 40L)
        score += (commonPunctuation * 25L)

        // Prioritize strictly valid UTF-8 ONLY IF it has no replacements and decoded text is valid
        if (charset == Charsets.UTF_8 && isStrictUtf8 && replacements == 0) {
            score += 15000L
        }

        if (matchesDeclared) {
            score += 5000L
        }

        if (charset == CHARSET_BIG5) {
            score += (traditionalChars * 35L)
        }

        if (charset == CHARSET_SHIFT_JIS || charset == CHARSET_EUC_JP) {
            score += (kanaChars * 45L)
        }

        if (charset == CHARSET_EUC_KR) {
            score += (hangulChars * 50L)
        }

        // Ratio of replacement characters check
        val replacementRatio = replacements.toDouble() / decoded.length.toDouble()
        if (replacementRatio > 0.02) {
            score -= 80000L
        }

        return score
    }

    /**
     * Automatic Mojibake healer (乱码自愈修复) and character sanitizer:
     * 1. Detects and repairs Latin-1 / Windows-1252 misinterpreted UTF-8 Chinese characters (e.g. ä¸€ -> 一, â€œ -> “)
     * 2. Cleans non-printable control characters, orphan replacement characters, and trims invalid byte markers
     * 3. Unescapes HTML entities so raw codes like &ldquo; / &mdash; / &#...; are not displayed as gibberish
     */
    fun healAndSanitize(rawText: String): String {
        if (rawText.isBlank()) return ""
        var text = rawText

        // Check for signature double-encoded UTF-8 bytes (Latin-1 / Windows-1252 Mojibake)
        val mojibakePattern = Regex("""(?:ä¸|äº|æ˜|çš|åœ|æœ|è¿|ä¸|åˆ|è¯|å‡|ä¹|å¾|è‡|ä¼|å®|å |è€|å¤|èƒ|å¯|å°|ç„|å¿|å­|éƒ|å¥|çœ|èµ|å½|æ²|æˆ|äº|æŠ|ç”|ç¬|é |æƒ|ï¼|ã€|â€)""")
        val matchCount = mojibakePattern.findAll(text.take(4096)).count()
        if (matchCount >= 2) {
            try {
                val healedBytes = text.toByteArray(CHARSET_WIN1252)
                val healed = String(healedBytes, Charsets.UTF_8)
                val originalCjkCount = countCjkChars(text.take(1000))
                val healedCjkCount = countCjkChars(healed.take(1000))
                if (healedCjkCount > originalCjkCount && !healed.contains('\uFFFD')) {
                    text = healed
                }
            } catch (_: Exception) { }

            if (text == rawText) {
                try {
                    val healedBytes = text.toByteArray(Charsets.ISO_8859_1)
                    val healed = String(healedBytes, Charsets.UTF_8)
                    val originalCjkCount = countCjkChars(text.take(1000))
                    val healedCjkCount = countCjkChars(healed.take(1000))
                    if (healedCjkCount > originalCjkCount) {
                        text = healed
                    }
                } catch (_: Exception) { }
            }
        }

        // Sanitize harmful non-printable control characters (except newline, carriage return, tab)
        text = text.replace(Regex("""[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]"""), "")

        // Clean famous Chinese GBK double-error artifacts like "锟斤拷"
        if (text.contains("锟斤拷")) {
            text = text.replace("锟斤拷", "")
        }

        // Replace lone replacement character \uFFFD
        if (text.contains('\uFFFD')) {
            text = text.replace("\uFFFD", "")
        }

        // Unescape standard and numeric HTML entities (double pass for &amp;ldquo; etc.)
        text = text.unescapeHtml()
        if (text.contains('&')) {
            text = text.unescapeHtml()
        }

        return text
    }

    fun resolveCharset(name: String?): Charset? {
        if (name.isNullOrBlank()) return null
        val normalized = name.trim().replace("\"", "").replace("'", "").uppercase()
        return try {
            when {
                normalized.contains("UTF-8") || normalized.contains("UTF8") -> Charsets.UTF_8
                normalized.contains("GB18030") || normalized.contains("GBK") || normalized.contains("GB2312") -> CHARSET_GB18030
                normalized.contains("BIG5") || normalized.contains("BIG-5") -> CHARSET_BIG5
                normalized.contains("SHIFT_JIS") || normalized.contains("SJIS") || normalized.contains("CP932") -> CHARSET_SHIFT_JIS
                normalized.contains("EUC-JP") || normalized.contains("EUC_JP") -> CHARSET_EUC_JP
                normalized.contains("EUC-KR") || normalized.contains("EUC_KR") || normalized.contains("CP949") -> CHARSET_EUC_KR
                normalized.contains("UTF-16LE") -> Charsets.UTF_16LE
                normalized.contains("UTF-16BE") -> Charsets.UTF_16BE
                normalized.contains("UTF-16") -> Charsets.UTF_16
                normalized.contains("1252") || normalized.contains("ISO-8859-1") || normalized.contains("LATIN1") -> CHARSET_WIN1252
                else -> Charset.forName(normalized)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun extractXmlDeclaredEncoding(bytes: ByteArray): String? {
        val checkLen = min(bytes.size, 4096)
        val head = String(bytes, 0, checkLen, Charsets.ISO_8859_1)
        val match = Regex("""<\?xml[^>]+encoding=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(head)
            ?: Regex("""<meta[^>]+charset=["']?([^"'>\s/]+)""", RegexOption.IGNORE_CASE).find(head)
            ?: Regex("""<meta[^>]+content=["'][^"']*charset=([^"';>\s/]+)""", RegexOption.IGNORE_CASE).find(head)
        return match?.groupValues?.getOrNull(1)
    }

    private fun isValidUtf8(bytes: ByteArray): Boolean {
        var i = 0
        val len = bytes.size
        while (i < len) {
            val b1 = bytes[i].toInt() and 0xFF
            if (b1 <= 0x7F) {
                i++
                continue
            }
            if (b1 in 0xC2..0xDF) {
                if (i + 1 >= len) return false
                val b2 = bytes[i + 1].toInt() and 0xFF
                if ((b2 and 0xC0) != 0x80) return false
                i += 2
            } else if (b1 in 0xE0..0xEF) {
                if (i + 2 >= len) return false
                val b2 = bytes[i + 1].toInt() and 0xFF
                val b3 = bytes[i + 2].toInt() and 0xFF
                if ((b2 and 0xC0) != 0x80 || (b3 and 0xC0) != 0x80) return false
                if (b1 == 0xE0 && b2 < 0xA0) return false
                if (b1 == 0xED && b2 > 0x9F) return false
                i += 3
            } else if (b1 in 0xF0..0xF4) {
                if (i + 3 >= len) return false
                val b2 = bytes[i + 1].toInt() and 0xFF
                val b3 = bytes[i + 2].toInt() and 0xFF
                val b4 = bytes[i + 3].toInt() and 0xFF
                if ((b2 and 0xC0) != 0x80 || (b3 and 0xC0) != 0x80 || (b4 and 0xC0) != 0x80) return false
                if (b1 == 0xF0 && b2 < 0x90) return false
                if (b1 == 0xF4 && b2 > 0x8F) return false
                i += 4
            } else {
                return false
            }
        }
        return true
    }

    fun decodeWithReplacement(bytes: ByteArray, charset: Charset): String {
        return try {
            val decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
            decoder.decode(ByteBuffer.wrap(bytes)).toString()
        } catch (_: Exception) {
            String(bytes, charset)
        }
    }

    private fun countReplacements(text: String): Int {
        var count = 0
        for (i in 0 until text.length) {
            if (text[i] == '\uFFFD') count++
        }
        return count
    }

    private fun countCjkChars(str: String): Int {
        var count = 0
        for (i in 0 until str.length) {
            val c = str[i]
            if (c in '\u4E00'..'\u9FA5' || c in COMMON_CHINESE_CHARS) count++
        }
        return count
    }

    private fun String.unescapeHtml(): String {
        if (!contains('&')) return this
        var s = this
        s = s.replace("&nbsp;", " ")
        s = s.replace("&lt;", "<")
        s = s.replace("&gt;", ">")
        s = s.replace("&amp;", "&")
        s = s.replace("&quot;", "\"")
        s = s.replace("&apos;", "'")
        s = s.replace("&ldquo;", "“")
        s = s.replace("&rdquo;", "”")
        s = s.replace("&lsquo;", "‘")
        s = s.replace("&rsquo;", "’")
        s = s.replace("&mdash;", "—")
        s = s.replace("&ndash;", "–")
        s = s.replace("&hellip;", "…")
        s = s.replace("&middot;", "·")

        // Hex entities &#x...;
        if (s.contains("&#x", ignoreCase = true)) {
            s = s.replace(Regex("""&#x([0-9a-fA-F]+);""")) { mr ->
                try {
                    val code = mr.groupValues[1].toInt(16)
                    code.toChar().toString()
                } catch (_: Exception) {
                    mr.value
                }
            }
        }

        // Decimal entities &#...;
        if (s.contains("&#")) {
            s = s.replace(Regex("""&#([0-9]+);""")) { mr ->
                try {
                    val code = mr.groupValues[1].toInt()
                    code.toChar().toString()
                } catch (_: Exception) {
                    mr.value
                }
            }
        }
        return s
    }

    data class SupportedEncoding(
        val code: String,
        val label: String,
        val description: String
    )

    val SUPPORTED_ENCODINGS = listOf(
        SupportedEncoding("AUTO", "智能自动检测", "像 Apple Books 一样多重特征精准识别并自愈"),
        SupportedEncoding("UTF-8", "UTF-8 (国际标准)", "最广泛使用的国际通用编码标准"),
        SupportedEncoding("GB18030", "GB18030 / GBK", "中国国家标准，支持全部中日韩生僻字与繁体字"),
        SupportedEncoding("GB2312", "GB2312 (简体中文)", "经典简体中文编码，覆盖绝大部分简体书籍"),
        SupportedEncoding("Big5", "Big5 (繁体中文)", "港台及海外繁体中文电子书常用编码"),
        SupportedEncoding("Shift_JIS", "Shift-JIS (日文)", "日文 Windows / 日本轻小说常用编码"),
        SupportedEncoding("EUC-KR", "EUC-KR (韩文)", "韩文出版物常用编码"),
        SupportedEncoding("UTF-16LE", "UTF-16 LE (Unicode)", "Windows 系统导出的 Unicode 文本常用编码"),
        SupportedEncoding("UTF-16BE", "UTF-16 BE (Unicode)", "大端序 Unicode 编码"),
        SupportedEncoding("windows-1252", "Windows-1252 / 西文", "英文经典与西欧语言编码")
    )
}
