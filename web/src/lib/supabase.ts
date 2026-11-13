import { createClient } from "@supabase/supabase-js";

// The same Supabase project as the Android app, so the website and the app share accounts and data.
// The publishable (anon) key is made to be public: Row Level Security in supabase/schema.sql
// decides what each user may read and write. Never put the secret / service_role key here.
export const SUPABASE_URL: string = import.meta.env.VITE_SUPABASE_URL || "https://fryulyyrppyimbshsnvf.supabase.co";
export const SUPABASE_KEY: string = import.meta.env.VITE_SUPABASE_ANON_KEY || "sb_publishable_W5GebSnGN1vd2umX-qKGkw_qSNNFSIi";

export const supabase = createClient(SUPABASE_URL, SUPABASE_KEY, {
  auth: { persistSession: true, autoRefreshToken: true, storageKey: "dream.auth" },
});
