# Dream (Android)

Dream helps students from Uzbekistan get into a university in South Korea without paying a consulting agency. The app is in Russian.

Three connected parts:

1. **Путь** — the admissions roadmap (Uzbekistan → South Korea) as a checklist.
2. **Гид** — what to do after arriving in Korea.
3. **Курсы** — a marketplace of recorded Korean lessons. The teacher gets 85% of the price, the platform keeps 15%. Every course has a discussion room and 3 questions to the teacher.

Plus **Вузы** (a student shows interest in a university, only with consent) and **community rooms** for every roadmap section, because people help each other here, not only the app.

Kotlin + Jetpack Compose (Material 3) + Room + MVVM. Backend: **Supabase** (Auth + PostgreSQL + REST API). The future web version will use the same Supabase project, so web and app share accounts and data.

## Run

1. Android Studio → File → Open → choose this folder (the one with `settings.gradle.kts`).
2. Wait for Gradle sync, then press the green Run button. Needs an emulator or phone with **Android 8.0 (API 26) or newer**.
3. Register a new account in the app and log in.
4. Unit tests: right-click `app/src/test/java/uz/dream/app/MoneyTest.kt` → Run.

## Connect Supabase (one time)

1. In the Supabase dashboard open **SQL Editor → New query**, paste the whole file [`supabase/schema.sql`](supabase/schema.sql) and press **Run**. It creates the tables, the security rules and the content. It is safe to run again.
2. **Authentication → Sign In / Providers → Email**: turn **Confirm email** off (otherwise a new user cannot log in until they click the link in an e-mail).
3. Open `app/src/main/java/uz/dream/app/data/remote/SupabaseConfig.kt` and fill in:
   - `URL` — the Project URL, like `https://abcdefghijkl.supabase.co`
   - `ANON_KEY` — the **anon / publishable** key
4. Run the app again. The profile tab shows "Онлайн: аккаунт и данные хранятся в Supabase".

Never put the `service_role` / secret key or the database password in the app or in this repository. The anon key is made to be public: Row Level Security in `schema.sql` decides what each user can read and write.

**Local mode.** While `SupabaseConfig` is empty the app still works: accounts and data stay in the Room database on the phone, and the app says so on the login and profile screens.

## How it is built

```
app/src/main/java/uz/dream/app/
  data/
    Entities.kt          Room tables (same shape as the Supabase tables)
    DreamDao.kt          SQL the app runs on the phone
    DreamRepository.kt   the one place that decides: Supabase or local
    SeedData.kt          built-in copy of the content (generated)
    remote/
      SupabaseClient.kt  HTTPS calls to Supabase Auth and REST (no extra libraries)
      SupabaseConfig.kt  project URL + anon key
  ui/
    DreamViewModel.kt    screen state
    AppNav.kt            routes and the 5 tabs
    screens/             login, roadmap, guide, courses, rooms, universities, profile
supabase/schema.sql      tables + Row Level Security + content (generated)
tools/build_content.py   the single source of the content
```

Data flow: screen → ViewModel → Repository → Supabase (over the internet) → saved into Room → the screen updates by itself from Room.

### Changing the content

Edit the lists in `tools/build_content.py`, then run `python3 tools/build_content.py`. It rewrites `SeedData.kt` and `supabase/schema.sql` together, so the app and the database never disagree. Run the new `schema.sql` in Supabase.

### Facts and sources

Every card has a source and a status:

- **Проверено + date** — checked against the source on that date.
- **Нужна проверка** — not checked yet. The app says so instead of presenting it as a fact.
- **Совет Dream** — advice, not a fact.

## What is a demo for now

- **Payment is a demo.** No real money moves. The purchase and the 85% / 15% split are saved in the database.
- **Teachers and courses are demo data.** Lessons are short text notes; video upload by teachers is not built yet.
- **Universities are a demo list.** They are not connected to Dream and do not see the interest list yet.
- **Paid lessons are locked in the app only.** The database lets any client read lesson text; lock it with a Row Level Security rule before real sales.
- **Fonts.** The design uses Unbounded (headings) and Onest (body). The app currently uses the system font; see the comment in `ui/theme/Theme.kt` to switch them on.
- Rooms refresh every 4 seconds while open (simple polling, not realtime).

## Author

Najmiddin Kurganov, group I24A, Ajou University in Tashkent.
