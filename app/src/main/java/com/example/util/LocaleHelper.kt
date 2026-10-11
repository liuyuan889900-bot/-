package com.example.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.core.os.ConfigurationCompat
import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String) {
    SYSTEM("system", "跟随系统"),
    ZH_CN("zh_CN", "简体中文"),
    ZH_TW("zh_TW", "繁體中文"),
    EN("en", "English"),
    KO("ko", "한국어")
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.SYSTEM }
val LocalAppLangCode = compositionLocalOf { "zh_CN" }

@Composable
@ReadOnlyComposable
fun appString(key: String): String {
    val langCode = LocalAppLangCode.current
    return AppStrings.get(key, langCode)
}

object LocaleHelper {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_LANGUAGE = "app_language"

    fun getSelectedLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LANGUAGE, AppLanguage.SYSTEM.code) ?: AppLanguage.SYSTEM.code
        return AppLanguage.values().find { it.code == code } ?: AppLanguage.SYSTEM
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    fun getEffectiveLanguage(context: Context, appLanguage: AppLanguage): String {
        return if (appLanguage == AppLanguage.SYSTEM) {
            val systemLocale = ConfigurationCompat.getLocales(context.resources.configuration).get(0) ?: Locale.getDefault()
            val lang = systemLocale.language.lowercase()
            val script = systemLocale.script.lowercase()
            val country = systemLocale.country.uppercase()

            when {
                lang == "zh" && (country == "TW" || country == "HK" || country == "MO" || script.contains("hant")) -> "zh_TW"
                lang == "zh" -> "zh_CN"
                lang == "ko" -> "ko"
                lang == "en" -> "en"
                else -> "zh_CN"
            }
        } else {
            appLanguage.code
        }
    }
}

/**
 * Multi-language String Key Provider
 */
object AppStrings {

    fun get(key: String, langCode: String): String {
        val map = when (langCode) {
            "zh_TW" -> stringsZhTw
            "en" -> stringsEn
            "ko" -> stringsKo
            else -> stringsZhCn
        }
        return map[key] ?: stringsZhCn[key] ?: key
    }

