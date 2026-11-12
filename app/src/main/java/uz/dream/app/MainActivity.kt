package uz.dream.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import uz.dream.app.ui.DreamRoot
import uz.dream.app.ui.DreamViewModel
import uz.dream.app.ui.theme.DreamTheme

class MainActivity : ComponentActivity() {
    private val vm: DreamViewModel by viewModels { DreamViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Every screen starts with a turquoise header, so the status bar icons must be light
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        setContent {
            DreamTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    DreamRoot(vm)
                }
            }
        }
    }
}
