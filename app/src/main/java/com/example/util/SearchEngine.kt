package com.example.util

import com.example.data.dao.ChapterWithBookInfo
import com.example.data.repository.SearchResultSnippet
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * High-Performance Intelligent Search Engine
 * 
 * Supports:
 * 1. Long continuous text / paragraph search (resolves newline, whitespace, and punctuation mismatch)
 * 2. Segment and proximity multi-clause matching for paragraphs up to 500+ characters
 * 3. Fuzzy search tolerating typos, homophones, and minor omissions
 * 4. Rich context snippet generation with exact character offset and page estimations
 * 5. Intelligent TOC (Table of Contents) title fuzzy and subsequence search
 */
object SearchEngine {

    // Common Chinese homophones, near-synonyms, and frequently confused characters
    private val HOMOPHONE_GROUPS = listOf(
        setOf('的', '得', '地'),
        setOf('在', '再'),
        setOf('作', '做'),
        setOf('已', '己', '巳'),
        setOf('需', '须'),
        setOf('辨', '辩', '辫'),
        setOf('像', '相', '向'),
        setOf('炼', '练'),
        setOf('复', '覆'),
        setOf('恒', '衡'),
        setOf('盘', '磐'),
        setOf('度', '渡'),
        setOf('定', '订'),
        setOf('唯', '惟'),
        setOf('渔', '鱼'),
        setOf('彩', '采'),
        setOf('座', '坐'),
        setOf('具', '俱'),
        setOf('既', '即'),
        setOf('藉', '籍'),
        setOf('弛', '驰'),
        setOf('分', '份'),
        setOf('予', '预'),
        setOf('至', '志'),
        setOf('致', '治'),
        setOf('尊', '遵'),
        setOf('宏', '弘'),
        setOf('壁', '璧'),
        setOf('决', '绝'),
        setOf('记', '纪'),
        setOf('攻', '工', '公'),
        setOf('悟', '物', '务'),
        // Spiritual, religious, and philosophical typos
        setOf('圣', '胜', '盛', '生', '升'),
        setOf('经', '精', '金', '晶', '惊', '京', '井'),
        setOf('神', '深', '申', '沈'),
        setOf('言', '严', '研', '延'),
        setOf('道', '到', '倒'),
        setOf('德', '得'),
        setOf('真', '箴', '甄'),
        setOf('理', '礼', '李', '里'),
        setOf('心', '辛', '新'),
        setOf('天', '添', '田'),
        setOf('地', '第', '弟'),
        setOf('佛', '弗', '福'),
        setOf('爱', '艾', '碍'),
        // Book-specific character typos
        setOf('查', '察', '茶'),
        setOf('拉', '腊', '辣'),
        setOf('图', '途', '屠'),
        setOf('斯', '司', '丝', '思'),
        setOf('特', '忒'),
        setOf('瓦', '袜'),
        setOf('尔', '二', '耳', '儿'),
        setOf('登', '灯'),
        setOf('湖', '胡', '壶', '蝴'),
        setOf('歌', '各', '阁', '格'),
        setOf('本', '奔'),
        setOf('集', '极', '急', '及', '季')
    )

    // Semantic conceptual synonyms map for intent search
    private val SEMANTIC_SYNONYMS = mapOf(
        "圣经" to listOf("经书", "圣言", "经典", "神圣", "上帝", "真理", "福音", "先知", "基督", "教义"),
        "胜经" to listOf("圣经", "经书", "圣言", "经典", "神圣", "上帝", "真理"),
        "生经" to listOf("圣经", "经书", "圣言", "经典", "神圣", "上帝", "真理"),
        "盛经" to listOf("圣经", "经书", "圣言", "经典", "神圣", "上帝", "真理"),
        "哲学" to listOf("智慧", "思想", "沉思", "真理", "超人", "精神", "尼采"),
        "上帝" to listOf("神", "主", "造物主", "天主", "耶和华", "圣者", "天意"),
        "超人" to listOf("查拉图斯特拉", "超越", "精神", "尼采", "意志"),
        "自然" to listOf("瓦尔登湖", "梭罗", "林中", "草木", "湖水", "天地", "宁静"),
        "散文" to listOf("文萃", "名篇", "古今", "辞赋", "散记"),
        "歌本" to listOf("乐府", "国风", "诗歌", "雅韵", "清商", "琴歌")
    )

    private val HOMOPHONE_CANONICAL_MAP: Map<Char, Char> = buildMap {
        for (group in HOMOPHONE_GROUPS) {
            val canonical = group.first()
            for (ch in group) {
                put(ch, canonical)
            }
        }
    }

