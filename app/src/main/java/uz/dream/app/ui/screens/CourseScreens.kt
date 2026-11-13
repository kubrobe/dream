package uz.dream.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.dream.app.R
import uz.dream.app.data.Course
import uz.dream.app.data.DreamRepository
import uz.dream.app.data.Lesson
import uz.dream.app.data.Rooms
import uz.dream.app.data.content
import uz.dream.app.data.description
import uz.dream.app.data.level
import uz.dream.app.data.title
import uz.dream.app.ui.DreamCard
import uz.dream.app.ui.DreamHeader
import uz.dream.app.ui.DreamViewModel
import uz.dream.app.ui.EmptyState
import uz.dream.app.ui.GlyphTile
import uz.dream.app.ui.LocalLang
import uz.dream.app.ui.PrimaryButton
import uz.dream.app.ui.SecondaryButton
import uz.dream.app.ui.SkeletonCards
import uz.dream.app.ui.Tag
import uz.dream.app.ui.enter
import uz.dream.app.ui.money
import uz.dream.app.ui.rememberEntrance
import uz.dream.app.ui.shortTime
import uz.dream.app.ui.theme.DreamTheme
import uz.dream.app.ui.theme.HeadingFont

/** Tab 3: the Korean learning marketplace. */
@Composable
fun CoursesScreen(vm: DreamViewModel, onCourse: (String) -> Unit) {
    val courses by vm.courses.collectAsStateWithLifecycle()
    val enrolled by vm.enrolledIds.collectAsStateWithLifecycle()
    val lang = LocalLang.current
    val since = rememberEntrance()
    val palette = DreamTheme.colors

    Column(Modifier.fillMaxSize()) {
        DreamHeader(title = stringResource(R.string.courses_title), subtitle = stringResource(R.string.courses_subtitle), glyph = "한")
        if (courses.isEmpty()) {
            Box(Modifier.padding(16.dp)) { SkeletonCards() }
            return@Column
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "split") {
                DreamCard(modifier = Modifier.enter(0, since), background = palette.brandSoft, border = palette.brandSoft) {
                    Text(stringResource(R.string.split_title), style = MaterialTheme.typography.titleSmall, color = palette.onBrandSoft)
                    Spacer(Modifier.height(10.dp))
                    SplitBar(teacherShare = 0.85f)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Text(stringResource(R.string.split_teacher), style = MaterialTheme.typography.labelMedium, color = palette.onBrandSoft, modifier = Modifier.weight(1f))
                        Text(stringResource(R.string.split_platform), style = MaterialTheme.typography.labelMedium, color = palette.onBrandSoft)
                    }
                }
            }
            item(key = "demo_note") {
                Tag(stringResource(R.string.demo_catalog), palette.accentSoft, palette.onAccentSoft, dot = palette.accent, modifier = Modifier.enter(1, since))
            }
            itemsIndexed(courses, key = { _, c -> c.id }) { i, course ->
                val owned = course.id in enrolled
                DreamCard(onClick = { onCourse(course.id) }, modifier = Modifier.enter(i + 2, since), padding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GlyphTile(course.glyph, size = 78.dp, tone = i)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(course.title(lang), style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(3.dp))
                            Text(
                                course.teacher + " · " + course.level(lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            AnimatedContent(targetState = owned, label = "owned") { isOwned ->
                                if (isOwned) {
                                    Tag(stringResource(R.string.owned), palette.brandSoft, palette.onBrandSoft, dot = MaterialTheme.colorScheme.primary)
                                } else {
                                    Text(money(course.priceUzs), fontFamily = HeadingFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Two-colour bar that grows in: teacher's part and Dream's part of the price. */
@Composable
private fun SplitBar(teacherShare: Float) {
    val grow = remember { Animatable(0f) }
    LaunchedEffect(Unit) { grow.animateTo(1f, tween(900, delayMillis = 200, easing = FastOutSlowInEasing)) }
    val palette = DreamTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(50))
            .background(palette.track)
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .weight(0.0001f + teacherShare * grow.value)
                .background(MaterialTheme.colorScheme.primary)
        )
        Box(
            Modifier
                .fillMaxHeight()
                .weight(0.0001f + (1f - teacherShare) * grow.value)
                .background(palette.accent)
        )
        if (grow.value < 1f) Spacer(Modifier.weight(1.0001f - grow.value))
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
    val lang = LocalLang.current
    val since = rememberEntrance()
    var showPay by rememberSaveable { mutableStateOf(false) }
    var justBought by rememberSaveable { mutableStateOf(false) }
    val c = course
    val owned = courseId in enrolled
    val palette = DreamTheme.colors

    LaunchedEffect(courseId) {
        vm.purchased.collect { if (it == courseId) justBought = true }
    }

    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        DreamHeader(title = c?.title(lang) ?: "", subtitle = c?.teacher, glyph = c?.glyph ?: "한", onBack = onBack)
        if (c == null) return@Column
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.enter(0, since)) {
                GlyphTile(c.glyph, size = 88.dp, tone = c.sort)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Tag(stringResource(R.string.level, c.level(lang)), palette.brandSoft, palette.onBrandSoft)
                    Spacer(Modifier.height(8.dp))
                    Text(c.description(lang), style = MaterialTheme.typography.bodyMedium)
                }
            }

            DreamCard(modifier = Modifier.enter(1, since)) {
                Text(stringResource(R.string.course_includes), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(10.dp))
                Perk(stringResource(R.string.perk_lessons, lessons.size))
                Perk(stringResource(R.string.perk_room))
                Perk(stringResource(R.string.perk_questions, DreamRepository.QUESTION_LIMIT))
            }

            AnimatedVisibility(
                visible = owned,
                enter = expandVertically(spring(dampingRatio = 0.8f)) + fadeIn() + scaleIn(initialScale = 0.9f),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PurchasedBanner(celebrate = justBought)
                    SecondaryButton(stringResource(R.string.course_room), onClick = { onRoom(Rooms.course(c.id)) }, icon = Icons.Filled.Face)
                    SecondaryButton(stringResource(R.string.questions_title), onClick = { onQuestions(c.id) }, icon = Icons.AutoMirrored.Filled.Send)
                }
            }

            Text(stringResource(R.string.lessons), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
            lessons.forEachIndexed { i, lesson ->
                LessonRow(
                    number = i + 1,
                    lesson = lesson,
                    open = owned || lesson.free,
                    onClick = { onLesson(lesson.id) },
                    modifier = Modifier.enter(i + 2, since)
                )
            }
        }

        // sticky buy bar at the bottom
        AnimatedVisibility(
            visible = !owned,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.price), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(money(c.priceUzs), fontFamily = HeadingFont, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                PrimaryButton(
                    text = stringResource(R.string.buy),
                    icon = Icons.Filled.ShoppingCart,
                    onClick = { showPay = true },
                    busy = busy,
                    modifier = Modifier.width(170.dp)
                )
            }
        }
    }

    if (showPay && c != null) {
        PayDialog(c, onConfirm = { showPay = false; vm.buyCourse(c.id) }, onDismiss = { showPay = false })
    }
}

@Composable
private fun Perk(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(
            Modifier
                .size(22.dp)
                .background(DreamTheme.colors.brandSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

/** "Course unlocked" banner. Right after buying, the tile spins in and bursts with small sparks. */
@Composable
private fun PurchasedBanner(celebrate: Boolean) {
    val palette = DreamTheme.colors
    val spin = remember { Animatable(if (celebrate) 0f else 1f) }
    LaunchedEffect(celebrate) {
        if (celebrate) spin.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow))
    }
    DreamCard(background = palette.accentSoft, border = palette.accent.copy(alpha = 0.5f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                if (celebrate) Sparks(progress = spin.value)
                GlyphTile(
                    "축", size = 48.dp, tone = 1,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = (1f - spin.value) * -180f
                        scaleX = 0.4f + 0.6f * spin.value
                        scaleY = 0.4f + 0.6f * spin.value
                    }
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(stringResource(R.string.owned_title), style = MaterialTheme.typography.titleMedium, color = palette.onAccentSoft)
                Text(stringResource(R.string.owned_text), style = MaterialTheme.typography.bodySmall, color = palette.onAccentSoft)
            }
        }
    }
}

/** Eight dots flying out of the centre, fading as they go. */
@Composable
private fun Sparks(progress: Float) {
    val colors = listOf(DreamTheme.colors.accent, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
    Box(
        Modifier
            .size(48.dp)
            .graphicsLayer { alpha = (1f - progress).coerceIn(0f, 1f) * 1.4f }
    ) {
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            val center = this.center
            val maxR = size.minDimension * 0.95f
            for (k in 0 until 8) {
                val angle = Math.toRadians(k * 45.0 + 20.0)
                val r = maxR * progress
                drawCircle(
                    colors[k % colors.size],
                    radius = 4.dp.toPx() * (1f - progress * 0.6f),
                    center = androidx.compose.ui.geometry.Offset(
                        center.x + (kotlin.math.cos(angle) * r).toFloat(),
                        center.y + (kotlin.math.sin(angle) * r).toFloat()
                    )
                )
            }
        }
    }
}

@Composable
private fun LessonRow(number: Int, lesson: Lesson, open: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val lang = LocalLang.current
    val palette = DreamTheme.colors
    val tint by animateColorAsState(if (open) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, label = "lesson-tint")
    DreamCard(onClick = if (open) onClick else null, modifier = modifier, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "%02d".format(number),
                fontFamily = HeadingFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (open) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(lesson.title(lang), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.minutes, lesson.minutes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (lesson.free) {
                        Spacer(Modifier.width(8.dp))
                        Tag(stringResource(R.string.free), palette.accentSoft, palette.onAccentSoft)
                    }
                }
            }
            Box(
                Modifier
                    .size(38.dp)
                    .background(if (open) palette.brandSoft else MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (open) Icons.Filled.PlayArrow else Icons.Filled.Lock,
                    contentDescription = stringResource(if (open) R.string.lesson_open else R.string.lesson_locked),
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/** Shows where the money goes before the (demo) payment. */
@Composable
private fun PayDialog(course: Course, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val lang = LocalLang.current
    val fee = DreamRepository.platformFee(course.priceUzs)
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        icon = { GlyphTile(course.glyph, size = 52.dp, tone = course.sort) },
        title = { Text(stringResource(R.string.pay_title), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(course.title(lang), style = MaterialTheme.typography.titleMedium)
                SplitBar(teacherShare = 0.85f)
                MoneyRow(stringResource(R.string.pay_teacher), money(course.priceUzs - fee), MaterialTheme.colorScheme.primary)
                MoneyRow(stringResource(R.string.pay_platform), money(fee), DreamTheme.colors.accent)
                MoneyRow(stringResource(R.string.pay_total), money(course.priceUzs), null)
                Text(
                    stringResource(R.string.pay_demo_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            PrimaryButton(stringResource(R.string.pay_confirm), onClick = onConfirm, modifier = Modifier.width(180.dp))
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun MoneyRow(label: String, value: String, dot: Color?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (dot != null) {
            Box(
                Modifier
                    .size(10.dp)
                    .background(dot, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleSmall)
    }
}

/**
 * A lesson. Lines like "안녕하세요 — hello" become flash cards.
 * Practice mode hides the translations: tap a card to reveal it.
 */
@Composable
fun LessonScreen(vm: DreamViewModel, lessonId: String, onBack: () -> Unit) {
    val lesson by remember(lessonId) { vm.lesson(lessonId) }.collectAsStateWithLifecycle(initialValue = null)
    val enrolled by vm.enrolledIds.collectAsStateWithLifecycle()
    val lang = LocalLang.current
    val since = rememberEntrance()
    var practice by rememberSaveable { mutableStateOf(false) }
    val revealed = remember(lessonId, practice) { mutableStateListOf<Int>() }
    val l = lesson
    val palette = DreamTheme.colors

    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        DreamHeader(
            title = l?.title(lang) ?: "",
            subtitle = if (l == null) null else stringResource(R.string.minutes, l.minutes),
            glyph = "말",
            onBack = onBack
        )
        if (l == null) return@Column
        if (!(l.free || l.courseId in enrolled)) {
            EmptyState("잠", stringResource(R.string.lesson_locked_text))
            return@Column
        }
        val lines = l.content(lang).split("\n")
        val pairs = lines.mapNotNull { flashPair(it) }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Tag(stringResource(R.string.lesson_note), palette.accentSoft, palette.onAccentSoft, dot = palette.accent, modifier = Modifier.enter(0, since))
            if (pairs.size >= 3) {
                DreamCard(modifier = Modifier.enter(1, since), padding = 12.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.practice_mode), style = MaterialTheme.typography.titleSmall)
                            Text(stringResource(R.string.practice_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = practice, onCheckedChange = { practice = it })
                    }
                }
            }
            var pairIndex = 0
            lines.forEachIndexed { i, line ->
                val pair = flashPair(line)
                if (pair != null) {
                    val k = pairIndex++
                    val shown = !practice || k in revealed
                    FlashRow(
                        front = pair.first,
                        back = pair.second,
                        shown = shown,
                        onTap = { if (practice && k !in revealed) revealed.add(k) },
                        modifier = Modifier.enter(i + 2, since)
                    )
                } else if (line.isNotBlank()) {
                    Text(line, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.enter(i + 2, since))
                } else {
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

/** "안녕하세요 — hello" -> ("안녕하세요", "hello"). Only lines that start with Korean become flash cards. */
private fun flashPair(line: String): Pair<String, String>? {
    val parts = line.split(" — ", limit = 2)
    if (parts.size != 2) return null
    val front = parts[0].trim()
    val hasHangul = front.any { it in '\uAC00'..'\uD7A3' || it in '\u3131'..'\u318E' }
    return if (hasHangul && parts[1].isNotBlank()) front to parts[1].trim() else null
}

@Composable
private fun FlashRow(front: String, back: String, shown: Boolean, onTap: () -> Unit, modifier: Modifier = Modifier) {
    val reveal by animateFloatAsState(if (shown) 1f else 0f, tween(320), label = "reveal")
    DreamCard(onClick = onTap, modifier = modifier, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                front,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(0.45f)
            )
            Spacer(Modifier.width(12.dp))
            Box(Modifier.weight(0.55f)) {
                Text(
                    back,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.graphicsLayer {
                        alpha = reveal
                        translationX = (1f - reveal) * 16.dp.toPx()
                    }
                )
                if (reveal < 1f) {
                    Box(
                        Modifier
                            .graphicsLayer { alpha = 1f - reveal }
                            .fillMaxWidth()
                            .height(22.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
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
    val lang = LocalLang.current
    var text by rememberSaveable { mutableStateOf("") }
    val left = (DreamRepository.QUESTION_LIMIT - questions.size).coerceAtLeast(0)
    val palette = DreamTheme.colors

    LaunchedEffect(courseId) { vm.loadQuestions(courseId) }

    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        DreamHeader(title = stringResource(R.string.questions_title), subtitle = course?.title(lang), glyph = "문", onBack = onBack) {
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(DreamRepository.QUESTION_LIMIT) { i ->
                    val filled = i < left
                    val scale by animateFloatAsState(if (filled) 1f else 0.7f, spring(dampingRatio = 0.4f), label = "q-dot")
                    Box(
                        Modifier
                            .padding(end = 6.dp)
                            .size(14.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .background(if (filled) palette.accent else Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.questions_left, left, DreamRepository.QUESTION_LIMIT),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (questions.isEmpty()) {
                item(key = "empty") { EmptyState("문", stringResource(R.string.questions_empty)) }
            }
            items(questions, key = { it.id }) { q ->
                DreamCard(modifier = Modifier.animateItem()) {
                    Text(shortTime(q.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(q.body, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(10.dp))
                    if (q.answer.isBlank()) {
                        Tag(stringResource(R.string.no_answer_yet), MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, dot = palette.accent)
                    } else {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(palette.brandSoft, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Text(stringResource(R.string.answer), style = MaterialTheme.typography.labelMedium, color = palette.onBrandSoft)
                            Text(q.answer, style = MaterialTheme.typography.bodyMedium, color = palette.onBrandSoft)
                        }
                    }
                }
            }
        }
        AnimatedContent(targetState = left > 0, label = "ask-box") { canAsk ->
            if (canAsk) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = text, onValueChange = { text = it.take(1000) },
                        label = { Text(stringResource(R.string.your_question)) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), maxLines = 4
                    )
                    PrimaryButton(
                        stringResource(R.string.send_question),
                        icon = Icons.AutoMirrored.Filled.Send,
                        onClick = { vm.askQuestion(courseId, text); text = "" },
                        enabled = text.isNotBlank()
                    )
                }
            } else {
                Text(
                    stringResource(R.string.questions_used_up),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                )
            }
        }
    }
}
