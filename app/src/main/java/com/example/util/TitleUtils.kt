package com.example.util

/**
 * 清除书名中的标点符号与括号（如《》、（）、【】、( )等），让读者更直观清晰地查找与阅读书籍
 */
fun cleanBookTitle(rawTitle: String): String {
    if (rawTitle.isBlank()) return ""
    // 去除各种书名号、中英文括号、中英文方括号、各种标点符号、文件后缀及首尾多余空格
    val cleaned = rawTitle
        .replace(Regex("""\.(txt|epub|docx|pdf|mobi)$""", RegexOption.IGNORE_CASE), "")
        .replace(Regex("[《》()（）\\[\\]【】「」『』〈〉〔〕\\{\\}‹›\"'“”‘’.,!?;:，。？！；：]"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
    return if (cleaned.isNotEmpty()) cleaned else rawTitle.trim()
}

