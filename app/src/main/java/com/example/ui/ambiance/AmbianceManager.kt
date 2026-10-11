package com.example.ui.ambiance

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ReadingAmbiance(
    val id: String,
    val title: String,
    val subtitle: String,
    val poeticTag: String,
    val poemSnippet: String
) {
    NONE("none", "无场景", "纯净安歇 · 什么效果都没有", "默认", "白纸黑字，清心自安"),
    PETALS("petals", "落英缤纷", "花瓣轻旋 · 庭院微风摇曳", "雅致", "幽芳一瓣随风落，淡卷清茶伴此心"),
    WINDOW_RAIN("window_rain", "隔窗听雨", "苹果4S经典水滴 · 玻璃水珠折射与窗外细雨", "真实4S", "水滴凝窗欞，如临雨幕后"),
    RAIN("rain", "听雨读书", "雨中庭院 · 细雨润物飘落", "静谧", "风声雨声读书声，润物细无声"),
    SNOW("snow", "踏雪寻章", "素雪纷飞 · 晶莹六角旋转", "幽远", "素雪纷纷夜读书，清气满乾坤"),
    STARS("stars", "星夜伴读", "明耀星芒 · 错落呼吸闪烁", "深邃", "天宇浩瀚，星汉如洗引遐思"),
    AI_FLOW("ai_flow", "AI 脑波流动光带", "智能流光 · 细长彩色光带如思绪律动", "灵动", "智能流转，浮光掠影映篇章");

    companion object {
        fun fromId(id: String?): ReadingAmbiance {
            return entries.firstOrNull { it.id == id } ?: NONE
        }
    }
}

/**
 * Task 9: Global Ambiance Manager
 * Manages active scene state, low-power mode, and persistence across Bookshelf and Reader screens.
 */
object AmbianceManager {
    private const val PREFS_NAME = "reading_ambiance_prefs"
    private const val KEY_SCENE = "active_ambiance_scene"
    private const val KEY_LOW_POWER = "ambiance_low_power_mode"

    private val _currentScene = MutableStateFlow(ReadingAmbiance.NONE)
    val currentScene: StateFlow<ReadingAmbiance> = _currentScene.asStateFlow()

    private val _isLowPowerMode = MutableStateFlow(false)
    val isLowPowerMode: StateFlow<Boolean> = _isLowPowerMode.asStateFlow()

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val sceneId = prefs.getString(KEY_SCENE, ReadingAmbiance.NONE.id)
            _currentScene.value = ReadingAmbiance.fromId(sceneId)
            _isLowPowerMode.value = prefs.getBoolean(KEY_LOW_POWER, false)
            initialized = true
        } catch (_: Exception) {
            _currentScene.value = ReadingAmbiance.NONE
            _isLowPowerMode.value = false
        }
    }

    fun setScene(context: Context, scene: ReadingAmbiance) {
        _currentScene.value = scene
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_SCENE, scene.id).apply()
        } catch (_: Exception) {}
    }

    fun setLowPowerMode(context: Context, enabled: Boolean) {
        _isLowPowerMode.value = enabled
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_LOW_POWER, enabled).apply()
        } catch (_: Exception) {}
    }
}
