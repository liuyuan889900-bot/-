package com.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.AppDatabase
import com.example.data.repository.BookRepository
import com.example.ui.MainAppScreen
import com.example.ui.theme.AppThemeManager
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StitchTokens

class MainActivity : ComponentActivity() {

    private var hasFocus by mutableStateOf(false)
    private var initialBookId by mutableLongStateOf(-1L)
    private var initialAction by mutableStateOf<String?>(null)

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        this.hasFocus = hasFocus
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun hideSystemBars() {
        val controller = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        hideSystemBars()
        window.decorView.post { hideSystemBars() }

        handleIntent(intent)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = BookRepository(database.bookDao(), applicationContext)
        AppThemeManager.init(applicationContext)
        com.example.ui.ambiance.AmbianceManager.init(applicationContext)

        setContent {
            val currentThemePreset by AppThemeManager.currentTheme.collectAsStateWithLifecycle()
            MyApplicationTheme(themePreset = currentThemePreset) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StitchTokens.Background
                ) {
                    MainAppScreen(
                        repository = repository,
                        initialBookId = initialBookId,
                        initialAction = initialAction,
                        hasWindowFocus = hasFocus,
                        onConsumedIntent = {
                            initialBookId = -1L
                            initialAction = null
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val bookId = intent.getLongExtra(EXTRA_BOOK_ID, -1L)
        if (bookId > 0) {
            initialBookId = bookId
        }
        val action = intent.getStringExtra(EXTRA_ACTION)
        if (!action.isNullOrEmpty()) {
            initialAction = action
        }
    }

    companion object {
        const val EXTRA_BOOK_ID = "EXTRA_BOOK_ID"
        const val EXTRA_ACTION = "EXTRA_ACTION"
        const val EXTRA_SOURCE = "EXTRA_SOURCE"
        const val ACTION_SEARCH = "ACTION_SEARCH"
    }
}


