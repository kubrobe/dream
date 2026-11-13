package uz.dream.app.data

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/** The three languages of the app. Uzbek is written in the Latin alphabet. */
enum class Lang(val code: String, val nativeName: String, val short: String) {
    UZ("uz", "Oʻzbekcha", "UZ"),
    RU("ru", "Русский", "RU"),
    EN("en", "English", "EN");

    val locale: Locale get() = Locale.forLanguageTag(code)

    companion object {
        fun of(code: String?): Lang? = entries.firstOrNull { it.code == code }
    }
}

/** Follow the phone, or always light, or always dark. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Choices that belong to the phone, not to the account: language and theme. */
class Settings(private val prefs: SharedPreferences) {

    private val _lang = MutableStateFlow(Lang.of(prefs.getString(KEY_LANG, null)) ?: phoneLanguage())
    val lang: StateFlow<Lang> = _lang.asStateFlow()

    private val _theme = MutableStateFlow(
        ThemeMode.entries.firstOrNull { it.name == prefs.getString(KEY_THEME, null) } ?: ThemeMode.SYSTEM
    )
    val theme: StateFlow<ThemeMode> = _theme.asStateFlow()

    fun setLang(lang: Lang) {
        prefs.edit().putString(KEY_LANG, lang.code).apply()
        _lang.value = lang
    }

    fun setTheme(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
        _theme.value = mode
    }

    /** First start: use the phone's language if the app has it, otherwise Uzbek. */
    private fun phoneLanguage(): Lang = Lang.of(Locale.getDefault().language) ?: Lang.UZ

    private companion object {
        const val KEY_LANG = "lang"
        const val KEY_THEME = "theme"
    }
}
