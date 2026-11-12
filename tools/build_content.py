#!/usr/bin/env python3
"""
Single source of truth for Dream's content.

Run:  python3 tools/build_content.py
It writes two files from the lists below, so the app's built-in copy and the
Supabase database always hold the same text:
  - app/src/main/java/uz/dream/app/data/SeedData.kt   (first-start / offline copy)
  - supabase/schema.sql                               (tables + security rules + content)

Rule for every fact card: it has a source and a status.
  verified = checked against the source on the date in `verified_at`
  check    = NOT checked yet -> the app shows "нужна проверка"
  tip      = advice from Dream, not a fact
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TODAY = "2026-11-12"

# (id, kind, section, title, body, source, source_url, status)
CARDS = [
    # ---------------- ROADMAP: Uzbekistan -> South Korea ----------------
    ("r_goal", "roadmap", "lang", "Выбери тип программы",
     "От программы зависит тип визы. Бакалавриат идёт по визе D-2-2, программа обмена — по D-2-6. "
     "Языковые курсы при университете — это отдельная виза D-4.\n\n"
     "Реши сначала, куда ты идёшь: сразу на степень или сначала на язык. От этого зависят документы и сроки.",
     "Sahmyook University — Residence Qualifications for International Students",
     "https://iie.syu.ac.kr/en/student-life/residence", "verified"),
    ("r_topik", "roadmap", "lang", "Сдай TOPIK",
     "TOPIK — государственный экзамен по корейскому. TOPIK I даёт уровни 1–2, TOPIK II — уровни 3–6. "
     "Для программ на корейском вузы обычно просят уровень 3 и выше, но точное требование у каждого вуза своё.\n\n"
     "Даты экзамена в Ташкенте и регистрацию смотри на официальном сайте.",
     "TOPIK — официальный сайт", "https://www.topik.go.kr", "check"),
    ("r_english", "roadmap", "lang", "Английский трек: IELTS или TOEFL",
     "Если программа на английском, вместо TOPIK вуз попросит IELTS или TOEFL. "
     "Минимальный балл зависит от вуза и факультета — смотри Admission Guide конкретного университета.",
     "Study in Korea — портал Министерства образования Кореи", "https://www.studyinkorea.go.kr", "check"),

    ("r_list", "roadmap", "univ", "Составь список из 5–8 вузов",
     "Раздели список на три группы: мечта, реальные, запасные. По каждому вузу выпиши: язык обучения, "
     "требование по языку, стоимость семестра, дедлайн и стипендии для иностранцев.\n\n"
     "В разделе «Вузы» можно показать университету свой интерес.",
     "Совет Dream", "", "tip"),
    ("r_gks", "roadmap", "univ", "Стипендия GKS",
     "Global Korea Scholarship — стипендия правительства Кореи для иностранных студентов. "
     "Подать можно через посольство Кореи или напрямую через университет.\n\n"
     "Что покрывает стипендия и точные сроки набора на твой год — проверь в официальном объявлении.",
     "Study in Korea — раздел GKS", "https://www.studyinkorea.go.kr", "check"),
    ("r_deadlines", "roadmap", "univ", "Следи за сроками подачи",
     "В Корее два семестра: весенний и осенний. Приём документов обычно идёт за несколько месяцев до начала семестра. "
     "Точные даты у каждого вуза свои.",
     "Сайт приёмной комиссии выбранного вуза", "https://www.studyinkorea.go.kr", "check"),

    ("r_docs", "roadmap", "docs", "Аттестат или диплом с апостилем",
     "Вуз попросит документ об образовании и выписку оценок с переводом и легализацией. "
     "Как именно заверять документы из Узбекистана (апостиль или консульская легализация) — уточни в выбранном вузе.",
     "HCCH — статус Конвенции об апостиле", "https://www.hcch.net", "check"),
    ("r_bank", "roadmap", "docs", "Финансовая гарантия",
     "Для визы нужна справка об остатке на банковском счёте. Суммы в разных источниках отличаются: "
     "встречаются 20 млн вон (16 млн для вузов вне столичного региона) и 20 000 долларов.\n\n"
     "Не полагайся на чужие цифры: спроси точную сумму у своего вуза и в посольстве Кореи в Ташкенте.",
     "Международный отдел вуза и посольство Кореи", "https://iie.syu.ac.kr/en/student-life/residence", "check"),
    ("r_letter", "roadmap", "docs", "Мотивационное письмо и учебный план",
     "Пиши конкретно: почему эта специальность, почему этот вуз, что ты будешь делать после выпуска. "
     "Дай прочитать письмо человеку, который уже поступил — спроси в комнате этого раздела.",
     "Совет Dream", "", "tip"),

    ("r_admission", "roadmap", "visa", "Получи Certificate of Admission",
     "После зачисления вуз выдаёт стандартное письмо о зачислении. На его основе подаётся виза. "
     "Подкатегорию визы определяет вуз в этом документе.",
     "Korea Visa Portal", "https://www.visa.go.kr", "check"),
    ("r_visa", "roadmap", "visa", "Подай на визу D-2",
     "Заявление подаётся через Korea Visa Portal или в консульском отделе посольства Кореи. "
     "Список документов и срок рассмотрения уточняй в посольстве в Ташкенте.",
     "Korea Visa Portal", "https://www.visa.go.kr", "check"),

    ("r_insurance", "roadmap", "arrival", "Купи страховку на первые месяцы",
     "Государственная страховка NHIS начинает действовать только после регистрации иностранца в Корее. "
     "Korea University советует купить частную страховку как минимум на первые 90 дней после приезда.",
     "Korea University — Student Guidebook, Insurance",
     "https://sites.google.com/view/ku-student-guidebook/insurance", "verified"),
    ("r_housing", "roadmap", "arrival", "Реши вопрос с жильём",
     "Подай заявку на общежитие сразу после зачисления: мест обычно меньше, чем желающих. "
     "Если снимаешь жильё, сохрани договор — он понадобится для регистрации.",
     "Совет Dream", "", "tip"),

    # ---------------- GUIDE: after arrival ----------------
    ("g_arc", "guide", "first90", "Residence Card — в течение 90 дней",
     "Иностранцы, которые остаются в Корее дольше 90 дней, обязаны зарегистрироваться и подать на Residence Card "
     "(раньше называлась ARC) в течение 90 дней после въезда. За опоздание возможен штраф.\n\n"
     "Не выезжай из Кореи, пока карта не на руках: Ewha предупреждает, что визу в этом случае аннулируют. "
     "По данным BUFS, пошлина за карту — 30 000 вон.",
     "Ewha Womans University — Foreign Resident Registration",
     "https://isa.ewha.ac.kr/oisa/2181/subview.do", "verified"),
    ("g_address", "guide", "first90", "Сообщай о смене адреса",
     "Если ты переехал, о новом адресе нужно сообщить в иммиграционную службу или районную администрацию. "
     "В неофициальных источниках указан срок 14 дней — проверь его на Hi Korea.",
     "Hi Korea — портал иммиграционной службы", "https://www.hikorea.go.kr", "check"),

    ("g_nhis", "guide", "health", "Госстраховка NHIS обязательна",
     "С 1 марта 2021 года иностранные студенты обязательно подключаются к государственной медстраховке NHIS. "
     "Подключение происходит автоматически с даты регистрации иностранца, самому ничего подавать не нужно. "
     "Карту NHIS и инструкцию присылают на твой адрес в Корее.\n\n"
     "Размер ежемесячного взноса меняется — смотри на сайте NHIS.",
     "Seoul National University — National Health Insurance",
     "https://oga.snu.ac.kr/isss/immigration/national-health-insurance/", "verified"),

    ("g_work", "guide", "work", "Подработка только с разрешением",
     "С визой D-2 нельзя просто так работать. Сначала нужно получить разрешение иммиграционной службы, "
     "и только потом выходить на работу. Перед подачей вуз подтверждает заявку.\n\n"
     "Работа без разрешения — это штраф и проблемы с визой.",
     "Korea University — Part-time Work",
     "https://univ.korea.ac.kr/gsc/DegreeUndergraduate/Visa_Immigration/Part-timeWork/Part-timeWork.do", "verified"),
    ("g_work_hours", "guide", "work", "Сколько часов можно работать",
     "Лимит часов зависит от уровня TOPIK, курса и типа программы. Правила обновлялись, и в разных источниках "
     "цифры расходятся (от 10 до 30 часов в будни).\n\n"
     "Актуальную таблицу смотри на Study in Korea и уточняй в международном отделе своего вуза.",
     "Study in Korea — Part-time work", "https://www.studyinkorea.go.kr", "check"),

    ("g_bank", "guide", "life", "Банковский счёт",
     "Счёт обычно открывают после получения Residence Card. Возьми паспорт, карту и справку об обучении. "
     "Условия зависят от банка.",
     "Международный отдел вуза", "", "check"),
    ("g_sim", "guide", "life", "SIM-карта и связь",
     "До получения Residence Card доступны в основном предоплаченные SIM. Контракт на своё имя обычно оформляют уже с картой.",
     "Международный отдел вуза", "", "check"),
    ("g_transport", "guide", "life", "Транспорт",
     "Автобусы и метро оплачиваются транспортной картой (например, T-money), её пополняют в магазинах у дома и на станциях.",
     "Visit Korea", "https://english.visitkorea.or.kr", "check"),
    ("g_emergency", "guide", "life", "Экстренные номера",
     "112 — полиция, 119 — пожарные и скорая, 1345 — контакт-центр иммиграционной службы для иностранцев.",
     "Hi Korea — портал иммиграционной службы", "https://www.hikorea.go.kr", "check"),
]

# (id, title, teacher, level, price_uzs, glyph, description)
COURSES = [
    ("c_hangul", "Хангыль за 7 дней", "Дилноза К. (демо-преподаватель)", "С нуля", 149000, "한",
     "Научишься читать и писать корейский алфавит. Без этого курса дальше никуда."),
    ("c_topik1", "TOPIK I: уверенный старт", "Ким Со-ён (демо-преподаватель)", "A1–A2", 349000, "토",
     "Грамматика и лексика для первого и второго уровня TOPIK."),
    ("c_dorm", "Корейский для общежития", "Жасур Т. (демо-преподаватель)", "A2", 249000, "말",
     "Фразы на каждый день: столовая, офис, аптека, соседи."),
    ("c_interview", "Интервью в вуз на корейском", "Пак Мин-джи (демо-преподаватель)", "B1", 399000, "꿈",
     "Как рассказать о себе и своей цели на собеседовании."),
]

# (id, course_id, title, minutes, free, content)
LESSONS = [
    ("l_hangul_1", "c_hangul", "Шесть базовых гласных", 12, True,
     "ㅏ — а\nㅓ — о (открытое)\nㅗ — о\nㅜ — у\nㅡ — ы\nㅣ — и\n\n"
     "Задание: напиши каждую букву по 10 раз и произнеси вслух."),
    ("l_hangul_2", "c_hangul", "Первые согласные", 15, False,
     "ㄱ — к/г\nㄴ — н\nㄷ — т/д\nㄹ — р/ль\nㅁ — м\nㅂ — п/б\nㅅ — с\nㅇ — не читается в начале слога, «нг» в конце\nㅈ — ч/дж"),
    ("l_hangul_3", "c_hangul", "Как собирается слог", 14, False,
     "Слог — это блок: согласная + гласная (+ согласная снизу).\n\n"
     "ㄱ + ㅏ = 가 (ка)\nㄴ + ㅏ = 나 (на)\nㅎ + ㅏ + ㄴ = 한 (хан)\nㄱ + ㅡ + ㄹ = 글 (кыль)\n\n한글 — хангыль."),
    ("l_hangul_4", "c_hangul", "Читаем первые слова", 16, False,
     "안녕하세요 — здравствуйте\n감사합니다 — спасибо\n네 — да\n아니요 — нет\n학생 — студент\n대학교 — университет"),

    ("l_topik1_1", "c_topik1", "Из чего состоит TOPIK I", 10, True,
     "TOPIK I проверяет аудирование и чтение. Письменной части нет — она появляется в TOPIK II.\n\n"
     "План курса: частицы, числа, вежливый стиль, типовые задания."),
    ("l_topik1_2", "c_topik1", "Частицы 은/는 и 이/가", 18, False,
     "은/는 — тема предложения: 저는 학생이에요 (Я студент).\n"
     "이/가 — подлежащее: 비가 와요 (Идёт дождь).\n\n"
     "После согласной — 은 и 이, после гласной — 는 и 가."),
    ("l_topik1_3", "c_topik1", "Два ряда чисел", 17, False,
     "Корейские: 하나, 둘, 셋, 넷, 다섯 — для счёта предметов и возраста.\n"
     "Сино-корейские: 일, 이, 삼, 사, 오 — для дат, денег, номеров."),
    ("l_topik1_4", "c_topik1", "Вежливый стиль -아요/어요", 20, False,
     "가다 → 가요 (иду)\n먹다 → 먹어요 (ем)\n공부하다 → 공부해요 (учусь)\n\n"
     "Этот стиль подходит почти для любой повседневной ситуации."),

    ("l_dorm_1", "c_dorm", "Знакомство с соседом", 11, True,
     "안녕하세요, 저는 ...입니다 — Здравствуйте, я ...\n"
     "우즈베키스탄에서 왔어요 — Я приехал из Узбекистана\n"
     "잘 부탁드립니다 — Рассчитываю на хорошее отношение"),
    ("l_dorm_2", "c_dorm", "В столовой и магазине", 13, False,
     "이거 주세요 — Дайте это, пожалуйста\n얼마예요? — Сколько стоит?\n"
     "카드 돼요? — Картой можно?\n돼지고기 들어 있어요? — Здесь есть свинина?"),
    ("l_dorm_3", "c_dorm", "В офисе и администрации", 15, False,
     "도와주세요 — Помогите, пожалуйста\n외국인등록증 — карта регистрации иностранца\n"
     "재학증명서 — справка об обучении\n어디에서 신청해요? — Где подать заявление?"),
    ("l_dorm_4", "c_dorm", "В аптеке", 12, False,
     "약국 — аптека\n머리가 아파요 — У меня болит голова\n감기에 걸렸어요 — Я простудился\n약 주세요 — Дайте лекарство"),

    ("l_int_1", "c_interview", "Самопрезентация", 14, True,
     "자기소개 — рассказ о себе. Структура: кто ты → что изучал → почему Корея → цель.\n\n"
     "저는 우즈베키스탄에서 온 ...입니다 — Я ..., приехал из Узбекистана."),
    ("l_int_2", "c_interview", "Мотивация", 16, False,
     "지원 동기 — причина подачи.\n한국에서 공부하고 싶습니다 — Я хочу учиться в Корее.\n"
     "이 전공에 관심이 많습니다 — Мне очень интересна эта специальность."),
    ("l_int_3", "c_interview", "Планы после выпуска", 15, False,
     "졸업 후 계획 — планы после выпуска.\n졸업 후에 고향에 돌아가서 일하고 싶습니다 — "
     "После выпуска я хочу вернуться на родину и работать."),
    ("l_int_4", "c_interview", "Официальный стиль -습니다", 18, False,
     "На интервью используют официальный стиль.\n가다 → 갑니다\n먹다 → 먹습니다\n공부하다 → 공부합니다"),
]

# (id, name, city, type, url)
UNIVERSITIES = [
    ("u_snu", "Seoul National University", "Сеул", "Государственный", "https://www.snu.ac.kr"),
    ("u_kaist", "KAIST", "Тэджон", "Государственный", "https://www.kaist.ac.kr"),
    ("u_yonsei", "Yonsei University", "Сеул", "Частный", "https://www.yonsei.ac.kr"),
    ("u_korea", "Korea University", "Сеул", "Частный", "https://www.korea.ac.kr"),
    ("u_ajou", "Ajou University", "Сувон", "Частный", "https://www.ajou.ac.kr"),
    ("u_inha", "Inha University", "Инчхон", "Частный", "https://www.inha.ac.kr"),
    ("u_pnu", "Pusan National University", "Пусан", "Государственный", "https://www.pusan.ac.kr"),
    ("u_skku", "Sungkyunkwan University", "Сеул / Сувон", "Частный", "https://www.skku.edu"),
]


# ---------------------------------------------------------------- Kotlin
def kt(s: str) -> str:
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$").replace("\n", "\\n") + '"'


def build_kotlin() -> str:
    out = [
        "package uz.dream.app.data",
        "",
        "// GENERATED by tools/build_content.py - do not edit by hand, edit the lists in that script.",
        "// Built-in copy of the content: shown on first start and when Supabase is not reachable.",
        "object SeedData {",
        "    val cards: List<InfoCard> = listOf(",
    ]
    for i, (cid, kind, section, title, body, source, url, status) in enumerate(CARDS):
        verified = TODAY if status == "verified" else ""
        out.append(f"        InfoCard({kt(cid)}, {kt(kind)}, {kt(section)}, {i}, {kt(title)},\n"
                   f"            {kt(body)},\n"
                   f"            {kt(source)}, {kt(url)}, {kt(status)}, {kt(verified)}),")
    out.append("    )")
    out.append("")
    out.append("    val courses: List<Course> = listOf(")
    for i, (cid, title, teacher, level, price, glyph, desc) in enumerate(COURSES):
        out.append(f"        Course({kt(cid)}, {i}, {kt(title)}, {kt(teacher)}, {kt(level)}, {price}L, {kt(glyph)},\n"
                   f"            {kt(desc)}),")
    out.append("    )")
    out.append("")
    out.append("    val lessons: List<Lesson> = listOf(")
    for i, (lid, course, title, minutes, free, content) in enumerate(LESSONS):
        out.append(f"        Lesson({kt(lid)}, {kt(course)}, {i}, {kt(title)}, {minutes}, {'true' if free else 'false'},\n"
                   f"            {kt(content)}),")
    out.append("    )")
    out.append("")
    out.append("    val universities: List<University> = listOf(")
    for i, (uid, name, city, typ, url) in enumerate(UNIVERSITIES):
        out.append(f"        University({kt(uid)}, {i}, {kt(name)}, {kt(city)}, {kt(typ)}, {kt(url)}),")
    out.append("    )")
    out.append("}")
    return "\n".join(out) + "\n"


# ---------------------------------------------------------------- SQL
def sq(s: str) -> str:
    return "'" + s.replace("'", "''") + "'"


SCHEMA = r"""-- Dream: database for the Android app (and later the web version).
-- HOW TO USE: Supabase dashboard -> SQL Editor -> New query -> paste this whole file -> Run.
-- Safe to run again: it never deletes user data, it only (re)creates rules and refreshes content.
-- GENERATED by tools/build_content.py

