package uz.dream.app

import android.app.Application
import android.content.Context
import uz.dream.app.data.AppDatabase
import uz.dream.app.data.DreamRepository
import uz.dream.app.data.Settings
import uz.dream.app.data.remote.SupabaseClient

/** Created once when the app starts. Holds objects the whole app shares. */
class DreamApp : Application() {
    val repository: DreamRepository by lazy {
        DreamRepository(
            db = AppDatabase.get(this),
            prefs = getSharedPreferences("session", Context.MODE_PRIVATE),
            api = SupabaseClient()
        )
    }

    /** Language and theme. Kept apart from the session so they survive logout. */
    val settings: Settings by lazy {
        Settings(getSharedPreferences("settings", Context.MODE_PRIVATE))
    }
}