    /**
     * Normalizes text for search comparisons.
     * - Replaces all forms of whitespace and line breaks with single space or collapses them.
     * - Normalizes full-width and half-width punctuation marks.
     * - Converts English to lowercase.
     */
    fun normalize(text: String, stripPunctuation: Boolean = false, applyHomophones: Boolean = false): String {
        val sb = StringBuilder(text.length)
        for (ch in text) {
            val normalizedChar = when (ch) {
                // Whitespace & newlines
                '\r', '\n', '\t', '\u3000', '\u00A0' -> ' '
                // Fullwidth punctuation mapping
                '“', '”', '‘', '’' -> '"'
                '，' -> ','
                '。' -> '.'
                '！' -> '!'
                '？' -> '?'
                '：' -> ':'
                '；' -> ';'
                '（', '【', '《', '〔' -> '('
                '）', '】', '》', '〕' -> ')'
                '—', '–' -> '-'
                else -> ch.lowercaseChar()
            }

            if (stripPunctuation && isPunctuation(normalizedChar)) {
                continue
            }

            val finalChar = if (applyHomophones) {
                HOMOPHONE_CANONICAL_MAP[normalizedChar] ?: normalizedChar
            } else {
                normalizedChar
            }

            // Collapse consecutive spaces
            if (finalChar == ' ' && sb.isNotEmpty() && sb.last() == ' ') {
                continue
            }
            sb.append(finalChar)
        }
        return sb.toString().trim()
    }

    private fun isPunctuation(c: Char): Boolean {
        return c in " \t\r\n.,;:!?\"'()[]{}-/\\~`@#$%^&*+=|<>，。！？：；（）【】《》“”‘’、—…·•"
    }

