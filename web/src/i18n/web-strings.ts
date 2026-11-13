// Texts that exist only on the website. Texts shared with the Android app come from
// app-strings.json (generated from the app's strings.xml by `npm run strings`).
// Placeholders use the Android style: %1$s, %2$d.
type Entry = { en: string; ru: string; uz: string };

export const WEB_STRINGS: Record<string, Entry> = {
  nav_how: { en: "How it works", ru: "Как это работает", uz: "Qanday ishlaydi" },
  nav_facts: { en: "Sources", ru: "Источники", uz: "Manbalar" },
  nav_courses: { en: "Courses", ru: "Курсы", uz: "Kurslar" },
  nav_login: { en: "Log in", ru: "Войти", uz: "Kirish" },
  nav_signup: { en: "Sign up free", ru: "Регистрация", uz: "Roʻyxatdan oʻtish" },
  nav_open_app: { en: "Open my path", ru: "Открыть мой путь", uz: "Yoʻlimni ochish" },
  skip_to_content: { en: "Skip to content", ru: "Перейти к содержимому", uz: "Asosiy qismga oʻtish" },

  hero_title: {
    en: "Your way to a Korean university, without paying an agency",
    ru: "Твой путь в корейский вуз без платных агентств",
    uz: "Koreya universitetiga yoʻl, agentlikka pul toʻlamasdan",
  },
  hero_sub: {
    en: "Dream walks you from Tashkent to Seoul step by step. Every fact shows its source, the guide covers your first months in Korea, and students who have already done it answer your questions.",
    ru: "Dream ведёт тебя из Ташкента в Сеул шаг за шагом. У каждого факта есть источник, гид помогает в первые месяцы в Корее, а студенты, которые уже прошли этот путь, отвечают на вопросы.",
    uz: "Dream sizni Toshkentdan Seulgacha qadamma-qadam olib boradi. Har bir faktning manbasi bor, qoʻllanma Koreyadagi ilk oylarda yordam beradi, bu yoʻlni bosib oʻtgan talabalar esa savollaringizga javob beradi.",
  },
  hero_cta: { en: "Start my path", ru: "Начать мой путь", uz: "Yoʻlimni boshlash" },
  hero_cta2: { en: "See the steps", ru: "Посмотреть шаги", uz: "Qadamlarni koʻrish" },
  hero_free: { en: "Free. One account for the website and the Android app.", ru: "Бесплатно. Один аккаунт для сайта и Android-приложения.", uz: "Bepul. Sayt va Android ilova uchun bitta akkaunt." },
  route_from: { en: "Tashkent", ru: "Ташкент", uz: "Toshkent" },
  route_to: { en: "Seoul", ru: "Сеул", uz: "Seul" },
  route_steps: { en: "%1$d steps", ru: "%1$d {шаг|шага|шагов}", uz: "%1$d qadam" },
  route_label: { en: "The five stages of the path from Tashkent to Seoul", ru: "Пять этапов пути из Ташкента в Сеул", uz: "Toshkentdan Seulgacha yoʻlning besh bosqichi" },

  parts_title: { en: "One place for the whole journey", ru: "Всё путешествие в одном месте", uz: "Butun yoʻl bir joyda" },
  part_path_title: { en: "The path", ru: "Путь", uz: "Yoʻl" },
  part_path_body: {
    en: "%1$d steps from choosing a programme to boarding the plane. Tick what you have done and Dream shows what comes next.",
    ru: "%1$d {шаг|шага|шагов}: от выбора программы до посадки в самолёт. Отмечай сделанное, и Dream покажет, что дальше.",
    uz: "Dastur tanlashdan samolyotga chiqishgacha %1$d qadam. Bajarganingizni belgilang, Dream keyingi qadamni koʻrsatadi.",
  },
  part_path_try: { en: "Try it: tick a step", ru: "Попробуй: отметь шаг", uz: "Sinab koʻring: qadamni belgilang" },
  part_guide_title: { en: "After you land", ru: "После прилёта", uz: "Yetib kelgach" },
  part_guide_body: {
    en: "Registration card, health insurance, the rules for part-time work, a bank account, a phone number: what to do in your first weeks.",
    ru: "Карта иностранца, страховка, правила подработки, банк, номер телефона: что сделать в первые недели.",
    uz: "Xorijlik kartasi, sugʻurta, qisman ishlash qoidalari, bank, telefon raqami: ilk haftalarda nima qilish kerak.",
  },
  part_courses_title: { en: "Korean from real teachers", ru: "Корейский от живых преподавателей", uz: "Haqiqiy oʻqituvchilardan koreys tili" },
  part_courses_body: {
    en: "Recorded lessons. Every course comes with a room for its students and 3 questions to the teacher.",
    ru: "Записанные уроки. У каждого курса есть комната для учеников и 3 вопроса преподавателю.",
    uz: "Yozib olingan darslar. Har bir kursda talabalar xonasi va oʻqituvchiga 3 ta savol bor.",
  },

  facts_title: { en: "Every fact says where it came from", ru: "У каждого факта есть источник", uz: "Har bir faktning manbasi bor" },
  facts_body: {
    en: "Visa rules, deadlines and amounts change. So each card names its source and the date we last checked it. If we have not checked something yet, the card says so instead of guessing.",
    ru: "Правила визы, сроки и суммы меняются. Поэтому на каждой карточке указан источник и дата последней проверки. Если мы ещё не проверили, карточка так и говорит, а не угадывает.",
    uz: "Viza qoidalari, muddatlar va summalar oʻzgaradi. Shuning uchun har bir kartada manba va oxirgi tekshiruv sanasi koʻrsatilgan. Hali tekshirilmagan boʻlsa, karta taxmin qilmaydi, shuni ochiq aytadi.",
  },
  facts_verified: { en: "Checked against the source on a date", ru: "Сверено с источником в указанную дату", uz: "Koʻrsatilgan sanada manba bilan solishtirilgan" },
  facts_check: { en: "Not checked yet: compare before you act", ru: "Ещё не проверено: сверь перед тем, как действовать", uz: "Hali tekshirilmagan: harakat qilishdan oldin solishtiring" },
  facts_tip: { en: "Advice from Dream, not a fact", ru: "Совет Dream, а не факт", uz: "Dream maslahati, fakt emas" },
  facts_count: { en: "cards", ru: "{карточка|карточки|карточек}", uz: "ta karta" },
  facts_live: { en: "Live from the Dream database", ru: "Загружено из базы Dream", uz: "Dream bazasidan yuklandi" },
  facts_seed: { en: "Built-in copy: the database is not reachable right now", ru: "Встроенная копия: база сейчас недоступна", uz: "Ichki nusxa: baza hozir mavjud emas" },

  split_title: { en: "85% of every course goes to the teacher", ru: "85% каждого курса получает преподаватель", uz: "Har bir kursning 85% oʻqituvchiga tushadi" },
  split_body: {
    en: "Teachers sell recorded lessons on Dream. The platform keeps 15% to keep the lights on. The split is calculated on our server and saved with every purchase.",
    ru: "Преподаватели продают записанные уроки на Dream. Платформа оставляет себе 15%, чтобы работать дальше. Доли считает наш сервер и сохраняет с каждой покупкой.",
    uz: "Oʻqituvchilar Dream’da yozib olingan darslarni sotadi. Platforma ishlashda davom etish uchun 15% oladi. Ulushlarni serverimiz hisoblaydi va har bir xarid bilan saqlaydi.",
  },
  split_pick: { en: "Pick a course", ru: "Выбери курс", uz: "Kursni tanlang" },

  rooms_title: { en: "Here people help each other, not only the app", ru: "Здесь помогают люди, а не только приложение", uz: "Bu yerda faqat ilova emas, odamlar ham yordam beradi" },
  rooms_body: {
    en: "Every stage of the path and every course has its own room. Ask the student who did the same step last year.",
    ru: "У каждого этапа пути и каждого курса своя комната. Спроси того, кто прошёл этот шаг в прошлом году.",
    uz: "Yoʻlning har bir bosqichi va har bir kursning oʻz xonasi bor. Bu qadamni oʻtgan yili bosib oʻtgan talabadan soʻrang.",
  },
  rooms_topic: { en: "Rooms for the path and the guide", ru: "Комнаты пути и гида", uz: "Yoʻl va qoʻllanma xonalari" },
  rooms_course: { en: "Course rooms", ru: "Комнаты курсов", uz: "Kurs xonalari" },

  unis_land_title: { en: "Universities see you only if you say yes", ru: "Вузы видят тебя, только если ты согласен", uz: "Universitetlar sizni faqat roziligingiz bilan koʻradi" },
  unis_land_body: {
    en: "Mark the universities you are interested in. Your name, email and goal go on their list only after you tick the consent box, and you can withdraw at any time.",
    ru: "Отметь вузы, которые тебе интересны. Имя, почта и цель попадут в их список только после галочки согласия, и его можно отозвать в любой момент.",
    uz: "Sizni qiziqtirgan universitetlarni belgilang. Ismingiz, pochtangiz va maqsadingiz faqat rozilik belgisidan keyin ularning roʻyxatiga tushadi, uni istalgan vaqtda qaytarib olish mumkin.",
  },

  cta_title: { en: "Start today. It is free.", ru: "Начни сегодня. Это бесплатно.", uz: "Bugun boshlang. Bu bepul." },
  cta_body: {
    en: "Make an account once and use it on this website and in the Dream app for Android.",
    ru: "Создай аккаунт один раз и пользуйся им на сайте и в приложении Dream для Android.",
    uz: "Akkauntni bir marta yarating va undan shu saytda hamda Android uchun Dream ilovasida foydalaning.",
  },
  footer_github: { en: "Source code on GitHub", ru: "Исходный код на GitHub", uz: "GitHub’dagi manba kodi" },
  footer_api: { en: "API status", ru: "Статус API", uz: "API holati" },
  footer_demo: {
    en: "Courses, teachers, payments and the university list are a demo for now.",
    ru: "Курсы, преподаватели, оплата и список вузов пока демо.",
    uz: "Kurslar, oʻqituvchilar, toʻlov va universitetlar roʻyxati hozircha demo.",
  },

  // web app
  tab_rooms: { en: "Rooms", ru: "Комнаты", uz: "Xonalar" },
  loading: { en: "Loading", ru: "Загрузка", uz: "Yuklanmoqda" },
  close: { en: "Close", ru: "Закрыть", uz: "Yopish" },
  open_room: { en: "Open the room", ru: "Открыть комнату", uz: "Xonani ochish" },
  read: { en: "Read", ru: "Читать", uz: "Oʻqish" },
  hello: { en: "Hi, %1$s", ru: "Привет, %1$s", uz: "Salom, %1$s" },
  step_n: { en: "Step %1$d", ru: "Шаг %1$d", uz: "%1$d-qadam" },
  stage_n: { en: "Stage %1$d", ru: "Этап %1$d", uz: "%1$d-bosqich" },
  steps_in_stage: { en: "%1$d of %2$d", ru: "%1$d из %2$d", uz: "%2$d tadan %1$d" },
  course_room_locked: { en: "Buy the course to join its room", ru: "Купи курс, чтобы войти в комнату", uz: "Xonaga kirish uchun kursni sotib oling" },
  go_to_course: { en: "Go to the course", ru: "К курсу", uz: "Kursga oʻtish" },
  room_poll_note: { en: "New messages appear every few seconds", ru: "Новые сообщения появляются каждые несколько секунд", uz: "Yangi xabarlar har bir necha soniyada chiqadi" },
  all_rooms: { en: "All rooms", ru: "Все комнаты", uz: "Barcha xonalar" },
  account: { en: "Account", ru: "Аккаунт", uz: "Akkaunt" },
  settings: { en: "Settings", ru: "Настройки", uz: "Sozlamalar" },
  saved: { en: "Saved", ru: "Сохранено", uz: "Saqlandi" },
  not_found_title: { en: "This page does not exist", ru: "Такой страницы нет", uz: "Bunday sahifa yoʻq" },
  not_found_body: { en: "The link may be old. Go back to the start.", ru: "Возможно, ссылка устарела. Вернись в начало.", uz: "Havola eskirgan boʻlishi mumkin. Boshiga qayting." },
  home: { en: "Home page", ru: "На главную", uz: "Bosh sahifa" },
  lesson_n: { en: "Lesson %1$d", ru: "Урок %1$d", uz: "%1$d-dars" },
  paying: { en: "Paying", ru: "Оплата", uz: "Toʻlanmoqda" },
  split_from_server: { en: "Split calculated by the Dream server", ru: "Доли посчитал сервер Dream", uz: "Ulushlarni Dream serveri hisobladi" },
  sending: { en: "Sending", ru: "Отправка", uz: "Yuborilmoqda" },
  card_not_found: { en: "Card not found", ru: "Карточка не найдена", uz: "Karta topilmadi" },
  web_and_app: { en: "The same account works in the Android app.", ru: "Этот же аккаунт работает в Android-приложении.", uz: "Shu akkaunt Android ilovada ham ishlaydi." },
};