    // Default: Simplified Chinese
    private val stringsZhCn = mapOf(
        "app_name" to "读丛书",
        "app_brand_title" to "读丛书 · 纯净阅读",
        "app_brand_subtitle" to "沉浸式排版 · 毫秒级极速检索",
        "app_brand_version" to "当前版本 v10.0.0 · 本地安全引擎",
        "tab_bookshelf" to "书架",
        "tab_search" to "搜索",
        "tab_companion" to "书童",
        "tab_reading" to "正在阅读",
        "tab_profile" to "我的",
        
        "category_default" to "默认书架",
        "category_all" to "所有书籍",
        "category_hymn" to "歌本",
        "category_favorites" to "收藏",
        
        "search_books_hint" to "搜索书籍",
        "empty_shelf_title" to "书架空空如也",
        "empty_shelf_desc" to "点击右上角「+」或下方按钮导入您的电子书或文档",
        "btn_import_book" to "导入书籍 / 文档",
        "btn_preset_literature" to "精选文学文库",
        
        "hot_search_title" to "大家都在搜",
        "hot_kw_1" to "宇宙天皇复归式",
        "hot_kw_2" to "西乃山上帝出发",
        "hot_kw_3" to "天山日记",
        "hot_kw_4" to "主的语录",
        
        "search_tab_title" to "书名搜索",
        "search_tab_toc" to "目录搜索",
        "search_tab_content" to "正文搜索",
        "search_hint" to "输入书名、作者、章节或正文内容...",
        "search_speed_tip" to "⚡ 实时极速搜索已开启",
        "search_speed_desc" to "• 书名搜索：内存毫秒级过滤，支持书名、作者与分类\n• 目录搜索：跨书籍检索章节标题，点击直达\n• 正文搜索：全文字段即时检索，提供高亮上下文片段",
        "recent_searches" to "最近搜索",
        "clear_history" to "清空",
        "no_results_found" to "未找到匹配内容",
        "read_action" to "阅读",
        "back_to_shelf" to "书架",
        "search_in_book" to "搜索",
        "clear_highlight" to "清除高亮",
        "highlight_active_tip" to "已高亮检索词：",
        
        "settings_title" to "设置与偏好",
        "section_appearance" to "语言与显示偏好",
        "app_language_title" to "应用显示语言",
        "app_language_desc" to "支持多语言，默认根据手机系统语言自动匹配",
        "app_font_size_title" to "应用全局字体大小",
        "app_font_size_desc" to "调节整个软件的文字显示尺寸",
        "font_small" to "小",
        "font_standard" to "标准",
        "font_large" to "大",
        "font_huge" to "超大",
        "font_small_desc" to "紧凑排版",
        "font_standard_desc" to "默认尺寸",
        "font_large_desc" to "清晰易读",
        "font_huge_desc" to "关怀大字",
        "reader_bg_color" to "阅读器默认底色",
        "reader_bg_bamboo" to "竹青护眼 (#EBF3E8)",
        "search_speed_label" to "搜索响应延迟",
        "search_speed_val" to "120ms (实时极速)",
        "section_library_security" to "本地文库与安全",
        "supported_formats_label" to "本地支持格式",
        "supported_formats_val" to "TXT, DOCX, EPUB",
        "privacy_label" to "隐私模式",
        "privacy_val" to "100% 本地存储 · 无云端上传",
        "core_engine_label" to "核心引擎",
        "core_engine_val" to "High-Performance Room FullText",
        
        "dialog_import_title" to "导入本地书籍与文档",
        "btn_select_file" to "选择本地文件 (TXT / DOCX / EPUB)",
        "import_custom_title" to "快速粘贴创作",
        "field_book_title" to "书籍标题",
        "field_book_author" to "作者名称",
        "field_category" to "分类",
        "field_content" to "书籍内容",
        "btn_confirm_import" to "确认导入",
        "btn_cancel" to "取消",
        "btn_delete" to "删除",
        "book_action_read" to "开始阅读",
        "book_action_favorite" to "收藏",
        "book_action_unfavorite" to "取消收藏",
        "book_action_delete" to "删除书籍",
        "view_mode_spine" to "实体书脊",
        "view_mode_grid" to "网格封面",
        "view_mode_list" to "详细列表",
        
        "tab_shelf" to "书架",
        "tab_search" to "搜索",
        "tab_notes" to "笔记",
        "tab_mine" to "我的",
        "profile_user_title" to "静心读者",
        "profile_user_subtitle" to "墨香致远 · 知行合一",
        "stat_total_books" to "藏书总数",
        "stat_read_books" to "已读书籍",
        "stat_focus_days" to "阅读专注",
        "stat_books_unit" to "本",
        "stat_days_unit" to "连续 7 天",
        "setting_theme_title" to "全局主题风格",
        "setting_ambiance_title" to "阅读场景模式",
        "setting_engine_title" to "阅读排版引擎",
        "setting_engine_sub" to "纯净黑白排版 / 原生 EPUB 富文本渲染",
        "setting_search_title" to "全文搜索模式",
        "setting_search_sub" to "长文段落匹配 & 智能错别字容错模糊检索",
        "app_font_size_title" to "应用文字大小",
        "app_font_size_desc" to "支持全局文字缩放，关怀视力不佳读者",
        "app_language_title" to "应用全局语言",
        "app_language_desc" to "支持全界面切换中文、英文、韩文",
        "dialog_done" to "完成",
        "dialog_cancel" to "取消",
        "reader_toc" to "目录",
        "reader_notes" to "笔记",
        "reader_ambiance" to "氛围",
        "reader_day" to "日间",
        "reader_night" to "夜间",
        "reader_settings" to "设置",
        "reader_pref_title" to "阅读偏好与排版",
        "paper_texture" to "精选护眼纸张",
        "font_size" to "字号大小",
        "font_zoom_out" to "A - 缩小",
        "font_zoom_in" to "A + 放大",
        "line_spacing" to "行间距",
        "spacing_compact" to "紧凑",
        "spacing_standard" to "标准",
        "spacing_relaxed" to "宽松",
        "page_turn_mode" to "翻页模式",
        "mode_instant" to "极速翻页",
        "mode_simulation" to "真实书籍翻页",
        "mode_scroll" to "上下滚动"
    )

