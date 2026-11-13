package uz.dream.app.ui

import android.content.res.Configuration
import android.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import uz.dream.app.R
import uz.dream.app.data.Lang

/** The language the user picked. Content (cards, courses) reads it to choose the right column. */
val LocalLang = staticCompositionLocalOf { Lang.UZ }

/**
 * Switches the whole app to another language instantly, without restarting the screen.
 *
 * stringResource(...) reads strings through LocalResources (which comes from LocalContext).
 * We give both a context whose configuration has the chosen locale,
 * so Android picks values-uz / values-ru / values (English).
 */
@Composable
fun ProvideLanguage(lang: Lang, content: @Composable () -> Unit) {
    val base = LocalContext.current
    val systemConfig = LocalConfiguration.current
    val localized = remember(lang, base, systemConfig) {
        val config = Configuration(systemConfig)
        config.setLocale(lang.locale)
        config.setLayoutDirection(lang.locale)
        ContextThemeWrapper(base, R.style.Theme_Dream).apply { applyOverrideConfiguration(config) }
    }
    CompositionLocalProvider(
        LocalContext provides localized,
        LocalConfiguration provides localized.resources.configuration,
        LocalResources provides localized.resources,
        LocalLang provides lang,
        content = content
    )
}
