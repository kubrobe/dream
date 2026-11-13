package uz.dream.app.data

import android.content.Context
import androidx.annotation.StringRes

/**
 * A message for the user that is translated only when it is shown.
 * The data layer says WHICH message (a string resource id); the screen turns it into
 * Uzbek, Russian or English text with the language the user picked.
 */
sealed interface UiText {
    class Res(@StringRes val id: Int, vararg val args: Any) : UiText
    class Raw(val text: String) : UiText   // text from the server that we have no translation for

    fun resolve(context: Context): String = when (this) {
        is Res -> if (args.isEmpty()) context.getString(id) else context.getString(id, *args)
        is Raw -> text
    }
}

/** A problem the user can fix (empty field, limit reached). */
class UserError(val text: UiText) : Exception(text.toString()) {
    constructor(@StringRes id: Int, vararg args: Any) : this(UiText.Res(id, *args))
}