    /**
     * Searches a chapter's content with:
     * 1. Exact raw match
     * 2. Normalized continuous text match (handles newlines, differing spaces, quotes)
     * 3. Proximity multi-clause match for long paragraphs (> 15 chars)
     * 4. Fuzzy fallback match (homophones, typos, edit distance)
     */
    fun searchInChapterContent(
        bookId: Long,
        bookTitle: String,
        bookAuthor: String,
        chapterIndex: Int,
        chapterTitle: String,
        content: String,
        rawQuery: String,
        maxMatchesPerChapter: Int = 10
    ): List<SearchResultSnippet> {
        val query = rawQuery.trim()
        if (query.isEmpty() || content.isEmpty()) return emptyList()

        val results = mutableListOf<SearchResultSnippet>()
        val matchedRanges = mutableListOf<IntRange>()

        fun isOverlapping(start: Int, end: Int): Boolean {
            return matchedRanges.any { existing ->
                max(existing.first, start) <= min(existing.last, end)
            }
        }

        // ================= LEVEL 1: Exact Raw Substring Search =================
        var rawIdx = content.indexOf(query, 0, ignoreCase = true)
        var matchInChap = 0
        while (rawIdx >= 0 && results.size < maxMatchesPerChapter && matchInChap < 5) {
            val endIdx = rawIdx + query.length
            if (!isOverlapping(rawIdx, endIdx)) {
                matchedRanges.add(rawIdx..endIdx)
                val snippet = buildSnippet(content, rawIdx, endIdx)
                val estPage = estimatePageNumber(rawIdx)
                results.add(
                    SearchResultSnippet(
                        bookId = bookId,
                        bookTitle = bookTitle,
                        bookAuthor = bookAuthor,
                        chapterIndex = chapterIndex,
                        chapterTitle = chapterTitle,
                        snippetText = snippet,
                        matchCount = 1,
                        matchIndexInChapter = matchInChap,
                        charOffset = rawIdx,
                        isFuzzyMatch = false,
                        similarityScore = 1.0f,
                        matchLabel = "精确匹配",
                        estimatedPage = estPage
                    )
                )
                matchInChap++
            }
            rawIdx = content.indexOf(query, rawIdx + max(1, query.length), ignoreCase = true)
        }

        // If we found direct exact matches and query is short, return
        if (results.size >= maxMatchesPerChapter) return results

        // ================= LEVEL 2: Normalized Continuous Search (Long Sentences / Paragraphs) =================
        val normQueryNoPunct = normalize(query, stripPunctuation = true)
        if (normQueryNoPunct.length >= 4) {
            // Find match in stripped text while mapping back to original offsets
            val (strippedContent, offsetMapping) = buildStrippedIndexMap(content)
            
            var normIdx = strippedContent.indexOf(normQueryNoPunct)
            while (normIdx >= 0 && results.size < maxMatchesPerChapter && matchInChap < 5) {
                val origStart = offsetMapping.getOrElse(normIdx) { 0 }
                val origEnd = offsetMapping.getOrElse(min(normIdx + normQueryNoPunct.length, offsetMapping.size - 1)) { content.length }
                
                if (!isOverlapping(origStart, origEnd)) {
                    matchedRanges.add(origStart..origEnd)
                    val snippet = buildSnippet(content, origStart, origEnd)
                    val estPage = estimatePageNumber(origStart)
                    results.add(
                        SearchResultSnippet(
                            bookId = bookId,
                            bookTitle = bookTitle,
                            bookAuthor = bookAuthor,
                            chapterIndex = chapterIndex,
                            chapterTitle = chapterTitle,
                            snippetText = snippet,
                            matchCount = 1,
                            matchIndexInChapter = matchInChap,
                            charOffset = origStart,
                            isFuzzyMatch = false,
                            similarityScore = 1.0f,
                            matchLabel = if (normQueryNoPunct.length > 20) "长段落匹配" else "全文匹配",
                            estimatedPage = estPage
                        )
                    )
                    matchInChap++
                }
                normIdx = strippedContent.indexOf(normQueryNoPunct, normIdx + max(1, normQueryNoPunct.length))
            }
        }

        if (results.size >= maxMatchesPerChapter) return results

        // ================= LEVEL 3: Multi-Clause Proximity Match (Paragraphs >= 15 chars) =================
        if (normQueryNoPunct.length >= 15) {
            // Split long query into clauses (delimited by comma, period, question mark, semicolon)
            val rawClauses = query.split(Regex("[，。！？；\n\r,;!?]+"))
                .map { it.trim() }
                .filter { it.length >= 3 }

            val clauses = if (rawClauses.size >= 2) rawClauses else {
                // Window chunks of 10 characters
                normQueryNoPunct.chunked(12).filter { it.length >= 4 }
            }

            if (clauses.isNotEmpty()) {
                val clauseHits = mutableListOf<Pair<Int, Int>>() // list of (origStart, origEnd)
                for (clause in clauses) {
                    val cleanClause = normalize(clause, stripPunctuation = true)
                    if (cleanClause.length < 3) continue
                    val pos = content.indexOf(clause, ignoreCase = true)
                    if (pos >= 0) {
                        clauseHits.add(pos to pos + clause.length)
                    } else {
                        // check in content without spaces
                        val subPos = content.replace(" ", "").indexOf(cleanClause, ignoreCase = true)
                        if (subPos >= 0) {
                            clauseHits.add(subPos to subPos + cleanClause.length)
                        }
                    }
                }

                if (clauseHits.size >= 2 || (clauses.size == 1 && clauseHits.isNotEmpty())) {
                    val minStart = clauseHits.minOf { it.first }
                    val maxEnd = clauseHits.maxOf { it.second }
                    val span = maxEnd - minStart

                    // If all matching clauses occur within a reasonable paragraph span (< query.length * 2.5)
                    if (span <= max(query.length * 3, 500) && !isOverlapping(minStart, maxEnd)) {
                        matchedRanges.add(minStart..maxEnd)
                        val matchRatio = clauseHits.size.toFloat() / clauses.size.toFloat()
                        val snippet = buildSnippet(content, minStart, maxEnd)
                        val estPage = estimatePageNumber(minStart)
                        results.add(
                            SearchResultSnippet(
                                bookId = bookId,
                                bookTitle = bookTitle,
                                bookAuthor = bookAuthor,
                                chapterIndex = chapterIndex,
                                chapterTitle = chapterTitle,
                                snippetText = snippet,
                                matchCount = 1,
                                matchIndexInChapter = matchInChap,
                                charOffset = minStart,
                                isFuzzyMatch = matchRatio < 0.95f,
                                similarityScore = matchRatio,
                                matchLabel = "长文段落匹配 (${(matchRatio * 100).toInt()}%)",
                                estimatedPage = estPage
                            )
                        )
                        matchInChap++
                    }
                }
            }
        }

        if (results.size >= maxMatchesPerChapter) return results

        // ================= LEVEL 4: Fuzzy Search (Homophones & Minor Typos) =================
        // Run when query length is >= 2 and we haven't found sufficient exact matches
        val normQueryHomophone = normalize(query, stripPunctuation = true, applyHomophones = true)
        if (normQueryHomophone.length in 2..60) {
            val (homoContent, homoMapping) = buildStrippedIndexMap(content, applyHomophones = true)
            var homoIdx = homoContent.indexOf(normQueryHomophone)
            while (homoIdx >= 0 && results.size < maxMatchesPerChapter && matchInChap < 5) {
                val origStart = homoMapping.getOrElse(homoIdx) { 0 }
                val origEnd = homoMapping.getOrElse(min(homoIdx + normQueryHomophone.length, homoMapping.size - 1)) { content.length }

                if (!isOverlapping(origStart, origEnd)) {
                    matchedRanges.add(origStart..origEnd)
                    val snippet = buildSnippet(content, origStart, origEnd)
                    val estPage = estimatePageNumber(origStart)
                    results.add(
                        SearchResultSnippet(
                            bookId = bookId,
                            bookTitle = bookTitle,
                            bookAuthor = bookAuthor,
                            chapterIndex = chapterIndex,
                            chapterTitle = chapterTitle,
                            snippetText = snippet,
                            matchCount = 1,
                            matchIndexInChapter = matchInChap,
                            charOffset = origStart,
                            isFuzzyMatch = true,
                            similarityScore = 0.90f,
                            matchLabel = "模糊同音匹配",
                            estimatedPage = estPage
                        )
                    )
                    matchInChap++
                }
                homoIdx = homoContent.indexOf(normQueryHomophone, homoIdx + max(1, normQueryHomophone.length))
            }

            // Levenshtein sliding window on sentences for short queries (2..20 chars)
            if (results.isEmpty() && normQueryNoPunct.length in 2..20) {
                val sentences = splitIntoSentences(content)
                for ((sentence, startOffset) in sentences) {
                    if (results.size >= maxMatchesPerChapter) break
                    val cleanSent = normalize(sentence, stripPunctuation = true)
                    if (cleanSent.length < normQueryNoPunct.length) continue

                    // Slide a window of length query.length +/- 1
                    val qLen = normQueryNoPunct.length
                    for (wStart in 0..(cleanSent.length - qLen).coerceAtLeast(0)) {
                        val sub = cleanSent.substring(wStart, (wStart + qLen).coerceAtMost(cleanSent.length))
                        val distance = computeLevenshtein(normQueryNoPunct, sub)
                        val maxAllowedDist = if (qLen <= 3) 1 else if (qLen <= 6) 2 else 3
                        if (distance in 1..maxAllowedDist) {
                            val origStart = (startOffset + wStart).coerceIn(0, content.length)
                            val origEnd = (origStart + qLen).coerceIn(origStart, content.length)
                            if (!isOverlapping(origStart, origEnd)) {
                                matchedRanges.add(origStart..origEnd)
                                val sim = if (qLen <= 2) 0.85f else (1.0f - (distance.toFloat() / qLen.toFloat())).coerceIn(0.6f, 0.95f)
                                val snippet = buildSnippet(content, origStart, origEnd)
                                val estPage = estimatePageNumber(origStart)
                                results.add(
                                    SearchResultSnippet(
                                        bookId = bookId,
                                        bookTitle = bookTitle,
                                        bookAuthor = bookAuthor,
                                        chapterIndex = chapterIndex,
                                        chapterTitle = chapterTitle,
                                        snippetText = snippet,
                                        matchCount = 1,
                                        matchIndexInChapter = matchInChap,
                                        charOffset = origStart,
                                        isFuzzyMatch = true,
                                        similarityScore = sim,
                                        matchLabel = "错字容错匹配 (${(sim * 100).toInt()}%)",
                                        estimatedPage = estPage
                                    )
                                )
                                matchInChap++
                                break // only one fuzzy hit per sentence
                            }
                        }
                    }
                }
            }
        }

        // ================= LEVEL 5: Semantic Intent & Synonyms Fallback Search =================
        if (results.isEmpty()) {
            val synonyms = SEMANTIC_SYNONYMS[query] ?: SEMANTIC_SYNONYMS[normQueryNoPunct]
            if (synonyms != null) {
                for (syn in synonyms) {
                    if (results.size >= maxMatchesPerChapter) break
                    var synIdx = content.indexOf(syn, 0, ignoreCase = true)
                    while (synIdx >= 0 && results.size < maxMatchesPerChapter && matchInChap < 4) {
                        val endIdx = synIdx + syn.length
                        if (!isOverlapping(synIdx, endIdx)) {
                            matchedRanges.add(synIdx..endIdx)
                            val snippet = buildSnippet(content, synIdx, endIdx)
                            val estPage = estimatePageNumber(synIdx)
                            results.add(
                                SearchResultSnippet(
                                    bookId = bookId,
                                    bookTitle = bookTitle,
                                    bookAuthor = bookAuthor,
                                    chapterIndex = chapterIndex,
                                    chapterTitle = chapterTitle,
                                    snippetText = snippet,
                                    matchCount = 1,
                                    matchIndexInChapter = matchInChap,
                                    charOffset = synIdx,
                                    isFuzzyMatch = true,
                                    similarityScore = 0.88f,
                                    matchLabel = "语义意图关联（含“$syn”）",
                                    estimatedPage = estPage
                                )
                            )
                            matchInChap++
                        }
                        synIdx = content.indexOf(syn, synIdx + max(1, syn.length), ignoreCase = true)
                    }
                }
            }
        }

        return results
    }