-- ---------- tables: content (same for everyone) ----------
create table if not exists public.cards (
  id          text primary key,
  kind        text not null check (kind in ('roadmap', 'guide')),
  section     text not null,
  sort        int  not null default 0,
  title       text not null,
  body        text not null,
  source      text not null default '',
  source_url  text not null default '',
  status      text not null default 'check' check (status in ('verified', 'check', 'tip')),
  verified_at text not null default ''
);

create table if not exists public.courses (
  id          text primary key,
  sort        int    not null default 0,
  title       text   not null,
  teacher     text   not null,
  level       text   not null,
  price_uzs   bigint not null check (price_uzs >= 0),
  glyph       text   not null,
  description text   not null
);

create table if not exists public.lessons (
  id        text primary key,
  course_id text not null references public.courses (id) on delete cascade,
  sort      int  not null default 0,
  title     text not null,
  minutes   int  not null default 0,
  free      boolean not null default false,
  content   text not null default ''
);

create table if not exists public.universities (
  id   text primary key,
  sort int  not null default 0,
  name text not null,
  city text not null,
  type text not null,
  url  text not null default ''
);

-- ---------- tables: users and what they do ----------
create table if not exists public.profiles (
  id         uuid primary key references auth.users (id) on delete cascade,
  full_name  text not null default '',
  goal       text not null default '',
  created_at timestamptz not null default now()
);

