package uz.dream.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.dream.app.data.Course
import uz.dream.app.data.DreamRepository
import uz.dream.app.data.Lesson
import uz.dream.app.data.Rooms
import uz.dream.app.ui.DreamCard
import uz.dream.app.ui.DreamHeader
import uz.dream.app.ui.DreamViewModel
import uz.dream.app.ui.GlyphTile
import uz.dream.app.ui.Tag
import uz.dream.app.ui.shortTime
import uz.dream.app.ui.theme.SaffronInk
import uz.dream.app.ui.theme.SaffronSoft
import uz.dream.app.ui.theme.TurquoiseDeep
import uz.dream.app.ui.theme.TurquoiseSoft
import uz.dream.app.ui.uzs

/** Tab 3: the Korean learning marketplace. */
@Composable
fun CoursesScreen(vm: DreamViewModel, onCourse: (String) -> Unit) {
    val courses by vm.courses.collectAsStateWithLifecycle()
    val enrolled by vm.enrolledIds.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        DreamHeader(title = "Курсы корейского", subtitle = "Записанные уроки от преподавателей. 85% цены получает преподаватель.")
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "demo_note") {
                Tag("Демо-каталог: преподаватели и оплата пока не настоящие", SaffronSoft, SaffronInk)
            }
            items(courses, key = { it.id }) { course ->
                DreamCard(onClick = { onCourse(course.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GlyphTile(course.glyph, size = 64.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(course.title, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                course.teacher + " · " + course.level,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            if (course.id in enrolled) {
                                Tag("Куплен", TurquoiseSoft, TurquoiseDeep)
                            } else {
                                Text(uzs(course.priceUzs), style = MaterialTheme.typography.titleSmall, color = TurquoiseDeep)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** One course: lessons, purchase, its discussion room and questions to the teacher. */
@Composable
fun CourseScreen(
    vm: DreamViewModel,
    courseId: String,
    onBack: () -> Unit,
    onLesson: (String) -> Unit,
    onRoom: (String) -> Unit,
    onQuestions: (String) -> Unit
) {
    val course by remember(courseId) { vm.course(courseId) }.collectAsStateWithLifecycle(initialValue = null)
    val lessons by remember(courseId) { vm.lessons(courseId) }.collectAsStateWithLifecycle(initialValue = emptyList())
    val enrolled by vm.enrolledIds.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    var showPay by rememberSaveable { mutableStateOf(false) }
    val c = course
    val owned = courseId in enrolled

    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        DreamHeader(title = c?.title ?: "", subtitle = c?.teacher, onBack = onBack)
        if (c != null) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlyphTile(c.glyph, size = 72.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Уровень: " + c.level, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(c.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (owned) {
                    Tag("Курс куплен: все уроки открыты", TurquoiseSoft, TurquoiseDeep)
                    OutlinedButton(
                        onClick = { onRoom(Rooms.course(c.id)) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                    ) { Text("Комната курса") }
                    OutlinedButton(
                        onClick = { onQuestions(c.id) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                    ) { Text("Вопросы преподавателю") }
                } else {
                    Button(
                        onClick = { showPay = true },
                        enabled = !busy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Купить за " + uzs(c.priceUzs)) }
                    Text(
                        "В курс входят: комната для обсуждения и ${DreamRepository.QUESTION_LIMIT} вопроса преподавателю.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text("Уроки", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 6.dp))
                lessons.forEach { lesson ->
                    LessonRow(lesson, open = owned || lesson.free, onClick = { onLesson(lesson.id) })
                }
            }
        }
    }

    if (showPay && c != null) {
        PayDialog(c, onConfirm = { showPay = false; vm.buyCourse(c.id) }, onDismiss = { showPay = false })
    }
}

@Composable
private fun LessonRow(lesson: Lesson, open: Boolean, onClick: () -> Unit) {
    DreamCard(onClick = if (open) onClick else null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (open) Icons.Filled.PlayArrow else Icons.Filled.Lock,
                contentDescription = if (open) "Открыт" else "Закрыт",
                tint = if (open) TurquoiseDeep else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(lesson.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${lesson.minutes} мин" + if (lesson.free) " · бесплатно" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Shows where the money goes before the (demo) payment. */
@Composable
private fun PayDialog(course: Course, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val fee = DreamRepository.platformFee(course.priceUzs)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Покупка курса") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(course.title, style = MaterialTheme.typography.titleSmall)
                Text("Цена: " + uzs(course.priceUzs))
                Text("Преподавателю (85%): " + uzs(course.priceUzs - fee))
                Text("Платформе Dream (15%): " + uzs(fee))
                Spacer(Modifier.height(4.dp))
                Text(
                    "Это демо-оплата: настоящие деньги не списываются.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text("Оплатить (демо)") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
fun LessonScreen(vm: DreamViewModel, lessonId: String, onBack: () -> Unit) {
    val lesson by remember(lessonId) { vm.lesson(lessonId) }.collectAsStateWithLifecycle(initialValue = null)
    val enrolled by vm.enrolledIds.collectAsStateWithLifecycle()
    val l = lesson

    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        DreamHeader(title = l?.title ?: "", subtitle = if (l == null) null else "${l.minutes} мин", onBack = onBack)
        if (l != null) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (l.free || l.courseId in enrolled) {
                    Tag("Конспект урока. Видео преподаватель загрузит позже.", SaffronSoft, SaffronInk)
                    DreamCard {
                        Text(l.content, style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    Text("Этот урок откроется после покупки курса.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

/** Questions to the teacher: 3 per course, the counter is always visible. */
@Composable
fun QuestionsScreen(vm: DreamViewModel, courseId: String, onBack: () -> Unit) {
    val course by remember(courseId) { vm.course(courseId) }.collectAsStateWithLifecycle(initialValue = null)
    val questions by remember(courseId) { vm.questions(courseId) }.collectAsStateWithLifecycle(initialValue = emptyList())
    var text by rememberSaveable { mutableStateOf("") }
    val left = (DreamRepository.QUESTION_LIMIT - questions.size).coerceAtLeast(0)

    LaunchedEffect(courseId) { vm.loadQuestions(courseId) }

    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        DreamHeader(title = "Вопросы преподавателю", subtitle = course?.title, onBack = onBack)
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "left") {
                Tag("Осталось вопросов: $left из ${DreamRepository.QUESTION_LIMIT}", TurquoiseSoft, TurquoiseDeep)
            }
            items(questions, key = { it.id }) { q ->
                DreamCard {
                    Text(shortTime(q.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(q.body, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    if (q.answer.isBlank()) {
                        Text("Преподаватель ещё не ответил", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("Ответ: " + q.answer, style = MaterialTheme.typography.bodyMedium, color = TurquoiseDeep)
                    }
                }
            }
        }
        if (left > 0) {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = text, onValueChange = { text = it.take(1000) },
                    label = { Text("Твой вопрос") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), maxLines = 4
                )
                Button(
                    onClick = { vm.askQuestion(courseId, text); text = "" },
                    enabled = text.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                ) { Text("Отправить вопрос") }
            }
        }
    }
}