    /**
     * Evaluates a chapter title against query for TOC (Table of Contents) search.
     * Returns a score between 0.0 (no match) and 1.0 (exact match).
     */
    fun matchChapterTitleScore(title: String, query: String): Float {
        val cleanTitle = title.trim()
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty() || cleanTitle.isEmpty()) return 0f

        // 1. Direct exact or substring
        if (cleanTitle.equals(cleanQuery, ignoreCase = true)) return 1.0f
        if (cleanTitle.contains(cleanQuery, ignoreCase = true)) return 0.95f

        // 2. Normalized check (ignore punctuation & spaces)
        val normTitle = normalize(cleanTitle, stripPunctuation = true)
        val normQuery = normalize(cleanQuery, stripPunctuation = true)
        if (normQuery.isNotEmpty() && normTitle.contains(normQuery)) return 0.90f

        // 3. In-order subsequence / multi-keyword match (e.g. "乃粒 稼穑" matches "第一卷 乃粒·稼穑之本")
        val rawWords = cleanQuery.split(Regex("[\\s·•,，.。!！?？:：;；/、\\-_]+"))
            .map { normalize(it, stripPunctuation = true) }
            .filter { it.isNotBlank() }
        if (rawWords.size > 1 && rawWords.all { normTitle.contains(it) }) {
            return 0.85f
        }