    // Traditional Chinese (繁體中文)
    private val stringsZhTw = mapOf(
        "app_name" to "讀叢書",
        "app_brand_title" to "讀叢書 · 純淨閱讀",
        "app_brand_subtitle" to "沉浸式排版 · 毫秒級極速檢索",
        "app_brand_version" to "當前版本 v10.0.0 · 本地安全引擎",
        "tab_bookshelf" to "書架",
        "tab_search" to "搜尋",
        "tab_companion" to "書童",
        "tab_reading" to "正在閱讀",
        "tab_profile" to "我的",
        
        "category_default" to "預設書架",
        "category_all" to "所有書籍",
        "category_hymn" to "歌本",
        "category_favorites" to "收藏",
        
        "search_books_hint" to "搜尋書籍",
        "empty_shelf_title" to "書架空空如也",
        "empty_shelf_desc" to "點擊右上角「+」或下方按鈕匯入您的電子書或文件",
        "btn_import_book" to "匯入書籍 / 文件",
        "btn_preset_literature" to "精選文學文庫",
        
        "hot_search_title" to "大家都在搜",
        "hot_kw_1" to "宇宙天皇復歸式",
        "hot_kw_2" to "西乃山上帝出發",
        "hot_kw_3" to "天山日記",
        "hot_kw_4" to "主的語錄",
        
        "search_tab_title" to "書名搜尋",
        "search_tab_toc" to "目錄搜尋",
        "search_tab_content" to "正文搜尋",
        "search_hint" to "輸入書名、作者、章節或正文內容...",
        "search_speed_tip" to "⚡ 即時極速搜尋已開啟",
        "search_speed_desc" to "• 書名搜尋：記憶體毫秒級過濾，支援書名、作者與分類\n• 目錄搜尋：跨書籍檢索章節標題，點擊直達\n• 正文搜尋：全文字段即時檢索，提供高亮上下文片段",
        "recent_searches" to "最近搜尋",
        "clear_history" to "清空",
        "no_results_found" to "未找到匹配內容",
        "read_action" to "閱讀",
        "back_to_shelf" to "書架",
        "search_in_book" to "搜尋",
        "clear_highlight" to "清除高亮",
        "highlight_active_tip" to "已高亮檢索詞：",
        
        "settings_title" to "設定與偏好",
        "section_appearance" to "語言與顯示偏好",
        "app_language_title" to "應用程式語言",
        "app_language_desc" to "支援多國語言，預設跟隨手機系統",
        "app_font_size_title" to "應用程式字型大小",
        "app_font_size_desc" to "調節整個軟體的文字顯示尺寸",
        "font_small" to "小",
        "font_standard" to "標準",
        "font_large" to "大",
        "font_huge" to "超大",
        "font_small_desc" to "緊湊排版",
        "font_standard_desc" to "預設尺寸",
        "font_large_desc" to "清晰易讀",
        "font_huge_desc" to "關懷大字",
        "reader_bg_color" to "閱讀器預設底色",
        "reader_bg_bamboo" to "竹青護眼 (#EBF3E8)",
        "search_speed_label" to "搜尋回應延遲",
        "search_speed_val" to "120ms (即時極速)",
        "section_library_security" to "本地文庫與安全",
        "supported_formats_label" to "本機支援格式",
        "supported_formats_val" to "TXT, DOCX, EPUB",
        "privacy_label" to "隱私模式",
        "privacy_val" to "100% 本機儲存 · 無雲端上傳",
        "core_engine_label" to "核心引擎",
        "core_engine_val" to "High-Performance Room FullText",
        
        "dialog_import_title" to "匯入本機書籍與文件",
        "btn_select_file" to "選擇本機檔案 (TXT / DOCX / EPUB)",
        "import_custom_title" to "快速貼上創作",
        "field_book_title" to "書籍標題",
        "field_book_author" to "作者名稱",
        "field_category" to "分類",
        "field_content" to "書籍內容",
        "btn_confirm_import" to "確認匯入",
        "btn_cancel" to "取消",
        "btn_delete" to "刪除",
        "book_action_read" to "開始閱讀",
        "book_action_favorite" to "收藏",
        "book_action_unfavorite" to "取消收藏",
        "book_action_delete" to "刪除書籍",
        "view_mode_spine" to "實體書脊",
        "view_mode_grid" to "網格封面",
        "view_mode_list" to "詳細列表",
        
        "tab_shelf" to "書架",
        "tab_search" to "搜尋",
        "tab_notes" to "筆記",
        "tab_mine" to "我的",
        "profile_user_title" to "靜心讀者",
        "profile_user_subtitle" to "墨香致遠 · 知行合一",
        "stat_total_books" to "藏書總數",
        "stat_read_books" to "已讀書籍",
        "stat_focus_days" to "閱讀專注",
        "stat_books_unit" to "本",
        "stat_days_unit" to "連續 7 天",
        "setting_theme_title" to "全域主題風格",
        "setting_ambiance_title" to "閱讀場景模式",
        "setting_engine_title" to "閱讀排版引擎",
        "setting_engine_sub" to "純淨黑白排版 / 原生 EPUB 富文字渲染",
        "setting_search_title" to "全文搜尋模式",
        "setting_search_sub" to "長文段落匹配 & 智慧錯別字容錯模糊檢索",
        "app_font_size_title" to "應用程式字型大小",
        "app_font_size_desc" to "支援全域文字縮放，關懷視力不佳讀者",
        "app_language_title" to "應用程式語言",
        "app_language_desc" to "支援全介面切換中文、英文、韓文",
        "dialog_done" to "完成",
        "dialog_cancel" to "取消",
        "reader_toc" to "目錄",
        "reader_notes" to "筆記",
        "reader_ambiance" to "氛圍",
        "reader_day" to "日間",
        "reader_night" to "夜間",
        "reader_settings" to "設定",
        "reader_pref_title" to "閱讀偏好與排版",
        "paper_texture" to "精選護眼紙張",
        "font_size" to "字號大小",
        "font_zoom_out" to "A - 縮小",
        "font_zoom_in" to "A + 放大",
        "line_spacing" to "行間距",
        "spacing_compact" to "緊湊",
        "spacing_standard" to "標準",
        "spacing_relaxed" to "寬鬆",
        "page_turn_mode" to "翻頁模式",
        "mode_instant" to "極速翻頁",
        "mode_simulation" to "真實書籍翻頁",
        "mode_scroll" to "上下捲動"
    )

