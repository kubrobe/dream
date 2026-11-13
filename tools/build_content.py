#!/usr/bin/env python3
"""
Single source of truth for Dream's content.

Run:  python3 tools/build_content.py
It writes two files from the lists below, so the app's built-in copy and the
Supabase database always hold the same text:
  - app/src/main/java/uz/dream/app/data/SeedData.kt   (first-start / offline copy)
  - supabase/schema.sql                               (tables + security rules + content)

Every text exists in three languages: Russian (the original), English and Uzbek (Latin alphabet).
Write them with T(ru, en, uz). In the database the Russian text is in `title`, `body`, ...,
the translations in `title_en`, `title_uz`, ... The app shows the user's language and falls back to Russian.

Rule for every fact card: it has a source and a status.
  verified = checked against the source on the date in `verified_at`
  check    = NOT checked yet -> the app shows "нужна проверка"
  tip      = advice from Dream, not a fact
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TODAY = "2026-11-12"

def T(ru: str, en: str, uz: str) -> dict:
    """One text in the three app languages. Russian is the original; Uzbek uses the Latin alphabet (oʻ, gʻ, ʼ)."""
    return {"ru": ru, "en": en, "uz": uz}


def same(text: str) -> dict:
    """A name that is the same in every language (an official site or document title)."""
    return T(text, text, text)


SRC_STUDY = T("Study in Korea — портал Министерства образования Кореи",
              "Study in Korea — portal of the Korean Ministry of Education",
              "Study in Korea — Koreya Taʼlim vazirligi portali")
SRC_TIP = T("Совет Dream", "Dream tip", "Dream maslahati")
SRC_HIKOREA = T("Hi Korea — портал иммиграционной службы",
                "Hi Korea — immigration service portal",
                "Hi Korea — immigratsiya xizmati portali")
SRC_INTL = T("Международный отдел вуза", "Your university's international office", "Universitetning xalqaro boʻlimi")

# (id, kind, section, title, body, source, source_url, status)
CARDS = [
    # ---------------- ROADMAP: Uzbekistan -> South Korea ----------------
    ("r_goal", "roadmap", "lang",
     T("Выбери тип программы", "Choose the type of programme", "Dastur turini tanlang"),
     T("От программы зависит тип визы. Бакалавриат идёт по визе D-2-2, программа обмена — по D-2-6. "
       "Языковые курсы при университете — это отдельная виза D-4.\n\n"
       "Реши сначала, куда ты идёшь: сразу на степень или сначала на язык. От этого зависят документы и сроки.",
       "The programme decides the visa type. A bachelor's degree uses the D-2-2 visa, an exchange programme uses D-2-6. "
       "Language courses at a university are a separate D-4 visa.\n\n"
       "First decide where you are going: straight to a degree or to language study first. Your documents and deadlines depend on it.",
       "Viza turi dasturga bogʻliq. Bakalavriat uchun D-2-2 vizasi, almashinuv dasturi uchun D-2-6 vizasi beriladi. "
       "Universitet qoshidagi til kurslari — alohida D-4 vizasi.\n\n"
       "Avval qayerga borishingizni hal qiling: darhol darajaga yoki avval til oʻrganishga. Hujjatlar va muddatlar shunga bogʻliq."),
     same("Sahmyook University — Residence Qualifications for International Students"),
     "https://iie.syu.ac.kr/en/student-life/residence", "verified"),
    ("r_topik", "roadmap", "lang",
     T("Сдай TOPIK", "Take TOPIK", "TOPIK topshiring"),
     T("TOPIK — государственный экзамен по корейскому. TOPIK I даёт уровни 1–2, TOPIK II — уровни 3–6. "
       "Для программ на корейском вузы обычно просят уровень 3 и выше, но точное требование у каждого вуза своё.\n\n"
       "Даты экзамена в Ташкенте и регистрацию смотри на официальном сайте.",
       "TOPIK is the official Korean language test. TOPIK I gives levels 1–2, TOPIK II gives levels 3–6. "
       "For programmes taught in Korean, universities usually ask for level 3 or higher, but each university sets its own requirement.\n\n"
       "Check test dates in Tashkent and registration on the official website.",
       "TOPIK — koreys tili boʻyicha davlat imtihoni. TOPIK I 1–2-darajani, TOPIK II 3–6-darajani beradi. "
       "Koreys tilidagi dasturlar uchun universitetlar odatda 3-daraja va undan yuqorisini soʻraydi, lekin aniq talab har bir universitetda oʻziga xos.\n\n"
       "Toshkentdagi imtihon sanalari va roʻyxatdan oʻtishni rasmiy saytdan koʻring."),
     T("TOPIK — официальный сайт", "TOPIK — official website", "TOPIK — rasmiy sayt"),
     "https://www.topik.go.kr", "check"),
    ("r_english", "roadmap", "lang",
     T("Английский трек: IELTS или TOEFL", "English track: IELTS or TOEFL", "Ingliz tili yoʻli: IELTS yoki TOEFL"),
     T("Если программа на английском, вместо TOPIK вуз попросит IELTS или TOEFL. "
       "Минимальный балл зависит от вуза и факультета — смотри Admission Guide конкретного университета.",
       "If the programme is taught in English, the university will ask for IELTS or TOEFL instead of TOPIK. "
       "The minimum score depends on the university and the faculty: check that university's Admission Guide.",
       "Agar dastur ingliz tilida boʻlsa, universitet TOPIK oʻrniga IELTS yoki TOEFL soʻraydi. "
       "Minimal ball universitet va fakultetga bogʻliq — aniq universitetning Admission Guide hujjatini koʻring."),
     SRC_STUDY, "https://www.studyinkorea.go.kr", "check"),

    ("r_list", "roadmap", "univ",
     T("Составь список из 5–8 вузов", "Make a list of 5–8 universities", "5–8 ta universitetdan roʻyxat tuzing"),
     T("Раздели список на три группы: мечта, реальные, запасные. По каждому вузу выпиши: язык обучения, "
       "требование по языку, стоимость семестра, дедлайн и стипендии для иностранцев.\n\n"
       "В разделе «Вузы» можно показать университету свой интерес.",
       "Split the list into three groups: dream, realistic, safe. For each university write down: language of study, "
       "language requirement, cost per semester, deadline and scholarships for international students.\n\n"
       "In the Universities tab you can show a university that you are interested.",
       "Roʻyxatni uch guruhga ajrating: orzu, real, zaxira. Har bir universitet uchun yozib qoʻying: taʼlim tili, "
       "til talabi, semestr narxi, muddat va chet elliklar uchun grantlar.\n\n"
       "«OTMlar» boʻlimida universitetga qiziqishingizni bildirishingiz mumkin."),
     SRC_TIP, "", "tip"),
    ("r_gks", "roadmap", "univ",
     T("Стипендия GKS", "GKS scholarship", "GKS granti"),
     T("Global Korea Scholarship — стипендия правительства Кореи для иностранных студентов. "
       "Подать можно через посольство Кореи или напрямую через университет.\n\n"
       "Что покрывает стипендия и точные сроки набора на твой год — проверь в официальном объявлении.",
       "Global Korea Scholarship is the Korean government's scholarship for international students. "
       "You can apply through the Korean embassy or directly through a university.\n\n"
       "Check what the scholarship covers and the exact deadlines for your year in the official announcement.",
       "Global Korea Scholarship — Koreya hukumatining xorijiy talabalar uchun granti. "
       "Ariza Koreya elchixonasi orqali yoki toʻgʻridan-toʻgʻri universitet orqali topshiriladi.\n\n"
       "Grant nimalarni qoplashi va sizning yilingiz uchun aniq muddatlarni rasmiy eʼlondan tekshiring."),
     T("Study in Korea — раздел GKS", "Study in Korea — GKS section", "Study in Korea — GKS boʻlimi"),
     "https://www.studyinkorea.go.kr", "check"),
    ("r_deadlines", "roadmap", "univ",
     T("Следи за сроками подачи", "Watch the application deadlines", "Ariza muddatlarini kuzating"),
     T("В Корее два семестра: весенний и осенний. Приём документов обычно идёт за несколько месяцев до начала семестра. "
       "Точные даты у каждого вуза свои.",
       "Korea has two semesters: spring and autumn. Applications usually close several months before the semester starts. "
       "Each university has its own exact dates.",
       "Koreyada ikki semestr bor: bahorgi va kuzgi. Hujjatlar odatda semestr boshlanishidan bir necha oy oldin qabul qilinadi. "
       "Aniq sanalar har bir universitetda oʻziga xos."),
     T("Сайт приёмной комиссии выбранного вуза", "Admissions website of your chosen university", "Tanlangan universitetning qabul komissiyasi sayti"),
     "https://www.studyinkorea.go.kr", "check"),

    ("r_docs", "roadmap", "docs",
     T("Аттестат или диплом с апостилем", "School certificate or diploma with apostille", "Apostilli attestat yoki diplom"),
     T("Вуз попросит документ об образовании и выписку оценок с переводом и легализацией. "
       "Как именно заверять документы из Узбекистана (апостиль или консульская легализация) — уточни в выбранном вузе.",
       "The university will ask for your education document and transcript, translated and legalised. "
       "Ask your chosen university exactly how documents from Uzbekistan must be certified (apostille or consular legalisation).",
       "Universitet taʼlim haqidagi hujjat va baholar koʻchirmasini tarjima va legalizatsiya bilan soʻraydi. "
       "Oʻzbekiston hujjatlari qanday tasdiqlanishi kerakligini (apostil yoki konsullik legalizatsiyasi) tanlangan universitetdan aniqlang."),
     T("HCCH — статус Конвенции об апостиле", "HCCH — Apostille Convention status", "HCCH — Apostil konvensiyasi holati"),
     "https://www.hcch.net", "check"),
    ("r_bank", "roadmap", "docs",
     T("Финансовая гарантия", "Proof of funds", "Moliyaviy kafolat"),
     T("Для визы нужна справка об остатке на банковском счёте. Суммы в разных источниках отличаются: "
       "встречаются 20 млн вон (16 млн для вузов вне столичного региона) и 20 000 долларов.\n\n"
       "Не полагайся на чужие цифры: спроси точную сумму у своего вуза и в посольстве Кореи в Ташкенте.",
       "The visa needs a bank balance certificate. Different sources give different amounts: "
       "you will see 20 million won (16 million for universities outside the capital region) and 20,000 dollars.\n\n"
       "Don't rely on other people's numbers: ask your university and the Korean embassy in Tashkent for the exact amount.",
       "Viza uchun bank hisobidagi qoldiq haqida maʼlumotnoma kerak. Turli manbalarda summa har xil: "
       "20 mln von (poytaxt hududidan tashqaridagi universitetlar uchun 16 mln) va 20 000 dollar uchraydi.\n\n"
       "Boshqalarning raqamlariga ishonmang: aniq summani universitetingiz va Toshkentdagi Koreya elchixonasidan soʻrang."),
     T("Международный отдел вуза и посольство Кореи", "University international office and the Korean embassy", "Universitetning xalqaro boʻlimi va Koreya elchixonasi"),
     "https://iie.syu.ac.kr/en/student-life/residence", "check"),
    ("r_letter", "roadmap", "docs",
     T("Мотивационное письмо и учебный план", "Motivation letter and study plan", "Motivatsion xat va oʻquv rejasi"),
     T("Пиши конкретно: почему эта специальность, почему этот вуз, что ты будешь делать после выпуска. "
       "Дай прочитать письмо человеку, который уже поступил — спроси в комнате этого раздела.",
       "Be specific: why this major, why this university, what you will do after graduating. "
       "Ask someone who has already been admitted to read your letter: ask in this section's room.",
       "Aniq yozing: nega aynan shu mutaxassislik, nega shu universitet, bitirgandan keyin nima qilasiz. "
       "Xatni allaqachon oʻqishga kirgan odamga oʻqitib koʻring — shu boʻlim xonasida soʻrang."),
     SRC_TIP, "", "tip"),

    ("r_admission", "roadmap", "visa",
     T("Получи Certificate of Admission", "Get the Certificate of Admission", "Certificate of Admission oling"),
     T("После зачисления вуз выдаёт стандартное письмо о зачислении. На его основе подаётся виза. "
       "Подкатегорию визы определяет вуз в этом документе.",
       "After admission the university issues a standard admission letter. You apply for the visa based on it. "
       "The university sets the visa subcategory in this document.",
       "Qabul qilingandan soʻng universitet standart qabul xatini beradi. Viza shu asosda topshiriladi. "
       "Viza kichik toifasini universitet shu hujjatda belgilaydi."),
     same("Korea Visa Portal"), "https://www.visa.go.kr", "check"),
    ("r_visa", "roadmap", "visa",
     T("Подай на визу D-2", "Apply for the D-2 visa", "D-2 vizasiga ariza topshiring"),
     T("Заявление подаётся через Korea Visa Portal или в консульском отделе посольства Кореи. "
       "Список документов и срок рассмотрения уточняй в посольстве в Ташкенте.",
       "You apply through the Korea Visa Portal or at the consular section of the Korean embassy. "
       "Check the list of documents and processing time with the embassy in Tashkent.",
       "Ariza Korea Visa Portal orqali yoki Koreya elchixonasining konsullik boʻlimida topshiriladi. "
       "Hujjatlar roʻyxati va koʻrib chiqish muddatini Toshkentdagi elchixonadan aniqlang."),
     same("Korea Visa Portal"), "https://www.visa.go.kr", "check"),

    ("r_insurance", "roadmap", "arrival",
     T("Купи страховку на первые месяцы", "Buy insurance for the first months", "Dastlabki oylar uchun sugʻurta oling"),
     T("Государственная страховка NHIS начинает действовать только после регистрации иностранца в Корее. "
       "Korea University советует купить частную страховку как минимум на первые 90 дней после приезда.",
       "The national NHIS insurance starts only after you register as a foreigner in Korea. "
       "Korea University advises buying private insurance for at least the first 90 days after arrival.",
       "Davlat NHIS sugʻurtasi faqat Koreyada chet ellik sifatida roʻyxatdan oʻtgandan keyin amal qila boshlaydi. "
       "Korea University kelgandan keyingi kamida dastlabki 90 kun uchun xususiy sugʻurta olishni maslahat beradi."),
     same("Korea University — Student Guidebook, Insurance"),
     "https://sites.google.com/view/ku-student-guidebook/insurance", "verified"),
    ("r_housing", "roadmap", "arrival",
     T("Реши вопрос с жильём", "Sort out housing", "Turar joy masalasini hal qiling"),
     T("Подай заявку на общежитие сразу после зачисления: мест обычно меньше, чем желающих. "
       "Если снимаешь жильё, сохрани договор — он понадобится для регистрации.",
       "Apply for the dormitory right after admission: there are usually fewer places than applicants. "
       "If you rent a flat, keep the contract: you will need it to register.",
       "Qabul qilingandan soʻng darhol yotoqxonaga ariza bering: joylar odatda istovchilardan kam. "
       "Agar uy ijaraga olsangiz, shartnomani saqlang — roʻyxatdan oʻtish uchun kerak boʻladi."),
     SRC_TIP, "", "tip"),

    # ---------------- GUIDE: after arrival ----------------
    ("g_arc", "guide", "first90",
     T("Residence Card — в течение 90 дней", "Residence Card within 90 days", "Residence Card — 90 kun ichida"),
     T("Иностранцы, которые остаются в Корее дольше 90 дней, обязаны зарегистрироваться и подать на Residence Card "
       "(раньше называлась ARC) в течение 90 дней после въезда. За опоздание возможен штраф.\n\n"
       "Не выезжай из Кореи, пока карта не на руках: Ewha предупреждает, что визу в этом случае аннулируют. "
       "По данным BUFS, пошлина за карту — 30 000 вон.",
       "Foreigners staying in Korea longer than 90 days must register and apply for a Residence Card "
       "(formerly called ARC) within 90 days of entry. Being late can mean a fine.\n\n"
       "Don't leave Korea before you have the card: Ewha warns that the visa is cancelled in that case. "
       "According to BUFS, the card fee is 30,000 won.",
       "Koreyada 90 kundan uzoq qoladigan chet elliklar kirgandan keyin 90 kun ichida roʻyxatdan oʻtishi va Residence Card "
       "(avval ARC deb atalgan) uchun ariza berishi shart. Kechiksangiz, jarima solinishi mumkin.\n\n"
       "Karta qoʻlingizga tegmaguncha Koreyadan chiqmang: Ewha bu holda viza bekor qilinishini ogohlantiradi. "
       "BUFS maʼlumotiga koʻra, karta boji — 30 000 von."),
     same("Ewha Womans University — Foreign Resident Registration"),
     "https://isa.ewha.ac.kr/oisa/2181/subview.do", "verified"),
    ("g_address", "guide", "first90",
     T("Сообщай о смене адреса", "Report a change of address", "Manzil oʻzgarishini xabar qiling"),
     T("Если ты переехал, о новом адресе нужно сообщить в иммиграционную службу или районную администрацию. "
       "В неофициальных источниках указан срок 14 дней — проверь его на Hi Korea.",
       "If you move, you must report the new address to the immigration office or the district office. "
       "Unofficial sources say 14 days: check this on Hi Korea.",
       "Agar koʻchib oʻtsangiz, yangi manzilni immigratsiya xizmatiga yoki tuman maʼmuriyatiga xabar qilish kerak. "
       "Norasmiy manbalarda 14 kun muddat koʻrsatilgan — buni Hi Koreaʼda tekshiring."),
     SRC_HIKOREA, "https://www.hikorea.go.kr", "check"),

    ("g_nhis", "guide", "health",
     T("Госстраховка NHIS обязательна", "NHIS national insurance is mandatory", "NHIS davlat sugʻurtasi majburiy"),
     T("С 1 марта 2021 года иностранные студенты обязательно подключаются к государственной медстраховке NHIS. "
       "Подключение происходит автоматически с даты регистрации иностранца, самому ничего подавать не нужно. "
       "Карту NHIS и инструкцию присылают на твой адрес в Корее.\n\n"
       "Размер ежемесячного взноса меняется — смотри на сайте NHIS.",
       "Since 1 March 2021 international students are enrolled in the national NHIS health insurance. "
       "Enrolment happens automatically from the date of your foreigner registration; you don't need to apply. "
       "The NHIS card and instructions are mailed to your address in Korea.\n\n"
       "The monthly premium changes: check the NHIS website.",
       "2021-yil 1-martdan boshlab xorijiy talabalar NHIS davlat tibbiy sugʻurtasiga majburiy ulanadi. "
       "Ulanish chet ellik roʻyxatdan oʻtgan sanadan avtomatik boʻladi, oʻzingiz hech narsa topshirishingiz shart emas. "
       "NHIS kartasi va yoʻriqnoma Koreyadagi manzilingizga yuboriladi.\n\n"
       "Oylik badal miqdori oʻzgaradi — NHIS saytidan koʻring."),
     same("Seoul National University — National Health Insurance"),
     "https://oga.snu.ac.kr/isss/immigration/national-health-insurance/", "verified"),

    ("g_work", "guide", "work",
     T("Подработка только с разрешением", "Part-time work only with a permit", "Qoʻshimcha ish faqat ruxsat bilan"),
     T("С визой D-2 нельзя просто так работать. Сначала нужно получить разрешение иммиграционной службы, "
       "и только потом выходить на работу. Перед подачей вуз подтверждает заявку.\n\n"
       "Работа без разрешения — это штраф и проблемы с визой.",
       "On a D-2 visa you can't just start working. First get a permit from the immigration office, "
       "and only then start the job. The university approves the application before you submit it.\n\n"
       "Working without a permit means a fine and problems with your visa.",
       "D-2 vizasi bilan shunchaki ishlab boʻlmaydi. Avval immigratsiya xizmatidan ruxsat olish kerak, "
       "shundan keyingina ishga chiqish mumkin. Topshirishdan oldin universitet arizani tasdiqlaydi.\n\n"
       "Ruxsatsiz ishlash — jarima va viza bilan muammolar demakdir."),
     same("Korea University — Part-time Work"),
     "https://univ.korea.ac.kr/gsc/DegreeUndergraduate/Visa_Immigration/Part-timeWork/Part-timeWork.do", "verified"),
    ("g_work_hours", "guide", "work",
     T("Сколько часов можно работать", "How many hours you can work", "Necha soat ishlash mumkin"),
     T("Лимит часов зависит от уровня TOPIK, курса и типа программы. Правила обновлялись, и в разных источниках "
       "цифры расходятся (от 10 до 30 часов в будни).\n\n"
       "Актуальную таблицу смотри на Study in Korea и уточняй в международном отделе своего вуза.",
       "The hour limit depends on your TOPIK level, year of study and programme type. The rules have changed, and sources "
       "give different numbers (from 10 to 30 hours on weekdays).\n\n"
       "Check the current table on Study in Korea and confirm with your university's international office.",
       "Soatlar chegarasi TOPIK darajasi, kurs va dastur turiga bogʻliq. Qoidalar yangilangan va turli manbalarda "
       "raqamlar farq qiladi (ish kunlarida 10 dan 30 soatgacha).\n\n"
       "Amaldagi jadvalni Study in Koreaʼda koʻring va universitetingizning xalqaro boʻlimida aniqlang."),
     T("Study in Korea — Part-time work", "Study in Korea — Part-time work", "Study in Korea — Part-time work"),
     "https://www.studyinkorea.go.kr", "check"),

    ("g_bank", "guide", "life",
     T("Банковский счёт", "Bank account", "Bank hisobi"),
     T("Счёт обычно открывают после получения Residence Card. Возьми паспорт, карту и справку об обучении. "
       "Условия зависят от банка.",
       "You usually open an account after you get your Residence Card. Bring your passport, the card and a certificate of enrolment. "
       "Conditions depend on the bank.",
       "Hisob odatda Residence Card olingandan keyin ochiladi. Pasport, karta va oʻqish haqidagi maʼlumotnomani oling. "
       "Shartlar bankka bogʻliq."),
     SRC_INTL, "", "check"),
    ("g_sim", "guide", "life",
     T("SIM-карта и связь", "SIM card and phone", "SIM-karta va aloqa"),
     T("До получения Residence Card доступны в основном предоплаченные SIM. Контракт на своё имя обычно оформляют уже с картой.",
       "Before you get the Residence Card, mostly prepaid SIM cards are available. A contract in your own name usually needs the card.",
       "Residence Card olgunga qadar asosan oldindan toʻlanadigan SIM-kartalar mavjud. Oʻz nomingizga shartnoma odatda karta bilan rasmiylashtiriladi."),
     SRC_INTL, "", "check"),
    ("g_transport", "guide", "life",
     T("Транспорт", "Transport", "Transport"),
     T("Автобусы и метро оплачиваются транспортной картой (например, T-money), её пополняют в магазинах у дома и на станциях.",
       "Buses and the metro are paid with a transport card (for example T-money); you top it up at convenience stores and stations.",
       "Avtobus va metro transport kartasi (masalan, T-money) bilan toʻlanadi, uni doʻkonlarda va bekatlarda toʻldirish mumkin."),
     same("Visit Korea"), "https://english.visitkorea.or.kr", "check"),
    ("g_emergency", "guide", "life",
     T("Экстренные номера", "Emergency numbers", "Favqulodda raqamlar"),
     T("112 — полиция, 119 — пожарные и скорая, 1345 — контакт-центр иммиграционной службы для иностранцев.",
       "112 — police, 119 — fire and ambulance, 1345 — immigration contact centre for foreigners.",
       "112 — politsiya, 119 — oʻt oʻchiruvchilar va tez yordam, 1345 — chet elliklar uchun immigratsiya xizmati aloqa markazi."),
     SRC_HIKOREA, "https://www.hikorea.go.kr", "check"),
]

# (id, title, teacher, level, price_uzs, glyph, description)
COURSES = [
    ("c_hangul", T("Хангыль за 7 дней", "Hangul in 7 days", "7 kunda hangul"), "Dilnoza K.",
     T("С нуля", "Beginner", "Noldan"), 149000, "한",
     T("Научишься читать и писать корейский алфавит. Без этого курса дальше никуда.",
       "Learn to read and write the Korean alphabet. Everything else starts here.",
       "Koreys alifbosida oʻqish va yozishni oʻrganasiz. Bu kurssiz oldinga yoʻl yoʻq.")),
    ("c_topik1", T("TOPIK I: уверенный старт", "TOPIK I: a confident start", "TOPIK I: ishonchli start"), "Kim So-yeon",
     same("A1–A2"), 349000, "토",
     T("Грамматика и лексика для первого и второго уровня TOPIK.",
       "Grammar and vocabulary for TOPIK levels 1 and 2.",
       "TOPIK 1- va 2-darajalari uchun grammatika va soʻz boyligi.")),
    ("c_dorm", T("Корейский для общежития", "Korean for dorm life", "Yotoqxona uchun koreys tili"), "Jasur T.",
     same("A2"), 249000, "말",
     T("Фразы на каждый день: столовая, офис, аптека, соседи.",
       "Everyday phrases: canteen, office, pharmacy, roommates.",
       "Har kungi iboralar: oshxona, ofis, dorixona, qoʻshnilar.")),
    ("c_interview", T("Интервью в вуз на корейском", "University interview in Korean", "Koreys tilida universitet suhbati"), "Park Min-ji",
     same("B1"), 399000, "꿈",
     T("Как рассказать о себе и своей цели на собеседовании.",
       "How to talk about yourself and your goal in an interview.",
       "Suhbatda oʻzingiz va maqsadingiz haqida qanday gapirish kerak.")),
]

# (id, course_id, title, minutes, free, content)
# Lines written as "Korean — meaning" are shown as flash cards in the app.
LESSONS = [
    ("l_hangul_1", "c_hangul", T("Шесть базовых гласных", "Six basic vowels", "Oltita asosiy unli"), 12, True,
     T("ㅏ — а\nㅓ — о (открытое)\nㅗ — о\nㅜ — у\nㅡ — ы\nㅣ — и\n\n"
       "Задание: напиши каждую букву по 10 раз и произнеси вслух.",
       "ㅏ — a (as in “father”)\nㅓ — eo (open o, as in “up”)\nㅗ — o\nㅜ — u\nㅡ — eu (lips flat)\nㅣ — i\n\n"
       "Task: write each letter 10 times and say it out loud.",
       "ㅏ — a\nㅓ — o (ochiq)\nㅗ — o\nㅜ — u\nㅡ — i (lablar choʻzilmaydi)\nㅣ — i\n\n"
       "Topshiriq: har bir harfni 10 martadan yozing va ovoz chiqarib ayting.")),
    ("l_hangul_2", "c_hangul", T("Первые согласные", "First consonants", "Birinchi undoshlar"), 15, False,
     T("ㄱ — к/г\nㄴ — н\nㄷ — т/д\nㄹ — р/ль\nㅁ — м\nㅂ — п/б\nㅅ — с\nㅇ — не читается в начале слога, «нг» в конце\nㅈ — ч/дж",
       "ㄱ — k/g\nㄴ — n\nㄷ — t/d\nㄹ — r/l\nㅁ — m\nㅂ — p/b\nㅅ — s\nㅇ — silent at the start of a syllable, “ng” at the end\nㅈ — ch/j",
       "ㄱ — k/g\nㄴ — n\nㄷ — t/d\nㄹ — r/l\nㅁ — m\nㅂ — p/b\nㅅ — s\nㅇ — boʻgʻin boshida oʻqilmaydi, oxirida «ng»\nㅈ — ch/j")),
    ("l_hangul_3", "c_hangul", T("Как собирается слог", "How a syllable is built", "Boʻgʻin qanday tuziladi"), 14, False,
     T("Слог — это блок: согласная + гласная (+ согласная снизу).\n\n"
       "ㄱ + ㅏ = 가 (ка)\nㄴ + ㅏ = 나 (на)\nㅎ + ㅏ + ㄴ = 한 (хан)\nㄱ + ㅡ + ㄹ = 글 (кыль)\n\n한글 — хангыль.",
       "A syllable is a block: consonant + vowel (+ a consonant below).\n\n"
       "ㄱ + ㅏ = 가 (ga)\nㄴ + ㅏ = 나 (na)\nㅎ + ㅏ + ㄴ = 한 (han)\nㄱ + ㅡ + ㄹ = 글 (geul)\n\n한글 — hangul.",
       "Boʻgʻin — bu blok: undosh + unli (+ pastda undosh).\n\n"
       "ㄱ + ㅏ = 가 (ka)\nㄴ + ㅏ = 나 (na)\nㅎ + ㅏ + ㄴ = 한 (han)\nㄱ + ㅡ + ㄹ = 글 (kil)\n\n한글 — hangul.")),
    ("l_hangul_4", "c_hangul", T("Читаем первые слова", "Reading your first words", "Birinchi soʻzlarni oʻqiymiz"), 16, False,
     T("안녕하세요 — здравствуйте\n감사합니다 — спасибо\n네 — да\n아니요 — нет\n학생 — студент\n대학교 — университет",
       "안녕하세요 — hello\n감사합니다 — thank you\n네 — yes\n아니요 — no\n학생 — student\n대학교 — university",
       "안녕하세요 — assalomu alaykum\n감사합니다 — rahmat\n네 — ha\n아니요 — yoʻq\n학생 — talaba\n대학교 — universitet")),

    ("l_topik1_1", "c_topik1", T("Из чего состоит TOPIK I", "What TOPIK I is made of", "TOPIK I nimalardan iborat"), 10, True,
     T("TOPIK I проверяет аудирование и чтение. Письменной части нет — она появляется в TOPIK II.\n\n"
       "План курса: частицы, числа, вежливый стиль, типовые задания.",
       "TOPIK I tests listening and reading. There is no writing part: it appears in TOPIK II.\n\n"
       "Course plan: particles, numbers, the polite style, typical tasks.",
       "TOPIK I tinglab tushunish va oʻqishni tekshiradi. Yozma qismi yoʻq — u TOPIK IIʼda paydo boʻladi.\n\n"
       "Kurs rejasi: yuklamalar, sonlar, xushmuomala uslub, odatiy topshiriqlar.")),
    ("l_topik1_2", "c_topik1", T("Частицы 은/는 и 이/가", "Particles 은/는 and 이/가", "은/는 va 이/가 yuklamalari"), 18, False,
     T("은/는 — тема предложения: 저는 학생이에요 (Я студент).\n"
       "이/가 — подлежащее: 비가 와요 (Идёт дождь).\n\n"
       "После согласной — 은 и 이, после гласной — 는 и 가.",
       "은/는 — topic of the sentence: 저는 학생이에요 (I am a student).\n"
       "이/가 — subject: 비가 와요 (It is raining).\n\n"
       "After a consonant use 은 and 이, after a vowel use 는 and 가.",
       "은/는 — gap mavzusi: 저는 학생이에요 (Men talabaman).\n"
       "이/가 — ega: 비가 와요 (Yomgʻir yogʻyapti).\n\n"
       "Undoshdan keyin 은 va 이, unlidan keyin 는 va 가.")),
    ("l_topik1_3", "c_topik1", T("Два ряда чисел", "Two sets of numbers", "Ikki xil sonlar"), 17, False,
     T("Корейские: 하나, 둘, 셋, 넷, 다섯 — для счёта предметов и возраста.\n"
       "Сино-корейские: 일, 이, 삼, 사, 오 — для дат, денег, номеров.",
       "Native Korean: 하나, 둘, 셋, 넷, 다섯 — for counting things and age.\n"
       "Sino-Korean: 일, 이, 삼, 사, 오 — for dates, money, numbers.",
       "Koreyscha: 하나, 둘, 셋, 넷, 다섯 — narsalarni va yoshni sanash uchun.\n"
       "Xitoy-koreyscha: 일, 이, 삼, 사, 오 — sanalar, pul, raqamlar uchun.")),
    ("l_topik1_4", "c_topik1", T("Вежливый стиль -아요/어요", "Polite style -아요/어요", "-아요/어요 xushmuomala uslubi"), 20, False,
     T("가다 → 가요 (иду)\n먹다 → 먹어요 (ем)\n공부하다 → 공부해요 (учусь)\n\n"
       "Этот стиль подходит почти для любой повседневной ситуации.",
       "가다 → 가요 (I go)\n먹다 → 먹어요 (I eat)\n공부하다 → 공부해요 (I study)\n\n"
       "This style fits almost any everyday situation.",
       "가다 → 가요 (boraman)\n먹다 → 먹어요 (yeyman)\n공부하다 → 공부해요 (oʻqiyman)\n\n"
       "Bu uslub deyarli har qanday kundalik vaziyatga mos keladi.")),

    ("l_dorm_1", "c_dorm", T("Знакомство с соседом", "Meeting your roommate", "Qoʻshni bilan tanishuv"), 11, True,
     T("안녕하세요, 저는 ...입니다 — Здравствуйте, я ...\n"
       "우즈베키스탄에서 왔어요 — Я приехал из Узбекистана\n"
       "잘 부탁드립니다 — Рассчитываю на хорошее отношение",
       "안녕하세요, 저는 ...입니다 — Hello, I am ...\n"
       "우즈베키스탄에서 왔어요 — I came from Uzbekistan\n"
       "잘 부탁드립니다 — Nice to meet you, I look forward to getting along",
       "안녕하세요, 저는 ...입니다 — Assalomu alaykum, men ...\n"
       "우즈베키스탄에서 왔어요 — Men Oʻzbekistondan keldim\n"
       "잘 부탁드립니다 — Tanishganimdan xursandman, yaxshi munosabatda boʻlaylik")),
    ("l_dorm_2", "c_dorm", T("В столовой и магазине", "At the canteen and shop", "Oshxona va doʻkonda"), 13, False,
     T("이거 주세요 — Дайте это, пожалуйста\n얼마예요? — Сколько стоит?\n"
       "카드 돼요? — Картой можно?\n돼지고기 들어 있어요? — Здесь есть свинина?",
       "이거 주세요 — This one, please\n얼마예요? — How much is it?\n"
       "카드 돼요? — Can I pay by card?\n돼지고기 들어 있어요? — Is there pork in it?",
       "이거 주세요 — Mana buni bering, iltimos\n얼마예요? — Qancha turadi?\n"
       "카드 돼요? — Karta bilan boʻladimi?\n돼지고기 들어 있어요? — Ichida choʻchqa goʻshti bormi?")),
    ("l_dorm_3", "c_dorm", T("В офисе и администрации", "At the office", "Ofis va maʼmuriyatda"), 15, False,
     T("도와주세요 — Помогите, пожалуйста\n외국인등록증 — карта регистрации иностранца\n"
       "재학증명서 — справка об обучении\n어디에서 신청해요? — Где подать заявление?",
       "도와주세요 — Please help me\n외국인등록증 — foreigner registration card\n"
       "재학증명서 — certificate of enrolment\n어디에서 신청해요? — Where do I apply?",
       "도와주세요 — Yordam bering, iltimos\n외국인등록증 — chet ellik roʻyxat kartasi\n"
       "재학증명서 — oʻqish haqida maʼlumotnoma\n어디에서 신청해요? — Arizani qayerga topshiraman?")),
    ("l_dorm_4", "c_dorm", T("В аптеке", "At the pharmacy", "Dorixonada"), 12, False,
     T("약국 — аптека\n머리가 아파요 — У меня болит голова\n감기에 걸렸어요 — Я простудился\n약 주세요 — Дайте лекарство",
       "약국 — pharmacy\n머리가 아파요 — I have a headache\n감기에 걸렸어요 — I caught a cold\n약 주세요 — Medicine, please",
       "약국 — dorixona\n머리가 아파요 — Boshim ogʻriyapti\n감기에 걸렸어요 — Shamolladim\n약 주세요 — Dori bering")),

    ("l_int_1", "c_interview", T("Самопрезентация", "Introducing yourself", "Oʻzini tanishtirish"), 14, True,
     T("자기소개 — рассказ о себе. Структура: кто ты → что изучал → почему Корея → цель.\n\n"
       "저는 우즈베키스탄에서 온 ...입니다 — Я ..., приехал из Узбекистана.",
       "자기소개 — introducing yourself. Structure: who you are → what you studied → why Korea → your goal.\n\n"
       "저는 우즈베키스탄에서 온 ...입니다 — I am ..., from Uzbekistan.",
       "자기소개 — oʻzingiz haqingizda hikoya. Tuzilishi: kimsiz → nimani oʻrgangansiz → nega Koreya → maqsad.\n\n"
       "저는 우즈베키스탄에서 온 ...입니다 — Men ..., Oʻzbekistondan keldim.")),
    ("l_int_2", "c_interview", T("Мотивация", "Motivation", "Motivatsiya"), 16, False,
     T("지원 동기 — причина подачи.\n한국에서 공부하고 싶습니다 — Я хочу учиться в Корее.\n"
       "이 전공에 관심이 많습니다 — Мне очень интересна эта специальность.",
       "지원 동기 — reason for applying.\n한국에서 공부하고 싶습니다 — I want to study in Korea.\n"
       "이 전공에 관심이 많습니다 — I am very interested in this major.",
       "지원 동기 — ariza berish sababi.\n한국에서 공부하고 싶습니다 — Men Koreyada oʻqishni xohlayman.\n"
       "이 전공에 관심이 많습니다 — Bu mutaxassislik menga juda qiziq.")),
    ("l_int_3", "c_interview", T("Планы после выпуска", "Plans after graduation", "Bitirgandan keyingi rejalar"), 15, False,
     T("졸업 후 계획 — планы после выпуска.\n졸업 후에 고향에 돌아가서 일하고 싶습니다 — "
       "После выпуска я хочу вернуться на родину и работать.",
       "졸업 후 계획 — plans after graduation.\n졸업 후에 고향에 돌아가서 일하고 싶습니다 — "
       "After graduating I want to return home and work.",
       "졸업 후 계획 — bitirgandan keyingi rejalar.\n졸업 후에 고향에 돌아가서 일하고 싶습니다 — "
       "Bitirgandan keyin vatanimga qaytib ishlamoqchiman.")),
    ("l_int_4", "c_interview", T("Официальный стиль -습니다", "Formal style -습니다", "-습니다 rasmiy uslubi"), 18, False,
     T("На интервью используют официальный стиль.\n가다 → 갑니다\n먹다 → 먹습니다\n공부하다 → 공부합니다",
       "Interviews use the formal style.\n가다 → 갑니다\n먹다 → 먹습니다\n공부하다 → 공부합니다",
       "Suhbatda rasmiy uslub ishlatiladi.\n가다 → 갑니다\n먹다 → 먹습니다\n공부하다 → 공부합니다")),
]

SEOUL = T("Сеул", "Seoul", "Seul")
PUBLIC = T("Государственный", "Public", "Davlat")
PRIVATE = T("Частный", "Private", "Xususiy")

# (id, name, city, type, url)
UNIVERSITIES = [
    ("u_snu", "Seoul National University", SEOUL, PUBLIC, "https://www.snu.ac.kr"),
    ("u_kaist", "KAIST", T("Тэджон", "Daejeon", "Tejon"), PUBLIC, "https://www.kaist.ac.kr"),
    ("u_yonsei", "Yonsei University", SEOUL, PRIVATE, "https://www.yonsei.ac.kr"),
    ("u_korea", "Korea University", SEOUL, PRIVATE, "https://www.korea.ac.kr"),
    ("u_ajou", "Ajou University", T("Сувон", "Suwon", "Suvon"), PRIVATE, "https://www.ajou.ac.kr"),
    ("u_inha", "Inha University", T("Инчхон", "Incheon", "Inchxon"), PRIVATE, "https://www.inha.ac.kr"),
    ("u_pnu", "Pusan National University", T("Пусан", "Busan", "Pusan"), PUBLIC, "https://www.pusan.ac.kr"),
    ("u_skku", "Sungkyunkwan University", T("Сеул / Сувон", "Seoul / Suwon", "Seul / Suvon"), PRIVATE, "https://www.skku.edu"),
]


# ---------------------------------------------------------------- Kotlin
def kt(s: str) -> str:
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$").replace("\n", "\\n") + '"'


def build_kotlin() -> str:
    out = [
        "package uz.dream.app.data",
        "",
        "// GENERATED by tools/build_content.py - do not edit by hand, edit the lists in that script.",
        "// Built-in copy of the content in Russian, English and Uzbek: shown on first start and when Supabase is not reachable.",
        "object SeedData {",
        "    val cards: List<InfoCard> = listOf(",
    ]
    for i, (cid, kind, section, title, body, source, url, status) in enumerate(CARDS):
        verified = TODAY if status == "verified" else ""
        out.append(f"        InfoCard(\n"
                   f"            {kt(cid)}, {kt(kind)}, {kt(section)}, {i}, {kt(title['ru'])},\n"
                   f"            {kt(body['ru'])},\n"
                   f"            {kt(source['ru'])}, {kt(url)}, {kt(status)}, {kt(verified)},\n"
                   f"            titleEn = {kt(title['en'])}, titleUz = {kt(title['uz'])},\n"
                   f"            bodyEn = {kt(body['en'])},\n"
                   f"            bodyUz = {kt(body['uz'])},\n"
                   f"            sourceEn = {kt(source['en'])}, sourceUz = {kt(source['uz'])}\n"
                   f"        ),")
    out.append("    )")
    out.append("")
    out.append("    val courses: List<Course> = listOf(")
    for i, (cid, title, teacher, level, price, glyph, desc) in enumerate(COURSES):
        out.append(f"        Course(\n"
                   f"            {kt(cid)}, {i}, {kt(title['ru'])}, {kt(teacher)}, {kt(level['ru'])}, {price}L, {kt(glyph)},\n"
                   f"            {kt(desc['ru'])},\n"
                   f"            titleEn = {kt(title['en'])}, titleUz = {kt(title['uz'])},\n"
                   f"            levelEn = {kt(level['en'])}, levelUz = {kt(level['uz'])},\n"
                   f"            descriptionEn = {kt(desc['en'])},\n"
                   f"            descriptionUz = {kt(desc['uz'])}\n"
                   f"        ),")
    out.append("    )")
    out.append("")
    out.append("    val lessons: List<Lesson> = listOf(")
    for i, (lid, course, title, minutes, free, content) in enumerate(LESSONS):
        out.append(f"        Lesson(\n"
                   f"            {kt(lid)}, {kt(course)}, {i}, {kt(title['ru'])}, {minutes}, {'true' if free else 'false'},\n"
                   f"            {kt(content['ru'])},\n"
                   f"            titleEn = {kt(title['en'])}, titleUz = {kt(title['uz'])},\n"
                   f"            contentEn = {kt(content['en'])},\n"
                   f"            contentUz = {kt(content['uz'])}\n"
                   f"        ),")
    out.append("    )")
    out.append("")
    out.append("    val universities: List<University> = listOf(")
    for i, (uid, name, city, typ, url) in enumerate(UNIVERSITIES):
        out.append(f"        University(\n"
                   f"            {kt(uid)}, {i}, {kt(name)}, {kt(city['ru'])}, {kt(typ['ru'])}, {kt(url)},\n"
                   f"            cityEn = {kt(city['en'])}, cityUz = {kt(city['uz'])}, typeEn = {kt(typ['en'])}, typeUz = {kt(typ['uz'])}\n"
                   f"        ),")
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
  verified_at text not null default '',
  title_en    text not null default '',
  title_uz    text not null default '',
  body_en     text not null default '',
  body_uz     text not null default '',
  source_en   text not null default '',
  source_uz   text not null default ''
);

create table if not exists public.courses (
  id          text primary key,
  sort        int    not null default 0,
  title       text   not null,
  teacher     text   not null,
  level       text   not null,
  price_uzs   bigint not null check (price_uzs >= 0),
  glyph       text   not null,
  description text   not null,
  title_en       text not null default '',
  title_uz       text not null default '',
  level_en       text not null default '',
  level_uz       text not null default '',
  description_en text not null default '',
  description_uz text not null default ''
);

create table if not exists public.lessons (
  id        text primary key,
  course_id text not null references public.courses (id) on delete cascade,
  sort      int  not null default 0,
  title     text not null,
  minutes   int  not null default 0,
  free      boolean not null default false,
  content   text not null default '',
  title_en   text not null default '',
  title_uz   text not null default '',
  content_en text not null default '',
  content_uz text not null default ''
);

create table if not exists public.universities (
  id   text primary key,
  sort int  not null default 0,
  name text not null,
  city text not null,
  type text not null,
  url  text not null default '',
  city_en text not null default '',
  city_uz text not null default '',
  type_en text not null default '',
  type_uz text not null default ''
);

-- ---------- translations (Uzbek + English) for databases created before they existed ----------
alter table public.cards        add column if not exists title_en text not null default '';
alter table public.cards        add column if not exists title_uz text not null default '';
alter table public.cards        add column if not exists body_en text not null default '';
alter table public.cards        add column if not exists body_uz text not null default '';
alter table public.cards        add column if not exists source_en text not null default '';
alter table public.cards        add column if not exists source_uz text not null default '';
alter table public.courses      add column if not exists title_en text not null default '';
alter table public.courses      add column if not exists title_uz text not null default '';
alter table public.courses      add column if not exists level_en text not null default '';
alter table public.courses      add column if not exists level_uz text not null default '';
alter table public.courses      add column if not exists description_en text not null default '';
alter table public.courses      add column if not exists description_uz text not null default '';
alter table public.lessons      add column if not exists title_en text not null default '';
alter table public.lessons      add column if not exists title_uz text not null default '';
alter table public.lessons      add column if not exists content_en text not null default '';
alter table public.lessons      add column if not exists content_uz text not null default '';
alter table public.universities add column if not exists city_en text not null default '';
alter table public.universities add column if not exists city_uz text not null default '';
alter table public.universities add column if not exists type_en text not null default '';
alter table public.universities add column if not exists type_uz text not null default '';

-- ---------- tables: users and what they do ----------
create table if not exists public.profiles (
  id         uuid primary key references auth.users (id) on delete cascade,
  full_name  text not null default '',
  goal       text not null default '',   -- 'bachelor' | 'master' | 'language' | 'exchange' 
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


def upsert(table: str, columns: list, rows: list) -> str:
    """insert ... on conflict (id) do update: running the script again refreshes the content."""
    head = f"insert into public.{table} ({', '.join(columns)}) values"
    body = ",\n".join("  (" + ", ".join(rows_i) + ")" for rows_i in rows)
    update = ", ".join(f"{c} = excluded.{c}" for c in columns if c != "id")
    return f"{head}\n{body}\non conflict (id) do update set {update};\n"


def b(value: bool) -> str:
    return "true" if value else "false"


def build_sql() -> str:
    out = [SCHEMA]

    rows = []
    for i, (cid, kind, section, title, body, source, url, status) in enumerate(CARDS):
        verified = TODAY if status == "verified" else ""
        rows.append([sq(cid), sq(kind), sq(section), str(i), sq(title["ru"]), sq(body["ru"]), sq(source["ru"]), sq(url),
                     sq(status), sq(verified), sq(title["en"]), sq(title["uz"]), sq(body["en"]), sq(body["uz"]),
                     sq(source["en"]), sq(source["uz"])])
    out.append(upsert("cards", ["id", "kind", "section", "sort", "title", "body", "source", "source_url", "status",
                                "verified_at", "title_en", "title_uz", "body_en", "body_uz", "source_en", "source_uz"], rows))

    rows = [[sq(cid), str(i), sq(title["ru"]), sq(teacher), sq(level["ru"]), str(price), sq(glyph), sq(desc["ru"]),
             sq(title["en"]), sq(title["uz"]), sq(level["en"]), sq(level["uz"]), sq(desc["en"]), sq(desc["uz"])]
            for i, (cid, title, teacher, level, price, glyph, desc) in enumerate(COURSES)]
    out.append(upsert("courses", ["id", "sort", "title", "teacher", "level", "price_uzs", "glyph", "description",
                                  "title_en", "title_uz", "level_en", "level_uz", "description_en", "description_uz"], rows))

    rows = [[sq(lid), sq(course), str(i), sq(title["ru"]), str(minutes), b(free), sq(content["ru"]),
             sq(title["en"]), sq(title["uz"]), sq(content["en"]), sq(content["uz"])]
            for i, (lid, course, title, minutes, free, content) in enumerate(LESSONS)]
    out.append(upsert("lessons", ["id", "course_id", "sort", "title", "minutes", "free", "content",
                                  "title_en", "title_uz", "content_en", "content_uz"], rows))

    rows = [[sq(uid), str(i), sq(name), sq(city["ru"]), sq(typ["ru"]), sq(url),
             sq(city["en"]), sq(city["uz"]), sq(typ["en"]), sq(typ["uz"])]
            for i, (uid, name, city, typ, url) in enumerate(UNIVERSITIES)]
    out.append(upsert("universities", ["id", "sort", "name", "city", "type", "url",
                                       "city_en", "city_uz", "type_en", "type_uz"], rows))

    # old app versions saved the goal as a Russian word; turn it into the new key
    out.append("update public.profiles set goal = case goal\n"
               "  when 'Бакалавриат' then 'bachelor' when 'Магистратура' then 'master'\n"
               "  when 'Языковые курсы' then 'language' when 'Обмен' then 'exchange' else goal end\n"
               "where goal in ('Бакалавриат', 'Магистратура', 'Языковые курсы', 'Обмен');\n")
    return "\n".join(out)


if __name__ == "__main__":
    ids = [c[0] for c in CARDS] + [c[0] for c in COURSES] + [l[0] for l in LESSONS] + [u[0] for u in UNIVERSITIES]
    assert len(ids) == len(set(ids)), "duplicate id"
    course_ids = {c[0] for c in COURSES}
    assert all(l[1] in course_ids for l in LESSONS), "lesson points to unknown course"
    assert all(c[7] in ("verified", "check", "tip") for c in CARDS)
    assert all(c[5]["ru"] for c in CARDS), "every card needs a source"
    # every text must exist in all three languages
    texts = [t for c in CARDS for t in (c[3], c[4], c[5])] + [t for c in COURSES for t in (c[1], c[3], c[6])] \
        + [t for l in LESSONS for t in (l[2], l[5])] + [t for u in UNIVERSITIES for t in (u[2], u[3])]
    for t in texts:
        assert t["ru"] and t["en"] and t["uz"], f"missing translation: {t['ru'][:40]}"

    kotlin = ROOT / "app/src/main/java/uz/dream/app/data/SeedData.kt"
    kotlin.write_text(build_kotlin(), encoding="utf-8")
    sql = ROOT / "supabase/schema.sql"
    sql.write_text(build_sql(), encoding="utf-8")
    print(f"wrote {kotlin.relative_to(ROOT)} and {sql.relative_to(ROOT)}: "
          f"{len(CARDS)} cards, {len(COURSES)} courses, {len(LESSONS)} lessons, {len(UNIVERSITIES)} universities")