        // 4. Homophone match
        val homoTitle = normalize(cleanTitle, stripPunctuation = true, applyHomophones = true)
        val homoQuery = normalize(cleanQuery, stripPunctuation = true, applyHomophones = true)
        if (homoTitle.contains(homoQuery)) return 0.80f

        // 5. Semantic intent match on title
        val synList = SEMANTIC_SYNONYMS[cleanQuery] ?: SEMANTIC_SYNONYMS[normQuery]
        if (synList != null && synList.any { normTitle.contains(it) }) {
            return 0.78f
        }

        // 6. Levenshtein edit distance for typos
        if (normQuery.length >= 2 && abs(normTitle.length - normQuery.length) <= 8) {
            val dist = computeLevenshtein(normQuery, normTitle)
            val sim = 1.0f - (dist.toFloat() / max(normQuery.length, normTitle.length).toFloat())
            if (sim >= 0.50f) return sim * 0.75f
        }

        return 0f
    }

    private fun buildSnippet(content: String, startOffset: Int, endOffset: Int): String {
        val snippetRadius = 38
        val start = (startOffset - snippetRadius).coerceAtLeast(0)
        val end = (endOffset + snippetRadius).coerceAtMost(content.length)
        val prefix = if (start > 0) "..." else ""
        val suffix = if (end < content.length) "..." else ""
        return prefix + content.substring(start, end).replace(Regex("[\\r\\n\\t]+"), " ").trim() + suffix
    }

    private fun estimatePageNumber(charOffset: Int): Int {
        // Average screen page in mobile reader holds ~380 Chinese characters
        return (charOffset / 380) + 1
    }

    private fun buildStrippedIndexMap(
        content: String,
        applyHomophones: Boolean = false
    ): Pair<String, IntArray> {
        val sb = StringBuilder(content.length)
        val mapping = IntArray(content.length)
        var outIdx = 0

        for (i in content.indices) {
            val ch = content[i]
            if (!isPunctuation(ch)) {
                var c = ch.lowercaseChar()
                if (applyHomophones) {
                    c = HOMOPHONE_CANONICAL_MAP[c] ?: c
                }
                sb.append(c)
                mapping[outIdx++] = i
            }
        }
        val trimmedMapping = mapping.copyOf(outIdx)
        return sb.toString() to trimmedMapping
    }

    private fun splitIntoSentences(content: String): List<Pair<String, Int>> {
        val result = mutableListOf<Pair<String, Int>>()
        val delimiters = charArrayOf('。', '！', '？', '\n', '；')
        var start = 0
        for (i in content.indices) {
            if (content[i] in delimiters || i == content.length - 1) {
                val end = if (i == content.length - 1) content.length else i + 1
                val sentence = content.substring(start, end).trim()
                if (sentence.length >= 4) {
                    result.add(sentence to start)
                }
                start = end
            }
        }
        return result
    }

    private fun computeLevenshtein(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}