    // English
    private val stringsEn = mapOf(
        "app_name" to "Reader",
        "app_brand_title" to "Reader · Pure Reading",
        "app_brand_subtitle" to "Immersive Typography · Ultra-Fast Search",
        "app_brand_version" to "Version v10.0.0 · Local Secure Engine",
        "tab_bookshelf" to "Bookshelf",
        "tab_search" to "Search",
        "tab_companion" to "Companion",
        "tab_reading" to "Reading",
        "tab_profile" to "Settings",
        
        "category_default" to "Default Shelf",
        "category_all" to "All Books",
        "category_hymn" to "Hymns",
        "category_favorites" to "Favorites",
        
        "search_books_hint" to "Search books",
        "empty_shelf_title" to "Bookshelf is empty",
        "empty_shelf_desc" to "Tap '+' at the top right or click below to import your eBooks or documents",
        "btn_import_book" to "Import Books / Documents",
        "btn_preset_literature" to "Preset Classics Library",
        
        "hot_search_title" to "Trending Searches",
        "hot_kw_1" to "Cosmic Emperor Restoration",
        "hot_kw_2" to "Mount Sinai Journey",
        "hot_kw_3" to "Tianshan Diary",
        "hot_kw_4" to "Lord's Quotes",
        
        "search_tab_title" to "Title Search",
        "search_tab_toc" to "TOC Search",
        "search_tab_content" to "Content Search",
        "search_hint" to "Search title, author, chapter or text...",
        "search_speed_tip" to "⚡ Real-time ultra-fast search enabled",
        "search_speed_desc" to "• Title Search: In-memory millisecond filter\n• TOC Search: Search chapters across books\n• Content Search: Full-text search with highlighted context snippets",
        "recent_searches" to "Recent Searches",
        "clear_history" to "Clear",
        "no_results_found" to "No matching content found",
        "read_action" to "Read",
        "back_to_shelf" to "Shelf",
        "search_in_book" to "Search",
        "clear_highlight" to "Clear Highlight",
        "highlight_active_tip" to "Highlighted query: ",
        
        "settings_title" to "Settings & Preferences",
        "section_appearance" to "Language & Display",
        "app_language_title" to "App Language",
        "app_language_desc" to "Supports multiple languages, follows system by default",
        "app_font_size_title" to "Global App Font Size",
        "app_font_size_desc" to "Adjust the overall font scaling across the app",
        "font_small" to "Small",
        "font_standard" to "Standard",
        "font_large" to "Large",
        "font_huge" to "Extra Large",
        "font_small_desc" to "Compact",
        "font_standard_desc" to "Default size",
        "font_large_desc" to "Easy to read",
        "font_huge_desc" to "Large text",
        "reader_bg_color" to "Default Reader Background",
        "reader_bg_bamboo" to "Bamboo Green (#EBF3E8)",
        "search_speed_label" to "Search Response Latency",
        "search_speed_val" to "120ms (Real-time Fast)",
        "section_library_security" to "Local Library & Security",
        "supported_formats_label" to "Supported File Formats",
        "supported_formats_val" to "TXT, DOCX, EPUB",
        "privacy_label" to "Privacy Mode",
        "privacy_val" to "100% On-Device · No Cloud Upload",
        "core_engine_label" to "Core Engine",
        "core_engine_val" to "High-Performance Room FullText",
        
        "dialog_import_title" to "Import Local Books & Documents",
        "btn_select_file" to "Choose File (TXT / DOCX / EPUB)",
        "import_custom_title" to "Quick Paste & Create",
        "field_book_title" to "Book Title",
        "field_book_author" to "Author",
        "field_category" to "Category",
        "field_content" to "Book Content",
        "btn_confirm_import" to "Import",
        "btn_cancel" to "Cancel",
        "btn_delete" to "Delete",
        "book_action_read" to "Start Reading",
        "book_action_favorite" to "Favorite",
        "book_action_unfavorite" to "Unfavorite",
        "book_action_delete" to "Delete Book",
        "view_mode_spine" to "Book Spine",
        "view_mode_grid" to "Cover Grid",
        "view_mode_list" to "Detailed List",

        "tab_shelf" to "Bookshelf",
        "tab_search" to "Search",
        "tab_notes" to "Notes",
        "tab_mine" to "Mine",
        "app_font_size_title" to "Global App Font Size",
        "app_font_size_desc" to "Adjust system-wide font scale for accessibility",
        "app_language_title" to "Global App Language",
        "app_language_desc" to "Switch app language dynamically",
        "dialog_done" to "Done",
        "dialog_cancel" to "Cancel",
        "setting_theme_title" to "App Theme",
        "setting_ambiance_title" to "Reading Ambiance",
        "setting_engine_title" to "Typesetting Engine",
        "setting_engine_sub" to "Clean Black & White / Native Rich Text",
        "setting_search_title" to "Full Text Search",
        "setting_search_sub" to "Long Paragraph Matching & Fuzzy Search"
    )

