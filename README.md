# Dream (Android)

Dream helps students from Uzbekistan get into a university in South Korea without paying a consulting agency.
The app speaks **Uzbek (Latin), Russian and English** and switches language instantly, without a restart.

Three connected parts:

1. **Path / Yoʻl / Путь** — the admissions roadmap (Uzbekistan → South Korea) as an animated timeline.
2. **Guide / Qoʻllanma / Гид** — what to do after arriving in Korea, filtered by topic.
3. **Courses / Kurslar / Курсы** — a marketplace of recorded Korean lessons. The teacher gets 85% of the price, the platform keeps 15%. Every course has a discussion room and 3 questions to the teacher.

Plus **Universities** (a student shows interest in a university, only with consent) and **community rooms** for every roadmap section, because people help each other here, not only the app.

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
4. Run the app again. The profile tab shows "Online · Supabase".

**Updating from the Russian-only version:** run the new `supabase/schema.sql` once more. It adds the `*_en` / `*_uz`
columns, fills them, and turns old Russian goals into keys. Until you do, the app shows its built-in translations,
so nothing breaks. The phone's local database is rebuilt on first start (version 2) and refilled from Supabase.

Never put the `service_role` / secret key or the database password in the app or in this repository. The anon key is made to be public: Row Level Security in `schema.sql` decides what each user can read and write.

**Local mode.** While `SupabaseConfig` is empty the app still works: accounts and data stay in the Room database on the phone, and the app says so on the login and profile screens.

## Languages

- **Interface text** lives in `res/values/strings.xml` (English, the default), `res/values-ru/strings.xml` and
  `res/values-uz/strings.xml`. Every key exists in all three. In Android Studio, right-click `strings.xml` →
  **Open Translations Editor** to see them side by side.
- **Switching.** The user picks a language on the login screen or in Profile. `ui/Language.kt` gives Compose a context
  with that locale, so every `stringResource(...)` redraws at once. The choice is saved on the phone (`data/Settings.kt`).
  On first start the app uses the phone's language if it is Uzbek, Russian or English, otherwise Uzbek.
- **Content** (cards, courses, lessons, universities) is stored in all three languages: the Russian original in
  `title`, `body`, ... and translations in `title_en`, `title_uz`, ... `data/Localized.kt` picks the right one and falls
  back to Russian if a translation is missing.
- **Messages from the data layer** (errors, notices) are `UiText` resource ids, translated only when shown.
- Uzbek is written with the official Latin letters `oʻ`, `gʻ` and the apostrophe `ʼ`.

## Design and motion

- **Fonts:** Unbounded (headings) and Onest (body), bundled in `res/font`, both cover Uzbek Latin and Cyrillic
  (SIL Open Font License, see `licenses/`).
- **Light and dark theme**, or follow the phone: Profile → Theme. Colours are in `ui/theme/Theme.kt`.
- **Hangul glyphs** are Dream's signature: tab icons (길 path, 삶 life, 한 Korean, 학 study, 나 me), header watermarks
  and gradient tiles.
- **Motion** (`ui/Motion.kt`): cards spring when pressed, lists slide in one by one when a screen opens, screens slide
  between each other, the tab highlight glides, the progress ring counts up, timeline steps pop when ticked, the heart
  pops on universities, a purchase ends with a small burst, the login form shakes on a wrong password.
- **Lessons** turn lines like `안녕하세요 — hello` into flash cards; *Practice mode* hides translations until tapped.

## How it is built

```
app/src/main/java/uz/dream/app/
  data/
    Entities.kt          Room tables (same shape as the Supabase tables)
    Localized.kt         picks Uzbek / Russian / English text
    Settings.kt          language and theme, saved on the phone
    UiText.kt            translatable messages and UserError
    DreamDao.kt          SQL the app runs on the phone
    DreamRepository.kt   the one place that decides: Supabase or local
    SeedData.kt          built-in copy of the content (generated)
    remote/
      SupabaseClient.kt  HTTPS calls to Supabase Auth and REST (no extra libraries)
      SupabaseConfig.kt  project URL + anon key
  ui/
    DreamViewModel.kt    screen state
    AppNav.kt            routes, the 5 tabs and screen transitions
    Components.kt        Dream's building blocks: header, cards, tiles, buttons, progress ring, segmented switch
    Motion.kt            shared animations
    Language.kt          instant language switching
    theme/Theme.kt       colours (light + dark), fonts, type scale
    screens/             login, roadmap, guide, courses, rooms, universities, profile
supabase/schema.sql      tables + Row Level Security + content (generated)
tools/build_content.py   the single source of the content
```

Data flow: screen → ViewModel → Repository → Supabase (over the internet) → saved into Room → the screen updates by itself from Room.

### Changing the content

Edit the lists in `tools/build_content.py` (every text is `T(ru, en, uz)`), then run `python3 tools/build_content.py`. It rewrites `SeedData.kt` and `supabase/schema.sql` together, so the app and the database never disagree. Run the new `schema.sql` in Supabase.

### Facts and sources

Every card has a source and a status:

- **Checked + date** (Tekshirildi / Проверено) — checked against the source on that date.
- **Needs checking** (Tekshirish kerak / Нужна проверка) — not checked yet. The app says so instead of presenting it as a fact.
- **Dream tip** (Dream maslahati / Совет Dream) — advice, not a fact.

## What is a demo for now

- **Payment is a demo.** No real money moves. The purchase and the 85% / 15% split are saved in the database.
- **Teachers and courses are demo data.** Lessons are short text notes; video upload by teachers is not built yet.
- **Universities are a demo list.** They are not connected to Dream and do not see the interest list yet.
- **Paid lessons are locked in the app only.** The database lets any client read lesson text; lock it with a Row Level Security rule before real sales.
- Rooms refresh every 4 seconds while open (simple polling, not realtime).

## Author

Najmiddin Kurganov, group I24A, Ajou University in Tashkent.
