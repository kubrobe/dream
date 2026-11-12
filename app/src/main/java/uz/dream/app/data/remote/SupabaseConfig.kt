package uz.dream.app.data.remote

/**
 * Where the backend lives. Fill both values from the Supabase dashboard:
 *   Project Settings -> API  (Project URL)  and  API Keys (anon / publishable key).
 *
 * The anon / publishable key is made to be shipped inside apps: Row Level Security
 * in supabase/schema.sql decides what each user may read and write.
 * NEVER put the service_role / secret key or the database password here.
 *
 * While both are empty the app runs in "local mode": accounts and data stay on the phone.
 */
object SupabaseConfig {
    const val URL = "https://fryulyyrppyimbshsnvf.supabase.co"
    const val ANON_KEY = ""   // "eyJ..." or "sb_publishable_..."
}