create table if not exists public.progress (
  user_id    uuid not null default auth.uid() references auth.users (id) on delete cascade,
  card_id    text not null references public.cards (id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (user_id, card_id)
);

-- A purchase. The platform keeps 15%, the teacher gets 85%.
create table if not exists public.enrollments (
  user_id       uuid   not null default auth.uid() references auth.users (id) on delete cascade,
  course_id     text   not null references public.courses (id) on delete cascade,
  price_uzs     bigint not null,
  platform_fee  bigint not null,
  teacher_share bigint not null,
  created_at    timestamptz not null default now(),
  primary key (user_id, course_id),
  check (platform_fee + teacher_share = price_uzs)
);

-- "I am interested in this university". Stored only with the student's consent.
create table if not exists public.interests (
  user_id       uuid not null default auth.uid() references auth.users (id) on delete cascade,
  university_id text not null references public.universities (id) on delete cascade,
  consent       boolean not null check (consent),
  created_at    timestamptz not null default now(),
  primary key (user_id, university_id)
);

-- Community rooms. room_id = 'topic_<section>' or 'course_<course id>'.
create table if not exists public.messages (
  id         bigint generated always as identity primary key,
  room_id    text not null,
  user_id    uuid not null default auth.uid() references auth.users (id) on delete cascade,
  author     text not null,
  body       text not null check (char_length(body) between 1 and 1000),
  created_at timestamptz not null default now()
);
create index if not exists messages_room_idx on public.messages (room_id, created_at);

-- Questions to the teacher (the app allows 3 per course).
create table if not exists public.questions (
  id         bigint generated always as identity primary key,
  course_id  text not null references public.courses (id) on delete cascade,
  user_id    uuid not null default auth.uid() references auth.users (id) on delete cascade,
  body       text not null check (char_length(body) between 1 and 1000),
  answer     text not null default '',
  created_at timestamptz not null default now()
);

-- ---------- a profile row is created automatically for every new account ----------
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  insert into public.profiles (id, full_name)
  values (new.id, coalesce(new.raw_user_meta_data ->> 'full_name', ''))
  on conflict (id) do nothing;
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- ---------- security: Row Level Security ----------
alter table public.cards        enable row level security;
alter table public.courses      enable row level security;
alter table public.lessons      enable row level security;
alter table public.universities enable row level security;
alter table public.profiles     enable row level security;
alter table public.progress     enable row level security;
alter table public.enrollments  enable row level security;
alter table public.interests    enable row level security;
alter table public.messages     enable row level security;
alter table public.questions    enable row level security;

grant usage on schema public to anon, authenticated;
grant select on public.cards, public.courses, public.lessons, public.universities to anon, authenticated;
grant select, insert, update on public.profiles to authenticated;
grant select, insert, delete on public.progress, public.interests to authenticated;
grant select, insert on public.enrollments, public.messages, public.questions to authenticated;

-- content: everyone can read, nobody can change it through the API
drop policy if exists "cards are public" on public.cards;
create policy "cards are public" on public.cards for select using (true);
drop policy if exists "courses are public" on public.courses;
create policy "courses are public" on public.courses for select using (true);
drop policy if exists "lessons are public" on public.lessons;
create policy "lessons are public" on public.lessons for select using (true);
drop policy if exists "universities are public" on public.universities;
create policy "universities are public" on public.universities for select using (true);

-- profile: only the owner
drop policy if exists "own profile read" on public.profiles;
create policy "own profile read" on public.profiles for select to authenticated using (auth.uid() = id);
drop policy if exists "own profile insert" on public.profiles;
create policy "own profile insert" on public.profiles for insert to authenticated with check (auth.uid() = id);
drop policy if exists "own profile update" on public.profiles;
create policy "own profile update" on public.profiles for update to authenticated using (auth.uid() = id) with check (auth.uid() = id);

-- progress, purchases, interests, questions: each user sees and changes only their own rows
drop policy if exists "own progress read" on public.progress;
create policy "own progress read" on public.progress for select to authenticated using (auth.uid() = user_id);
drop policy if exists "own progress insert" on public.progress;
create policy "own progress insert" on public.progress for insert to authenticated with check (auth.uid() = user_id);
drop policy if exists "own progress delete" on public.progress;
create policy "own progress delete" on public.progress for delete to authenticated using (auth.uid() = user_id);

drop policy if exists "own enrollments read" on public.enrollments;
create policy "own enrollments read" on public.enrollments for select to authenticated using (auth.uid() = user_id);
drop policy if exists "own enrollments insert" on public.enrollments;
create policy "own enrollments insert" on public.enrollments for insert to authenticated with check (auth.uid() = user_id);

drop policy if exists "own interests read" on public.interests;
create policy "own interests read" on public.interests for select to authenticated using (auth.uid() = user_id);
drop policy if exists "own interests insert" on public.interests;
create policy "own interests insert" on public.interests for insert to authenticated with check (auth.uid() = user_id);
drop policy if exists "own interests delete" on public.interests;
create policy "own interests delete" on public.interests for delete to authenticated using (auth.uid() = user_id);

drop policy if exists "own questions read" on public.questions;
create policy "own questions read" on public.questions for select to authenticated using (auth.uid() = user_id);
drop policy if exists "own questions insert" on public.questions;
create policy "own questions insert" on public.questions for insert to authenticated with check (auth.uid() = user_id);

-- rooms: any signed-in user reads, and writes only under their own id
drop policy if exists "rooms read" on public.messages;
create policy "rooms read" on public.messages for select to authenticated using (true);
drop policy if exists "rooms write" on public.messages;
create policy "rooms write" on public.messages for insert to authenticated with check (auth.uid() = user_id);

-- ---------- content ----------
"""


def build_sql() -> str:
    out = [SCHEMA]

    out.append("insert into public.cards (id, kind, section, sort, title, body, source, source_url, status, verified_at) values")
    rows = []
    for i, (cid, kind, section, title, body, source, url, status) in enumerate(CARDS):
        verified = TODAY if status == "verified" else ""
        rows.append(f"  ({sq(cid)}, {sq(kind)}, {sq(section)}, {i}, {sq(title)}, {sq(body)}, {sq(source)}, {sq(url)}, {sq(status)}, {sq(verified)})")
    out.append(",\n".join(rows))
    out.append("on conflict (id) do update set kind = excluded.kind, section = excluded.section, sort = excluded.sort, "
               "title = excluded.title, body = excluded.body, source = excluded.source, source_url = excluded.source_url, "
               "status = excluded.status, verified_at = excluded.verified_at;\n")

    out.append("insert into public.courses (id, sort, title, teacher, level, price_uzs, glyph, description) values")
    rows = [f"  ({sq(cid)}, {i}, {sq(title)}, {sq(teacher)}, {sq(level)}, {price}, {sq(glyph)}, {sq(desc)})"
            for i, (cid, title, teacher, level, price, glyph, desc) in enumerate(COURSES)]
    out.append(",\n".join(rows))
    out.append("on conflict (id) do update set sort = excluded.sort, title = excluded.title, teacher = excluded.teacher, "
               "level = excluded.level, price_uzs = excluded.price_uzs, glyph = excluded.glyph, description = excluded.description;\n")

    out.append("insert into public.lessons (id, course_id, sort, title, minutes, free, content) values")
    rows = [f"  ({sq(lid)}, {sq(course)}, {i}, {sq(title)}, {minutes}, {'true' if free else 'false'}, {sq(content)})"
            for i, (lid, course, title, minutes, free, content) in enumerate(LESSONS)]
    out.append(",\n".join(rows))
    out.append("on conflict (id) do update set course_id = excluded.course_id, sort = excluded.sort, title = excluded.title, "
               "minutes = excluded.minutes, free = excluded.free, content = excluded.content;\n")

    out.append("insert into public.universities (id, sort, name, city, type, url) values")
    rows = [f"  ({sq(uid)}, {i}, {sq(name)}, {sq(city)}, {sq(typ)}, {sq(url)})"
            for i, (uid, name, city, typ, url) in enumerate(UNIVERSITIES)]
    out.append(",\n".join(rows))
    out.append("on conflict (id) do update set sort = excluded.sort, name = excluded.name, city = excluded.city, "
               "type = excluded.type, url = excluded.url;\n")
    return "\n".join(out)


if __name__ == "__main__":
    ids = [c[0] for c in CARDS] + [c[0] for c in COURSES] + [l[0] for l in LESSONS] + [u[0] for u in UNIVERSITIES]
    assert len(ids) == len(set(ids)), "duplicate id"
    course_ids = {c[0] for c in COURSES}
    assert all(l[1] in course_ids for l in LESSONS), "lesson points to unknown course"
    assert all(c[7] in ("verified", "check", "tip") for c in CARDS)
    assert all(c[5] for c in CARDS), "every card needs a source"

    kotlin = ROOT / "app/src/main/java/uz/dream/app/data/SeedData.kt"
    kotlin.write_text(build_kotlin(), encoding="utf-8")
    sql = ROOT / "supabase/schema.sql"
    sql.write_text(build_sql(), encoding="utf-8")
    print(f"wrote {kotlin.relative_to(ROOT)} and {sql.relative_to(ROOT)}: "
          f"{len(CARDS)} cards, {len(COURSES)} courses, {len(LESSONS)} lessons, {len(UNIVERSITIES)} universities")
