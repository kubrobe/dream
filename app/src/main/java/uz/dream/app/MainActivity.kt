package uz.dream.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.dream.app.data.ThemeMode
import uz.dream.app.ui.DreamRoot
import uz.dream.app.ui.DreamViewModel
import uz.dream.app.ui.ProvideLanguage
import uz.dream.app.ui.theme.DreamTheme

class MainActivity : ComponentActivity() {
    private val vm: DreamViewModel by viewModels { DreamViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        setContent {
            val lang by vm.lang.collectAsStateWithLifecycle()
            val theme by vm.theme.collectAsStateWithLifecycle()
            val dark = when (theme) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            // Every screen starts with a gradient header, so status bar icons are always light.
            // Navigation bar icons follow the app's theme (light icons on dark, dark icons on light).
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                    navigationBarStyle = if (dark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    }
                )
                onDispose { }
            }
            ProvideLanguage(lang) {
                DreamTheme(dark) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        DreamRoot(vm)
                    }
                }
            }
        }
    }
}
