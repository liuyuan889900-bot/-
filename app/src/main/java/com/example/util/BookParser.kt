package com.example.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.StringReader
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class ParsedChapter(
    val title: String,
    val content: String,
    val wordCount: Int
)

data class ParsedBook(
    val title: String,
    val author: String,
    val format: String,
    val intro: String,
    val chapters: List<ParsedChapter>,
    val totalWordCount: Long,
    val coverFilePath: String? = null
)

object BookParser {

    private val CHAPTER_REGEX = Regex(
        """^(第[0-9零一二三四五六七八九十百千两]+[章节卷回篇集部].*|Chapter\s+[0-9]+.*|ACT\s+[0-9IVXLCDM]+.*|序[言章]|前言|后记|引子|楔子|尾声|附录).*""",
        RegexOption.IGNORE_CASE
    )

    fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) {
                            result = cursor.getString(nameIndex)
                        }
                    }
                }
            } catch (_: Exception) { }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "本地导入书籍.txt"
    }

    fun parse(context: Context, uri: Uri, fileName: String, forcedEncoding: String? = null): ParsedBook {
        val lowerName = fileName.lowercase()
        return when {
            lowerName.endsWith(".epub") -> parseEpub(context, uri, fileName)
            lowerName.endsWith(".docx") -> parseDocx(context, uri, fileName)
            else -> parseTxt(context, uri, fileName, forcedEncoding)
        }
    }

    // ================== EPUB PARSER ==================
    private fun parseEpub(context: Context, uri: Uri, fileName: String): ParsedBook {
        var bookTitle = fileName.substringBeforeLast(".")
        var bookAuthor = "未知作者"
        var bookIntro = "导入的 EPUB 电子书《$bookTitle》"
        val htmlEntries = mutableMapOf<String, String>()
        val ncxEntries = mutableMapOf<String, String>()
        val opfEntries = mutableMapOf<String, String>()
        val spineItems = mutableListOf<String>()
        var opfDir = ""

        val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "webp", "svg")
        val bookHash = fileName.hashCode().toString()
        val epubExtractDir = java.io.File(context.filesDir, "epub_extracted/$bookHash")
        epubExtractDir.mkdirs()
        val imageFiles = mutableMapOf<String, String>()
        var detectedCoverHref: String? = null

        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                ZipInputStream(stream).use { zip ->
                    var entry: ZipEntry? = zip.nextEntry
                    while (entry != null) {
                        val name = entry.name
                        if (!entry.isDirectory) {
                            val targetFile = java.io.File(epubExtractDir, name)
                            targetFile.parentFile?.mkdirs()
                            val entryBytes = readZipEntryBytes(zip)
                            targetFile.writeBytes(entryBytes)

                            val lower = name.lowercase()
                            val ext = lower.substringAfterLast('.', "")
                            if (ext in imageExtensions) {
                                imageFiles[name] = targetFile.absolutePath
                                imageFiles[lower] = targetFile.absolutePath
                                val base = name.substringAfterLast('/')
                                imageFiles[base] = targetFile.absolutePath
                                imageFiles[base.lowercase()] = targetFile.absolutePath
                            } else if (lower.endsWith(".html") || lower.endsWith(".xhtml") || lower.endsWith(".htm")) {
                                val content = CharsetDetector.detectAndDecode(entryBytes, declaredHint = "UTF-8")
                                htmlEntries[name] = content
                            } else if (lower.endsWith(".ncx")) {
                                val content = CharsetDetector.detectAndDecode(entryBytes, declaredHint = "UTF-8")
                                ncxEntries[name] = content
                            } else if (lower.endsWith(".opf")) {
                                val opfContent = CharsetDetector.detectAndDecode(entryBytes, declaredHint = "UTF-8")
                                opfEntries[name] = opfContent
                                val lastSlash = name.lastIndexOf('/')
                                if (lastSlash != -1) {
                                    opfDir = name.substring(0, lastSlash + 1)
                                }
                            }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Parse OPF Metadata & Spine
        val manifestMap = mutableMapOf<String, OpfManifestItem>()
        for ((_, opfContent) in opfEntries) {
            parseOpfMetadata(opfContent)?.let { meta ->
                if (meta.title.isNotBlank()) bookTitle = meta.title
                if (meta.author.isNotBlank()) bookAuthor = meta.author
                if (meta.intro.isNotBlank()) bookIntro = meta.intro
                spineItems.addAll(meta.spineHrefs)
                if (meta.coverHref.isNotBlank()) detectedCoverHref = meta.coverHref
            }
            manifestMap.putAll(extractManifestFromOpf(opfContent))
        }

        // Resolve images in html entries to local file URLs
        for ((key, rawHtml) in htmlEntries.toMap()) {
            val keyDir = if (key.contains('/')) key.substringBeforeLast('/') + "/" else ""
            var resolvedHtml = rawHtml
            val imgRegex = Regex("""(<img[^>]+src=["'])([^"']+)(["'][^>]*>)""", RegexOption.IGNORE_CASE)
            resolvedHtml = imgRegex.replace(resolvedHtml) { match ->
                val p1 = match.groupValues[1]
                val src = match.groupValues[2]
                val p2 = match.groupValues[3]
                val cleanSrc = try { java.net.URLDecoder.decode(src.substringBefore('#').substringBefore('?'), "UTF-8") } catch (_: Exception) { src }
                val resolvedRel = resolveRelativePath(keyDir, cleanSrc)
                val fileNameOnly = cleanSrc.substringAfterLast('/')
                val localPath = imageFiles[resolvedRel]
                    ?: imageFiles[cleanSrc]
                    ?: imageFiles["$keyDir$cleanSrc"]
                    ?: imageFiles[fileNameOnly]
                    ?: imageFiles["$opfDir$cleanSrc"]
                    ?: imageFiles[resolvedRel.lowercase()]
                    ?: imageFiles[fileNameOnly.lowercase()]
                if (localPath != null) "${p1}file://$localPath$p2" else match.value
            }
            val imageTagRegex = Regex("""(<image[^>]+(?:xlink:href|href)=["'])([^"']+)(["'][^>]*>)""", RegexOption.IGNORE_CASE)
            resolvedHtml = imageTagRegex.replace(resolvedHtml) { match ->
                val p1 = match.groupValues[1]
                val src = match.groupValues[2]
                val p2 = match.groupValues[3]
                val cleanSrc = try { java.net.URLDecoder.decode(src.substringBefore('#').substringBefore('?'), "UTF-8") } catch (_: Exception) { src }
                val resolvedRel = resolveRelativePath(keyDir, cleanSrc)
                val fileNameOnly = cleanSrc.substringAfterLast('/')
                val localPath = imageFiles[resolvedRel]
                    ?: imageFiles[cleanSrc]
                    ?: imageFiles["$keyDir$cleanSrc"]
                    ?: imageFiles[fileNameOnly]
                    ?: imageFiles["$opfDir$cleanSrc"]
                    ?: imageFiles[resolvedRel.lowercase()]
                    ?: imageFiles[fileNameOnly.lowercase()]
                if (localPath != null) "${p1}file://$localPath$p2" else match.value
            }
            htmlEntries[key] = resolvedHtml
        }

        // Detect and persist book cover image
        var finalCoverFilePath: String? = null
        var candidateCoverPath: String? = null

        if (!detectedCoverHref.isNullOrBlank()) {
            val cleanCoverHref = detectedCoverHref!!.substringBefore('#').substringBefore('?')
            val decodedCoverHref = try { java.net.URLDecoder.decode(cleanCoverHref, "UTF-8") } catch (_: Exception) { cleanCoverHref }
            val fileNameOnly = decodedCoverHref.substringAfterLast('/')
            val resolvedRel = resolveRelativePath(opfDir, cleanCoverHref)

            candidateCoverPath = imageFiles[resolvedRel]
                ?: imageFiles[decodedCoverHref]
                ?: imageFiles["$opfDir$decodedCoverHref"]
                ?: imageFiles[cleanCoverHref]
                ?: imageFiles["$opfDir$cleanCoverHref"]
                ?: imageFiles[fileNameOnly]
                ?: imageFiles.entries.find { it.key.equals(decodedCoverHref, ignoreCase = true) || it.key.endsWith("/$fileNameOnly", ignoreCase = true) }?.value

            // If detectedCoverHref pointed to an HTML cover wrapper (e.g. cover.xhtml), inspect it for <img>
            if (candidateCoverPath == null && (decodedCoverHref.endsWith(".xhtml", ignoreCase = true) || decodedCoverHref.endsWith(".html", ignoreCase = true))) {
                val coverHtml = htmlEntries[decodedCoverHref] ?: htmlEntries["$opfDir$decodedCoverHref"] ?: htmlEntries[fileNameOnly]
                if (coverHtml != null) {
                    val imgMatch = Regex("""<img[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(coverHtml)
                        ?: Regex("""<image[^>]+(?:xlink:href|href)=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(coverHtml)
                    if (imgMatch != null) {
                        val imgSrc = imgMatch.groupValues[1].substringBefore('#').substringBefore('?')
                        val imgDecoded = try { java.net.URLDecoder.decode(imgSrc, "UTF-8") } catch (_: Exception) { imgSrc }
                        val imgFileOnly = imgDecoded.substringAfterLast('/')
                        val resolvedImg = resolveRelativePath(opfDir, imgDecoded)
                        candidateCoverPath = imageFiles[resolvedImg] ?: imageFiles[imgDecoded] ?: imageFiles["$opfDir$imgDecoded"] ?: imageFiles[imgFileOnly]
                    }
                }
            }
        }

        // Fallback 1: If no explicit cover href matched, check image files for a file named "cover"
        if (candidateCoverPath == null) {
            val coverNamedEntry = imageFiles.entries.find { it.key.contains("cover", ignoreCase = true) }
            if (coverNamedEntry != null) {
                candidateCoverPath = coverNamedEntry.value
                android.util.Log.i("EPUB_COVER", "[Cover Fallback 1] Found image with 'cover' in filename: ${coverNamedEntry.key}")
            }
        }

        // Fallback 2: Check manifest for the first item with mediaType starting with "image/"
        if (candidateCoverPath == null) {
            val firstManifestImage = manifestMap.values.find { it.mediaType.startsWith("image/", ignoreCase = true) }
            if (firstManifestImage != null) {
                val resolvedFirst = resolveRelativePath(opfDir, firstManifestImage.href)
                candidateCoverPath = imageFiles[resolvedFirst]
                    ?: imageFiles[firstManifestImage.href]
                    ?: imageFiles[firstManifestImage.href.substringAfterLast('/')]
                if (candidateCoverPath != null) {
                    android.util.Log.i("EPUB_COVER", "[Cover Fallback 2] Found first manifest image: ${firstManifestImage.href} -> $candidateCoverPath")
                }
            }
        }

        // Fallback 3: Check image files for any valid image (> 500 bytes, not icon)
        if (candidateCoverPath == null && imageFiles.isNotEmpty()) {
            val firstValidImage = imageFiles.values.find { path ->
                val f = java.io.File(path)
                f.exists() && f.length() > 500 && !f.name.contains("icon", ignoreCase = true)
            }
            if (firstValidImage != null) {
                candidateCoverPath = firstValidImage
                android.util.Log.i("EPUB_COVER", "[Cover Fallback 3] Used first available image file: $firstValidImage")
            }
        }

        if (candidateCoverPath != null) {
            try {
                val candidateFile = java.io.File(candidateCoverPath)
                if (candidateFile.exists() && candidateFile.length() > 0) {
                    val coversDir = java.io.File(context.filesDir, "covers")
                    coversDir.mkdirs()

                    // [Step 1 Debug Verification]: Save directly as a named debug cover file for verification
                    val safeTitle = bookTitle.take(20).replace(Regex("""[^\w\u4e00-\u9fa5]"""), "_")
                    val debugCoverFile = java.io.File(coversDir, "debug_cover_${safeTitle}.jpg")
                    candidateFile.copyTo(debugCoverFile, overwrite = true)
                    android.util.Log.i("EPUB_COVER_STEP1", "[Step 1 Extraction Check] Book='$bookTitle', extracted file='${debugCoverFile.absolutePath}', size=${debugCoverFile.length()} bytes, exists=${debugCoverFile.exists()}")

                    // Unique temporary cover file to prevent any collision between books
                    val uniqueCoverFile = java.io.File(coversDir, "temp_cover_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(6)}.jpg")
                    candidateFile.copyTo(uniqueCoverFile, overwrite = true)
                    finalCoverFilePath = uniqueCoverFile.absolutePath
                    android.util.Log.i("EPUB_COVER", "[Cover Extracted] Book='$bookTitle', source=$candidateCoverPath, size=${candidateFile.length()}B -> temp=${uniqueCoverFile.name}")
                }
            } catch (e: Exception) {
                android.util.Log.e("EPUB_COVER", "Failed to extract cover for '$bookTitle'", e)
            }
        } else {
            android.util.Log.w("EPUB_COVER", "[No Cover Found] Book='$bookTitle', detectedCoverHref='$detectedCoverHref', totalImages=${imageFiles.size} -> using distinctive typography fallback")
        }

        // Parse TOC from all NCX entries or EPUB3 NAV entries
        // Map normalized path/href -> Chapter Title
        val tocMap = mutableMapOf<String, String>()
        val orderedTocList = mutableListOf<Pair<String, String>>() // (src, title)

        for ((_, ncxContent) in ncxEntries) {
            val parsedNcx = parseNcxToc(ncxContent)
            for ((src, title) in parsedNcx) {
                if (title.isNotBlank()) {
                    val normalized = normalizeHref(src)
                    tocMap[normalized] = title
                    tocMap[src] = title
                    val filenameOnly = src.substringAfterLast('/').substringBefore('#')
                    if (filenameOnly.isNotBlank()) {
                        tocMap.putIfAbsent(filenameOnly, title)
                    }
                    orderedTocList.add(Pair(src, title))
                }
            }
        }

        // If no NCX TOC found, check for EPUB 3 Nav XHTML (nav.xhtml or toc.xhtml)
        if (tocMap.isEmpty()) {
            for ((name, htmlContent) in htmlEntries) {
                if (name.contains("nav", ignoreCase = true) || name.contains("toc", ignoreCase = true)) {
                    val navToc = parseNavXhtmlToc(htmlContent)
                    for ((src, title) in navToc) {
                        if (title.isNotBlank()) {
                            val normalized = normalizeHref(src)
                            tocMap[normalized] = title
                            tocMap[src] = title
                            val filenameOnly = src.substringAfterLast('/').substringBefore('#')
                            if (filenameOnly.isNotBlank()) {
                                tocMap.putIfAbsent(filenameOnly, title)
                            }
                            orderedTocList.add(Pair(src, title))
                        }
                    }
                }
            }
        }

        // Order chapters based on spine or sorted keys
        val orderedKeys = if (spineItems.isNotEmpty()) {
            val matched = mutableListOf<String>()
            for (href in spineItems) {
                val cleanHref = href.substringBefore('#')
                val targetName = cleanHref.substringAfterLast('/')
                val foundKey = htmlEntries.keys.find {
                    it == href || it == cleanHref || it.endsWith("/$cleanHref") || it.endsWith("/$targetName") || it == "$opfDir$cleanHref"
                }
                if (foundKey != null && !matched.contains(foundKey)) {
                    matched.add(foundKey)
                }
            }
            // Add remaining html entries not matched in spine (skip standalone nav if it has no body text)
            htmlEntries.keys.forEach { k ->
                if (!matched.contains(k) && !k.endsWith("nav.xhtml") && !k.endsWith("toc.xhtml")) {
                    matched.add(k)
                }
            }
            matched
        } else {
            htmlEntries.keys.sorted().filter { !it.endsWith("nav.xhtml") && !it.endsWith("toc.xhtml") }
        }

        val chapters = mutableListOf<ParsedChapter>()
        var chapterIndex = 1

        for (key in orderedKeys) {
            val rawHtml = htmlEntries[key] ?: continue
            val keyFilename = key.substringAfterLast('/')
            
            // Find TOC title matching this key
            val ncxTitle = tocMap[key] 
                ?: tocMap[keyFilename]
                ?: tocMap[key.substringBeforeLast(".")]
                ?: tocMap.entries.find { key.endsWith(it.key) || it.key.endsWith(keyFilename) }?.value

            val candidateTitle = extractChapterTitleOnly(rawHtml, chapterIndex, ncxTitle)
            val finalTitle = if (chapterIndex == 1 && (key.contains("cover", ignoreCase = true) || candidateTitle.contains("cover", ignoreCase = true))) {
                "封面"
            } else {
                normalizeChapterTitle(candidateTitle, chapterIndex)
            }
            val plainBody = EpubRichTextFormatter.stripHtml(rawHtml)
            val hasImages = rawHtml.contains("<img", ignoreCase = true) || rawHtml.contains("<image", ignoreCase = true)
            
            // Avoid adding empty covers or nav-only pages if they have neither text nor images
            if (plainBody.isNotBlank() || hasImages) {
                val prefixedContent = "<!-- EPUB_META: relPath=$key;hash=$bookHash -->\n$rawHtml"
                chapters.add(
                    ParsedChapter(
                        title = finalTitle,
                        content = prefixedContent,
                        wordCount = plainBody.length.coerceAtLeast(10)
                    )
                )
                chapterIndex++
            }
        }

        if (chapters.isEmpty()) {
            chapters.add(
                ParsedChapter(
                    title = "第一章 $bookTitle",
                    content = "无法从 EPUB 中解析正文内容或文件为空。",
                    wordCount = 20
                )
            )
        }

        val totalWordCount = chapters.sumOf { it.wordCount.toLong() }
        return ParsedBook(
            title = bookTitle,
            author = bookAuthor,
            format = "EPUB",
            intro = bookIntro,
            chapters = chapters,
            totalWordCount = totalWordCount,
            coverFilePath = finalCoverFilePath
        )
    }

    private data class OpfManifestItem(
        val id: String,
        val href: String,
        val mediaType: String,
        val properties: String = ""
    )

    private data class OpfMeta(
        val title: String,
        val author: String,
        val intro: String,
        val spineHrefs: List<String>,
        val coverHref: String = ""
    )

    private fun extractManifestFromOpf(opfContent: String): Map<String, OpfManifestItem> {
        val itemRegex = Regex("""<item\b([^>]*)/?>""", RegexOption.IGNORE_CASE)
        val idRegex = Regex("""\bid=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val hrefRegex = Regex("""\bhref=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val mediaTypeRegex = Regex("""\bmedia-type=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val propertiesRegex = Regex("""\bproperties=["']([^"']+)["']""", RegexOption.IGNORE_CASE)

        val manifest = mutableMapOf<String, OpfManifestItem>()
        itemRegex.findAll(opfContent).forEach { match ->
            val attrs = match.groupValues[1]
            val id = idRegex.find(attrs)?.groupValues?.getOrNull(1) ?: ""
            val href = hrefRegex.find(attrs)?.groupValues?.getOrNull(1) ?: ""
            val mediaType = mediaTypeRegex.find(attrs)?.groupValues?.getOrNull(1) ?: ""
            val properties = propertiesRegex.find(attrs)?.groupValues?.getOrNull(1) ?: ""
            if (id.isNotBlank() && href.isNotBlank()) {
                manifest[id] = OpfManifestItem(id, href, mediaType, properties)
            }
        }
        return manifest
    }

    private fun parseOpfMetadata(opfContent: String): OpfMeta? {
        return try {
            val titleRegex = Regex("""<dc:title[^>]*>([^<]+)</dc:title>""", RegexOption.IGNORE_CASE)
            val authorRegex = Regex("""<dc:creator[^>]*>([^<]+)</dc:creator>""", RegexOption.IGNORE_CASE)
            val descRegex = Regex("""<dc:description[^>]*>([^<]+)</dc:description>""", RegexOption.IGNORE_CASE)

            val title = titleRegex.find(opfContent)?.groupValues?.getOrNull(1)?.trim()?.unescapeHtml() ?: ""
            val author = authorRegex.find(opfContent)?.groupValues?.getOrNull(1)?.trim()?.unescapeHtml() ?: ""
            val desc = descRegex.find(opfContent)?.groupValues?.getOrNull(1)?.trim()?.unescapeHtml() ?: ""

            // Robust manifest extraction regardless of attribute order
            val manifest = extractManifestFromOpf(opfContent)

            // Spine idrefs
            val itemrefRegex = Regex("""<itemref\b[^>]*\bidref=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
            val spineHrefs = mutableListOf<String>()
            itemrefRegex.findAll(opfContent).forEach { match ->
                val idref = match.groupValues[1]
                manifest[idref]?.let { spineHrefs.add(it.href) }
            }

            // Cover detection from OPF:
            // 1. EPUB 2: <meta name="cover" content="itemId"/> (any attribute order)
            val metaRegex = Regex("""<meta\b([^>]*)/?>""", RegexOption.IGNORE_CASE)
            val nameRegex = Regex("""\bname=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
            val contentRegex = Regex("""\bcontent=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
            var coverMetaId = ""
            metaRegex.findAll(opfContent).forEach { m ->
                val attrs = m.groupValues[1]
                val name = nameRegex.find(attrs)?.groupValues?.getOrNull(1) ?: ""
                if (name.equals("cover", ignoreCase = true)) {
                    coverMetaId = contentRegex.find(attrs)?.groupValues?.getOrNull(1) ?: ""
                }
            }

            // 2. EPUB 3: <item properties="cover-image" .../>
            val epub3CoverItem = manifest.values.find { it.properties.contains("cover-image", ignoreCase = true) }

            // 3. Manifest item with id="cover" or id="cover-image"
            val idCoverItem = manifest["cover"] ?: manifest["cover-image"] ?: manifest["cover_image"] ?: manifest["book-cover"]

            // 4. Manifest item with href containing "cover" and media-type starts with "image/"
            val hrefCoverItem = manifest.values.find {
                it.mediaType.startsWith("image/", ignoreCase = true) &&
                (it.href.contains("cover", ignoreCase = true) || it.id.contains("cover", ignoreCase = true))
            }

            val coverHref = when {
                coverMetaId.isNotBlank() && manifest.containsKey(coverMetaId) -> manifest[coverMetaId]?.href ?: ""
                coverMetaId.isNotBlank() && isImageFile(coverMetaId) -> coverMetaId
                epub3CoverItem != null -> epub3CoverItem.href
                idCoverItem != null && (idCoverItem.mediaType.startsWith("image/") || isImageFile(idCoverItem.href)) -> idCoverItem.href
                hrefCoverItem != null -> hrefCoverItem.href
                else -> ""
            }

            android.util.Log.i("EPUB_COVER", "[OPF Meta] title='$title', author='$author', coverMetaId='$coverMetaId', coverHref='$coverHref', totalItems=${manifest.size}")

            OpfMeta(title, author, desc, spineHrefs, coverHref)
        } catch (e: Exception) {
            android.util.Log.e("EPUB_COVER", "Error parsing OPF metadata", e)
            null
        }
    }

    private fun parseNcxToc(ncxContent: String): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        try {
            // Match each <navPoint> block
            val navPointRegex = Regex("""<navPoint[^>]*>([\s\S]*?)(?=<navPoint|</navMap>)""", RegexOption.IGNORE_CASE)
            val textRegex = Regex("""<text[^>]*>([^<]+)</text>""", RegexOption.IGNORE_CASE)
            val contentRegex = Regex("""<content[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE)

            var idx = 1
            navPointRegex.findAll(ncxContent).forEach { match ->
                val block = match.groupValues[1]
                val text = textRegex.find(block)?.groupValues?.getOrNull(1)?.trim()?.unescapeHtml() ?: ""
                val src = contentRegex.find(block)?.groupValues?.getOrNull(1)?.trim() ?: ""
                if (text.isNotBlank() && src.isNotBlank()) {
                    val normalizedTitle = normalizeChapterTitle(text, idx++)
                    result.add(Pair(src, normalizedTitle))
                }
            }

            // Fallback for simple flat regex if navPointRegex found nothing
            if (result.isEmpty()) {
                val flatMatch = Regex("""<navLabel>\s*<text>([^<]+)</text>\s*</navLabel>\s*<content\s+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                var fallbackIdx = 1
                flatMatch.findAll(ncxContent).forEach { m ->
                    val title = m.groupValues[1].trim().unescapeHtml()
                    val src = m.groupValues[2].trim()
                    if (title.isNotBlank() && src.isNotBlank()) {
                        val normalizedTitle = normalizeChapterTitle(title, fallbackIdx++)
                        result.add(Pair(src, normalizedTitle))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun parseNavXhtmlToc(html: String): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        try {
            val aRegex = Regex("""<a[^>]+href=["']([^"']+)["'][^>]*>([\s\S]*?)</a>""", RegexOption.IGNORE_CASE)
            var idx = 1
            aRegex.findAll(html).forEach { match ->
                val href = match.groupValues[1].trim()
                val rawTitle = match.groupValues[2].replace(Regex("<[^>]+>"), "").trim().unescapeHtml()
                if (href.isNotBlank() && rawTitle.isNotBlank()) {
                    val normalizedTitle = normalizeChapterTitle(rawTitle, idx++)
                    result.add(Pair(href, normalizedTitle))
                }
            }
        } catch (_: Exception) {}
        return result
    }

    private fun normalizeHref(href: String): String {
        return href.substringBefore('#').substringAfterLast('/')
    }

    private fun resolveRelativePath(baseDir: String, relPath: String): String {
        val cleanRel = relPath.substringBefore('#').substringBefore('?')
        val combined = if (baseDir.isEmpty()) cleanRel else if (baseDir.endsWith('/')) "$baseDir$cleanRel" else "$baseDir/$cleanRel"
        val parts = combined.replace('\\', '/').split('/')
        val resolved = mutableListOf<String>()
        for (part in parts) {
            if (part == "." || part.isEmpty()) continue
            if (part == "..") {
                if (resolved.isNotEmpty()) resolved.removeAt(resolved.size - 1)
            } else {
                resolved.add(part)
            }
        }
        return resolved.joinToString("/")
    }

    private fun extractChapterTitleOnly(html: String, chapterIndex: Int, preferredTitle: String? = null): String {
        if (!preferredTitle.isNullOrBlank()) {
            return normalizeChapterTitle(preferredTitle, chapterIndex)
        }

        // 1. Try to find headings (h1, h2, h3, h4)
        val hTagRegex = Regex("""<(h[1-4]|header)[^>]*>([\s\S]*?)</\1>""", RegexOption.IGNORE_CASE)
        val headingMatch = hTagRegex.find(html)
        if (headingMatch != null) {
            val candidate = headingMatch.groupValues[2].replace(Regex("<[^>]+>"), "").trim().unescapeHtml()
            if (candidate.isNotBlank() && candidate.length <= 60) {
                return normalizeChapterTitle(candidate, chapterIndex)
            }
        }

        // 2. Try class with title or chapter
        val classTitleRegex = Regex("""<p[^>]+class=["'][^"']*(?:title|chapter|heading)[^"']*["'][^>]*>([\s\S]*?)</p>""", RegexOption.IGNORE_CASE)
        val classMatch = classTitleRegex.find(html)
        if (classMatch != null) {
            val candidate = classMatch.groupValues[1].replace(Regex("<[^>]+>"), "").trim().unescapeHtml()
            if (candidate.isNotBlank() && candidate.length <= 60) {
                return normalizeChapterTitle(candidate, chapterIndex)
            }
        }

        // 3. Try <title> tag
        val titleTagRegex = Regex("""<title[^>]*>([\s\S]*?)</title>""", RegexOption.IGNORE_CASE)
        val titleMatch = titleTagRegex.find(html)
        if (titleMatch != null) {
            val candidate = titleMatch.groupValues[1].replace(Regex("<[^>]+>"), "").trim().unescapeHtml()
            if (candidate.isNotBlank() && candidate.length <= 60 && !candidate.equals("untitled", ignoreCase = true)) {
                return normalizeChapterTitle(candidate, chapterIndex)
            }
        }

        // 4. First line of plain text
        val plainText = EpubRichTextFormatter.stripHtml(html)
        val firstLine = plainText.lines().map { it.trim() }.firstOrNull { it.isNotBlank() }
        if (firstLine != null && firstLine.length in 2..40 && (CHAPTER_REGEX.matches(firstLine) || firstLine.startsWith("第") || firstLine.startsWith("Chapter"))) {
            return normalizeChapterTitle(firstLine, chapterIndex)
        }

        return "第${chapterIndex}章"
    }

    private fun extractHtmlContent(html: String, chapterIndex: Int, preferredTitle: String? = null): Pair<String, String> {
        var extractedTitle = preferredTitle ?: ""

        if (extractedTitle.isBlank()) {
            // 1. Try to find headings (h1, h2, h3, h4)
            val hTagRegex = Regex("""<(h[1-4]|header)[^>]*>([\s\S]*?)</\1>""", RegexOption.IGNORE_CASE)
            val headingMatch = hTagRegex.find(html)
            if (headingMatch != null) {
                val candidate = headingMatch.groupValues[2].replace(Regex("<[^>]+>"), "").trim().unescapeHtml()
                if (candidate.isNotBlank() && candidate.length <= 60) {
                    extractedTitle = candidate
                }
            }
        }

        if (extractedTitle.isBlank()) {
            // 2. Try to find class containing title or chapter
            val classTitleRegex = Regex("""<p[^>]+class=["'][^"']*(?:title|chapter|heading)[^"']*["'][^>]*>([\s\S]*?)</p>""", RegexOption.IGNORE_CASE)
            val classMatch = classTitleRegex.find(html)
            if (classMatch != null) {
                val candidate = classMatch.groupValues[1].replace(Regex("<[^>]+>"), "").trim().unescapeHtml()
                if (candidate.isNotBlank() && candidate.length <= 60) {
                    extractedTitle = candidate
                }
            }
        }

        if (extractedTitle.isBlank()) {
            // 3. Try <title> tag if not generic
            val titleTagRegex = Regex("""<title[^>]*>([\s\S]*?)</title>""", RegexOption.IGNORE_CASE)
            val titleMatch = titleTagRegex.find(html)
            if (titleMatch != null) {
                val candidate = titleMatch.groupValues[1].replace(Regex("<[^>]+>"), "").trim().unescapeHtml()
                if (candidate.isNotBlank() && candidate.length <= 60 && !candidate.equals("untitled", ignoreCase = true)) {
                    extractedTitle = candidate
                }
            }
        }

        // Strip scripts, heads, and comments first so their text doesn't leak into body
        val sanitizedHtml = html
            .replace(Regex("""<!--[\s\S]*?-->"""), "")
            .replace(Regex("""<script[^>]*>[\s\S]*?</script>""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""<head[^>]*>[\s\S]*?</head>""", RegexOption.IGNORE_CASE), "")

        // Replace paragraph breaks and line breaks, preserving illustrations as [插图: url]
        val imgRegex = Regex("""<img[^>]+src=["']([^"']+)["'][^>]*>""", RegexOption.IGNORE_CASE)
        val imageTagRegex = Regex("""<image[^>]+(?:xlink:href|href)=["']([^"']+)["'][^>]*>""", RegexOption.IGNORE_CASE)
        var withImages = sanitizedHtml
        withImages = imgRegex.replace(withImages) { m ->
            val src = m.groupValues[1]
            "<p><b>[插图: $src]</b></p>"
        }
        withImages = imageTagRegex.replace(withImages) { m ->
            val src = m.groupValues[1]
            "<p><b>[插图: $src]</b></p>"
        }

        // Whitelist semantic HTML tags to preserve rich book formatting:
        // h1-h6, p, div, blockquote, b, strong, i, em, u, del, s, strike, br, and span with style
        val allowedTagPattern = Regex("""<(/?)(h[1-6]|p|div|blockquote|b|strong|i|em|u|del|s|strike|br|span)([^>]*)>""", RegexOption.IGNORE_CASE)
        val intermediate = allowedTagPattern.replace(withImages) { m ->
            val slash = m.groupValues[1]
            val tag = m.groupValues[2].lowercase()
            val attrs = m.groupValues[3]
            val styleMatch = Regex("""style=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(attrs)
            val styleAttr = if (styleMatch != null) " style=\"${styleMatch.groupValues[1]}\"" else ""
            "___KEEP_${slash}${tag}${styleAttr}___"
        }

        // Strip non-semantic tags
        val stripped = intermediate.replace(Regex("""<[^>]+>"""), "")

        // Restore allowed semantic tags
        val restored = stripped.replace(Regex("""___KEEP_(/?)(h[1-6]|p|div|blockquote|b|strong|i|em|u|del|s|strike|br|span)([^_]*)___""")) { m ->
            val slash = m.groupValues[1]
            val tag = m.groupValues[2]
            val extra = m.groupValues[3]
            "<$slash$tag$extra>"
        }.unescapeHtml().trim()

        // 4. If title is still blank, check if the first line of text is a valid chapter title
        val plainText = EpubRichTextFormatter.stripHtml(restored)
        val rawLines = plainText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (extractedTitle.isBlank() && rawLines.isNotEmpty()) {
            val firstLine = rawLines.first()
            if (firstLine.length in 2..40 && (CHAPTER_REGEX.matches(firstLine) || firstLine.startsWith("第") || firstLine.startsWith("Chapter"))) {
                extractedTitle = firstLine
            }
        }

        val finalBody = restored.ifBlank { plainText }
        return Pair(extractedTitle.ifBlank { "第${chapterIndex}章" }, finalBody)
    }

    // ================== DOCX PARSER ==================
    private fun parseDocx(context: Context, uri: Uri, fileName: String): ParsedBook {
        val bookTitle = fileName.substringBeforeLast(".")
        val bookAuthor = "Word 文档"
        val paragraphs = mutableListOf<Pair<String, Boolean>>() // text to isHeading

        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                ZipInputStream(stream).use { zip ->
                    var entry: ZipEntry? = zip.nextEntry
                    while (entry != null) {
                        if (entry.name == "word/document.xml") {
                            val xmlContent = readZipEntryToString(zip)
                            parseDocxXml(xmlContent, paragraphs)
                            break
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Split paragraphs into chapters
        val chapters = mutableListOf<ParsedChapter>()
        var currentTitle = "第一章 $bookTitle"
        val currentBody = StringBuilder()

        for ((pText, isHeading) in paragraphs) {
            val isChapterTitle = isHeading || (pText.length in 2..40 && CHAPTER_REGEX.matches(pText))
            if (isChapterTitle) {
                if (currentBody.isNotBlank()) {
                    chapters.add(
                        ParsedChapter(
                            title = normalizeChapterTitle(currentTitle, chapters.size + 1),
                            content = currentBody.toString().trim(),
                            wordCount = currentBody.length
                        )
                    )
                    currentBody.clear()
                }
                currentTitle = pText
            } else {
                currentBody.append(pText).append("\n\n")
            }
        }

        if (currentBody.isNotBlank() || chapters.isEmpty()) {
            chapters.add(
                ParsedChapter(
                    title = normalizeChapterTitle(currentTitle, chapters.size + 1),
                    content = currentBody.toString().trim().ifBlank { "文档无正文内容" },
                    wordCount = currentBody.length.coerceAtLeast(10)
                )
            )
        }

        val totalWordCount = chapters.sumOf { it.wordCount.toLong() }
        return ParsedBook(
            title = bookTitle,
            author = bookAuthor,
            format = "DOCX",
            intro = "导入的 DOCX 办公文档《$bookTitle》",
            chapters = chapters,
            totalWordCount = totalWordCount
        )
    }

    private fun parseDocxXml(xml: String, output: MutableList<Pair<String, Boolean>>) {
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var inParagraph = false
            var isHeading = false
            var currentP = StringBuilder()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tag = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tag == "p") {
                            inParagraph = true
                            isHeading = false
                            currentP.clear()
                        } else if (tag == "pStyle") {
                            val styleVal = parser.getAttributeValue(null, "val") ?: ""
                            if (styleVal.startsWith("Heading", ignoreCase = true) || styleVal.contains("标题")) {
                                isHeading = true
                            }
                        } else if (tag == "t") {
                            val text = parser.nextText()
                            currentP.append(text)
                        } else if (tag == "br" || tag == "cr") {
                            currentP.append("\n")
                        } else if (tag == "tab") {
                            currentP.append("    ")
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tag == "p") {
                            val text = currentP.toString().trim()
                            if (text.isNotEmpty()) {
                                output.add(Pair(text, isHeading))
                            }
                            inParagraph = false
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Exception) {
            // Regex fallback if parser fails
            val pRegex = Regex("""<w:p(?:\s+[^>]*)?>(.*?)</w:p>""", RegexOption.DOT_MATCHES_ALL)
            val tRegex = Regex("""<w:t(?:\s+[^>]*)?>(.*?)</w:t>""")
            pRegex.findAll(xml).forEach { match ->
                val pBlock = match.groupValues[1]
                val textBuilder = StringBuilder()
                tRegex.findAll(pBlock).forEach { tMatch ->
                    textBuilder.append(tMatch.groupValues[1].unescapeHtml())
                }
                val text = textBuilder.toString().trim()
                if (text.isNotEmpty()) {
                    val isHead = pBlock.contains("w:val=\"Heading") || pBlock.contains("w:val=\"标题")
                    output.add(Pair(text, isHead))
                }
            }
        }
    }

    // ================== TXT PARSER ==================
    private fun parseTxt(context: Context, uri: Uri, fileName: String, forcedEncoding: String? = null): ParsedBook {
        val rawBytes = readUriBytes(context, uri)
        val rawText = CharsetDetector.detectAndDecode(rawBytes, forcedCharset = forcedEncoding)
        val bookTitle = fileName.substringBeforeLast(".")

        val lines = rawText.lines()
        val chapters = mutableListOf<ParsedChapter>()
        var currentTitle = "第一章 $bookTitle"
        val currentBuffer = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.length in 2..40 && CHAPTER_REGEX.matches(trimmed)) {
                if (currentBuffer.isNotBlank()) {
                    val body = currentBuffer.toString().trim()
                    chapters.add(
                        ParsedChapter(
                            title = normalizeChapterTitle(currentTitle, chapters.size + 1),
                            content = body,
                            wordCount = body.length
                        )
                    )
                    currentBuffer.clear()
                }
                currentTitle = trimmed
            } else {
                currentBuffer.append(line).append("\n")
            }
        }

        if (currentBuffer.isNotBlank() || chapters.isEmpty()) {
            val body = currentBuffer.toString().trim().ifBlank { "文档内容为空" }
            chapters.add(
                ParsedChapter(
                    title = normalizeChapterTitle(currentTitle, chapters.size + 1),
                    content = body,
                    wordCount = body.length
                )
            )
        }

        val totalWordCount = rawText.length.toLong()
        return ParsedBook(
            title = bookTitle,
            author = "本地导入",
            format = "TXT",
            intro = "导入的本地 TXT 电子书《$bookTitle》",
            chapters = chapters,
            totalWordCount = totalWordCount
        )
    }

    private fun readUriBytes(context: Context, uri: Uri): ByteArray {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val bos = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var len: Int
            while (stream.read(buffer).also { len = it } != -1) {
                bos.write(buffer, 0, len)
            }
            return bos.toByteArray()
        }
        return ByteArray(0)
    }

    private fun readZipEntryBytes(zip: ZipInputStream): ByteArray {
        val bos = ByteArrayOutputStream()
        val buffer = ByteArray(32768)
        var len: Int
        while (zip.read(buffer).also { len = it } != -1) {
            bos.write(buffer, 0, len)
        }
        return bos.toByteArray()
    }

    private fun readZipEntryToString(zip: ZipInputStream): String {
        val bytes = readZipEntryBytes(zip)
        return CharsetDetector.detectAndDecode(bytes)
    }

    private fun detectCharset(bytes: ByteArray): Charset {
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return Charsets.UTF_8
        }
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return Charsets.UTF_16BE
        }
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            return Charsets.UTF_16LE
        }
        // Try UTF-8 validity
        return try {
            val decoder = Charsets.UTF_8.newDecoder()
            decoder.decode(java.nio.ByteBuffer.wrap(bytes))
            Charsets.UTF_8
        } catch (_: Exception) {
            try {
                Charset.forName("GBK")
            } catch (_: Exception) {
                Charsets.UTF_8
            }
        }
    }

    private fun isImageFile(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
                lower.endsWith(".webp") || lower.endsWith(".gif") || lower.endsWith(".svg")
    }
}

fun normalizeChapterTitle(title: String, chapterIndex: Int = 1): String {
    if (title.isBlank()) return "第${chapterIndex}章"

    // 1. Remove HTML tags, heal mojibake, and decode HTML entities
    var clean = CharsetDetector.healAndSanitize(title.replace(Regex("<[^>]+>"), "")).trim()

    // 2. Strip leading/trailing brackets, quotes, and punctuation
    clean = clean.replace(Regex("""^[\[\(\{【《「『（〔\s"'-]+"""), "")
        .replace(Regex("""[\]\)\}】》」』）〕\s"'-]+$"""), "")
        .trim()

    val lower = clean.lowercase().trim()

    // 3. Robust COVER / 封面 identification (addresses English COVER first directory bug)
    val isCover = lower == "cover" ||
            lower == "coverpage" ||
            lower == "cover page" ||
            lower == "cover_page" ||
            lower == "cover-page" ||
            lower == "bookcover" ||
            lower == "book cover" ||
            lower == "book_cover" ||
            lower == "frontcover" ||
            lower == "front cover" ||
            lower == "front_cover" ||
            lower == "cover image" ||
            lower == "coverimage" ||
            lower == "cover_image" ||
            lower == "cover-image" ||
            lower == "cover art" ||
            lower == "coverart" ||
            lower == "cover_art" ||
            lower == "cover.xhtml" ||
            lower == "cover.html" ||
            lower == "cover.htm" ||
            lower == "cover.jpg" ||
            lower == "cover.jpeg" ||
            lower == "cover.png" ||
            lower == "cover01" ||
            lower == "cover_01" ||
            lower == "cover-01" ||
            lower == "c1" ||
            lower == "cov" ||
            lower == "cov.xhtml" ||
            lower == "cov.html" ||
            lower.matches(Regex("""^(?:text/|oebps/)?cover[\d_\-.]*(?:\.x?html?|\.jpe?g|\.png)?$""")) ||
            (chapterIndex <= 1 && (lower.contains("cover") || lower == "01" || lower == "1"))

    if (isCover) {
        return "封面"
    }

    // 4. Other standard front matter and navigation titles
    val isTitlePage = lower == "titlepage" ||
            lower == "title_page" ||
            lower == "title page" ||
            lower == "title-page" ||
            lower == "titlepage.xhtml" ||
            lower == "titlepage.html" ||
            (chapterIndex <= 2 && lower == "title") ||
            lower.matches(Regex("""^(?:text/|oebps/)?title(?:page)?(?:\.x?html?)?$"""))
    if (isTitlePage) return "扉页"

    val isToc = lower == "contents" ||
            lower == "table of contents" ||
            lower == "table_of_contents" ||
            lower == "toc" ||
            lower == "toc.xhtml" ||
            lower == "toc.html" ||
            lower == "nav" ||
            lower == "nav.xhtml" ||
            (lower.contains("目录") && !clean.contains("第"))
    if (isToc) return "目录"

    val isCopyright = lower == "copyright" ||
            lower == "copyright page" ||
            lower == "copyright_page" ||
            lower == "copyright.xhtml" ||
            lower == "colophon" ||
            lower == "colophon.xhtml" ||
            (lower.contains("版权") && !clean.contains("第"))
    if (isCopyright) return "版权信息"

    if (lower == "preface" || lower == "foreword") return "前言"
    if (lower == "prologue") return "序言"
    if (lower == "epilogue") return "尾声"
    if (lower == "afterword" || lower == "postscript") return "后记"
    if (lower == "introduction") return "引言"
    if (lower == "appendix") return "附录"
    if (lower == "about the author" || lower == "about the authors") return "关于作者"

    // 5. Packager generated file names e.g. text/part0001.html, split_001.xhtml, Section0001.xhtml
    if (lower.matches(Regex("""^(?:text/|oebps/)?(?:text|section|chapter|part|page|split|ch|sec)[\d_\-.]+(?:\.x?html?)?$"""))) {
        return "第${chapterIndex}章"
    }

    // 6. English "Chapter X" conversion
    val chapterEnglishMatch = Regex("""^Chapter\s+([0-9]+)[:：\s]*(.*)$""", RegexOption.IGNORE_CASE).find(clean)
    if (chapterEnglishMatch != null) {
        val num = chapterEnglishMatch.groupValues[1]
        val rest = chapterEnglishMatch.groupValues[2].trim()
        return if (rest.isNotBlank()) "第${num}章 $rest" else "第${num}章"
    }

    // 7. Strip trailing web extension like .xhtml or .html
    val strippedExt = clean.replace(Regex("""\.(?:x?html?|xml|htm)$""", RegexOption.IGNORE_CASE), "").trim()

    return if (strippedExt.isNotBlank()) strippedExt else "第${chapterIndex}章"
}

internal fun String.unescapeHtml(): String {
    var str = this
        .replace("&nbsp;", " ")
        .replace("&quot;", "\"")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&apos;", "'")
        .replace("&mdash;", "—")
        .replace("&ndash;", "–")
        .replace("&ldquo;", "“")
        .replace("&rdquo;", "”")
        .replace("&lsquo;", "‘")
        .replace("&rsquo;", "’")
        .replace("&hellip;", "…")
        .replace("&middot;", "·")
        .replace("&copy;", "©")
        .replace("&reg;", "®")
        .replace("&trade;", "™")
        .replace("&times;", "×")
        .replace("&divide;", "÷")
        .replace("&permil;", "‰")
        .replace("&bull;", "•")
        .replace("&deg;", "°")
        .replace("&plusmn;", "±")

    // Decode &#x...; hex entities with 32-bit supplementary character support
    str = Regex("""&#x([0-9a-fA-F]+);""").replace(str) { match ->
        try {
            val code = match.groupValues[1].toInt(16)
            if (Character.isValidCodePoint(code)) {
                String(Character.toChars(code))
            } else {
                match.value
            }
        } catch (_: Exception) {
            match.value
        }
    }
    // Decode &#...; decimal entities with 32-bit supplementary character support
    str = Regex("""&#([0-9]+);""").replace(str) { match ->
        try {
            val code = match.groupValues[1].toInt()
            if (Character.isValidCodePoint(code)) {
                String(Character.toChars(code))
            } else {
                match.value
            }
        } catch (_: Exception) {
            match.value
        }
    }
    return str
}