    // Korean (한국어)
    private val stringsKo = mapOf(
        "app_name" to "독총서",
        "app_brand_title" to "독총서 · 순수 독서",
        "app_brand_subtitle" to "몰입형 조판 · 밀리초 초고속 검색",
        "app_brand_version" to "현재 버전 v10.0.0 · 로컬 보안 엔진",
        "tab_bookshelf" to "서가",
        "tab_search" to "검색",
        "tab_companion" to "책동무",
        "tab_reading" to "읽는 중",
        "tab_profile" to "설정",
        "tab_shelf" to "서가",
        "tab_notes" to "노트",
        "tab_mine" to "내 정보",
        
        "category_default" to "기본 서가",
        "category_all" to "모든 책",
        "category_hymn" to "노래집",
        "category_favorites" to "즐겨찾기",
        
        "search_books_hint" to "책 검색",
        "empty_shelf_title" to "서가가 비어 있습니다",
        "empty_shelf_desc" to "오른쪽 상단 '+' 또는 아래 버튼을 눌러 전자책이나 문서를 가져오세요",
        "btn_import_book" to "책 / 문서 가져오기",
        "btn_preset_literature" to "추천 문학 도서관",
        
        "hot_search_title" to "인기 검색어",
        "hot_kw_1" to "우주천황복귀식",
        "hot_kw_2" to "시내산출발",
        "hot_kw_3" to "천산일기",
        "hot_kw_4" to "주의 어록",
        
        "search_tab_title" to "제목 검색",
        "search_tab_toc" to "목차 검색",
        "search_tab_content" to "본문 검색",
        "search_hint" to "책 제목, 저자, 목차 또는 본문 검색...",
        "search_speed_tip" to "⚡ 실시간 초고속 검색 활성화됨",
        "search_speed_desc" to "• 제목 검색: 메모리 기반 밀리초 필터링\n• 목차 검색: 모든 도서의 챕터 검색\n• 본문 검색: 하이라이트 문맥 스니펫 전문 검색",
        "recent_searches" to "최근 검색",
        "clear_history" to "지우기",
        "no_results_found" to "일치하는 결과가 없습니다",
        "read_action" to "읽기",
        "back_to_shelf" to "서가",
        "search_in_book" to "검색",
        "clear_highlight" to "강조 해제",
        "highlight_active_tip" to "강조된 검색어: ",
        
        "settings_title" to "설정 및 환경설정",
        "section_appearance" to "언어 및 디스플레이",
        "app_language_title" to "앱 표시 언어",
        "app_language_desc" to "다국어 지원, 기본값은 휴대폰 시스템 언어",
        "app_font_size_title" to "앱 전체 글꼴 크기",
        "app_font_size_desc" to "앱 전반의 글꼴 크기를 조절합니다",
        "font_small" to "작게",
        "font_standard" to "표준",
        "font_large" to "크게",
        "font_huge" to "아주 크게",
        "font_small_desc" to "조밀한 조판",
        "font_standard_desc" to "기본 크기",
        "font_large_desc" to "읽기 쉬움",
        "font_huge_desc" to "큰 글씨",
        "reader_bg_color" to "리더기 기본 배경",
        "reader_bg_bamboo" to "대나무 녹색 (#EBF3E8)",
        "search_speed_label" to "검색 응답 지연",
        "search_speed_val" to "120ms (실시간 고속)",
        "section_library_security" to "로컬 도서관 및 보안",
        "supported_formats_label" to "지원 파일 형식",
        "supported_formats_val" to "TXT, DOCX, EPUB",
        "privacy_label" to "개인정보 보호 모드",
        "privacy_val" to "100% 로컬 저장 · 클라우드 업로드 없음",
        "core_engine_label" to "핵심 엔진",
        "core_engine_val" to "High-Performance Room FullText",
        
        "dialog_import_title" to "로컬 도서 및 문서 가져오기",
        "btn_select_file" to "로컬 파일 선택 (TXT / DOCX / EPUB)",
        "import_custom_title" to "빠른 붙여넣기 및 생성",
        "field_book_title" to "책 제목",
        "field_book_author" to "저자 이름",
        "field_category" to "카테고리",
        "field_content" to "책 내용",
        "btn_confirm_import" to "가져오기 확인",
        "btn_cancel" to "취소",
        "btn_delete" to "삭제",
        "book_action_read" to "읽기 시작",
        "book_action_favorite" to "즐겨찾기",
        "book_action_unfavorite" to "즐겨찾기 취소",
        "book_action_delete" to "책 삭제",
        "view_mode_spine" to "책등 모드",
        "view_mode_grid" to "표지 그리드",
        "view_mode_list" to "상세 목록",

        "app_font_size_title" to "앱 전체 글꼴 크기",
        "app_font_size_desc" to "시력 보호를 위한 전체 텍스트 크기 조절",
        "app_language_title" to "앱 전체 언어",
        "app_language_desc" to "한국어, 영어, 중국어 간체/번체 지원",
        "dialog_done" to "완료",
        "dialog_cancel" to "취소",
        "setting_theme_title" to "전체 테마 스타일",
        "setting_ambiance_title" to "독서 분위기 모드",
        "setting_engine_title" to "독서 조판 엔진",
        "setting_engine_sub" to "흑백 미니멀 조판 / 네이티브 EPUB 렌더링",
        "setting_search_title" to "전문 검색 모드",
        "setting_search_sub" to "장문 문단 일치 및 스마트 오타 허용 퍼지 검색"
    )
}

