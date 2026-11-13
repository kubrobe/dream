package uz.dream.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import uz.dream.app.R
import uz.dream.app.data.Goal
import uz.dream.app.data.Message
import uz.dream.app.data.Rooms
import uz.dream.app.data.Sections
import uz.dream.app.data.ThemeMode
import uz.dream.app.data.University
import uz.dream.app.data.city
import uz.dream.app.data.title
import uz.dream.app.data.type
import uz.dream.app.ui.Avatar
import uz.dream.app.ui.CountingText
import uz.dream.app.ui.DreamCard
import uz.dream.app.ui.DreamHeader
import uz.dream.app.ui.DreamViewModel
import uz.dream.app.ui.EmptyState
import uz.dream.app.ui.LanguageSwitch
import uz.dream.app.ui.LocalLang
import uz.dream.app.ui.Pill
import uz.dream.app.ui.SecondaryButton
import uz.dream.app.ui.Segmented
import uz.dream.app.ui.Tag
import uz.dream.app.ui.bounceClick
import uz.dream.app.ui.enter
import uz.dream.app.ui.rememberEntrance
import uz.dream.app.ui.shortTime
import uz.dream.app.ui.theme.DreamTheme
import uz.dream.app.ui.theme.HeadingFont
import uz.dream.app.ui.toneOf

/** A community room: students help each other. New messages are fetched every few seconds. */
@Composable
fun RoomScreen(vm: DreamViewModel, roomId: String, onBack: () -> Unit) {
    val session by vm.session.collectAsStateWithLifecycle()
    val messages by remember(roomId) { vm.messages(roomId) }.collectAsStateWithLifecycle(initialValue = emptyList())
    val course by remember(roomId) { vm.course(Rooms.courseId(roomId)) }.collectAsStateWithLifecycle(initialValue = null)
    val lang = LocalLang.current
    var text by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val isCourse = Rooms.isCourse(roomId)
    val title = if (isCourse) (course?.title(lang) ?: stringResource(R.string.course_room)) else stringResource(Sections.title(Rooms.sectionKey(roomId)))
    val glyph = if (isCourse) (course?.glyph ?: "한") else Sections.glyph(Rooms.sectionKey(roomId))

    // keep asking the server for new messages while this screen is open
    LaunchedEffect(roomId) {
        while (true) {
            vm.pollRoom(roomId)
            delay(4_000)
        }
    }
    // glide to the newest message when one arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        DreamHeader(
            title = title,
            subtitle = stringResource(if (vm.online) R.string.room_subtitle else R.string.room_local),
            glyph = glyph,
            onBack = onBack
        )
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item(key = "empty") { EmptyState(glyph, stringResource(R.string.room_empty)) }
            }
            itemsIndexed(messages, key = { _, m -> m.id }) { i, m ->
                val previous = messages.getOrNull(i - 1)
                Bubble(
                    m,
                    mine = m.userId == session?.userId,
                    showAuthor = previous?.userId != m.userId,
                    modifier = Modifier.animateItem()
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text, onValueChange = { text = it.take(1000) },
                placeholder = { Text(stringResource(R.string.message_hint)) },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), maxLines = 4
            )
            Spacer(Modifier.width(8.dp))
            val ready = text.isNotBlank()
            val scale by animateFloatAsState(if (ready) 1f else 0.82f, spring(dampingRatio = 0.45f), label = "send-scale")
            val back by animateColorAsState(if (ready) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, label = "send-bg")
            Box(
                Modifier
                    .size(50.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .bounceClick(enabled = ready, shape = CircleShape) { vm.sendMessage(roomId, text); text = "" }
                    .background(back),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.send),
                    tint = if (ready) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun Bubble(m: Message, mine: Boolean, showAuthor: Boolean, modifier: Modifier = Modifier) {
    val palette = DreamTheme.colors
    val name = if (mine) stringResource(R.string.you) else m.author.ifBlank { stringResource(R.string.student) }
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!mine) {
            if (showAuthor) Avatar(name, size = 32.dp, tone = toneOf(m.userId)) else Spacer(Modifier.width(32.dp))
            Spacer(Modifier.width(8.dp))
        }
        Column(
            Modifier
                .widthIn(max = 290.dp)
                .background(
                    if (mine) palette.brandSoft else MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(
                        topStart = 20.dp, topEnd = 20.dp,
                        bottomStart = if (mine) 20.dp else 6.dp,
                        bottomEnd = if (mine) 6.dp else 20.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (showAuthor || mine) {
                Text(
                    name + " · " + shortTime(m.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (mine) palette.onBrandSoft else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(3.dp))
            }
            Text(m.body, style = MaterialTheme.typography.bodyLarge, color = if (mine) palette.onBrandSoft else MaterialTheme.colorScheme.onSurface)
        }
    }
}

/** Tab 4: the pool where a student shows interest to a university, only with consent. */
@Composable
fun UniversitiesScreen(vm: DreamViewModel) {
    val universities by vm.universities.collectAsStateWithLifecycle()
    val interested by vm.interestIds.collectAsStateWithLifecycle()
    var asking by remember { mutableStateOf<University?>(null) }
    val uriHandler = LocalUriHandler.current
    val haptic = LocalHapticFeedback.current
    val lang = LocalLang.current
    val since = rememberEntrance()
    val palette = DreamTheme.colors

    Column(Modifier.fillMaxSize()) {
        DreamHeader(title = stringResource(R.string.unis_title), subtitle = stringResource(R.string.unis_subtitle), glyph = "학") {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = palette.accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.unis_count, interested.size), style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "demo_note") {
                Tag(stringResource(R.string.demo_unis), palette.accentSoft, palette.onAccentSoft, dot = palette.accent, modifier = Modifier.enter(0, since))
            }
            itemsIndexed(universities, key = { _, u -> u.id }) { i, u ->
                val on = u.id in interested
                DreamCard(modifier = Modifier.enter(i + 1, since)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(54.dp)
                                .background(palette.tileBrush(i), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(monogram(u.name), color = Color.White, fontFamily = HeadingFont, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(u.name, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    u.city(lang) + " · " + u.type(lang),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HeartButton(on = on) {
                            if (on) {
                                haptic.performHapticFeedback(HapticFeedbackType.ToggleOff)
                                vm.setInterest(u.id, false)
                            } else {
                                asking = u
                            }
                        }
                    }
                    AnimatedVisibility(visible = on, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                        Column {
                            Spacer(Modifier.height(10.dp))
                            Tag(stringResource(R.string.in_interest_list), palette.brandSoft, palette.onBrandSoft, dot = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (u.url.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = {
                            try {
                                uriHandler.openUri(u.url)
                            } catch (e: Exception) {
                                // no browser on the device
                            }
                        }) { Text(stringResource(R.string.website) + " →") }
                    }
                }
            }
        }
    }

    val target = asking
    if (target != null) {
        ConsentDialog(
            university = target,
            onConfirm = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                vm.setInterest(target.id, true)
                asking = null
            },
            onDismiss = { asking = null }
        )
    }
}

/** Heart that pops when switched on. */
@Composable
private fun HeartButton(on: Boolean, onClick: () -> Unit) {
    val palette = DreamTheme.colors
    val scale by animateFloatAsState(if (on) 1.15f else 1f, spring(dampingRatio = 0.3f, stiffness = Spring.StiffnessMedium), label = "heart")
    val back by animateColorAsState(if (on) palette.accentSoft else MaterialTheme.colorScheme.surfaceVariant, label = "heart-bg")
    Box(
        Modifier
            .size(46.dp)
            .bounceClick(shape = CircleShape, onClick = onClick)
            .background(back),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = on,
            transitionSpec = { (scaleIn(spring(dampingRatio = 0.4f)) + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
            label = "heart-icon"
        ) { isOn ->
            Icon(
                if (isOn) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = stringResource(if (isOn) R.string.withdraw_interest else R.string.interested),
                tint = if (isOn) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }
    }
}

/** "Seoul National University" -> "SNU", "KAIST" -> "KAI". */
private fun monogram(name: String): String {
    val words = name.split(" ").filter { it.isNotBlank() && it.first().isUpperCase() }
    return if (words.size >= 2) words.take(3).joinToString("") { it.take(1) } else name.take(3).uppercase()
}

/** Interest is saved only after the student ticks the consent box. */
@Composable
private fun ConsentDialog(university: University, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    var agreed by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        icon = { Icon(Icons.Filled.Favorite, contentDescription = null, tint = DreamTheme.colors.accent) },
        title = { Text(university.name, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.consent_text))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick(shape = RoundedCornerShape(14.dp)) { agreed = !agreed }
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(end = 10.dp)
                ) {
                    Checkbox(checked = agreed, onCheckedChange = { agreed = it })
                    Text(stringResource(R.string.consent_checkbox), style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    stringResource(R.string.consent_revoke),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirm, enabled = agreed) { Text(stringResource(R.string.confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

/** Tab 5: who is signed in, the study goal, numbers, language, theme, sync and logout. */
@Composable
fun ProfileScreen(vm: DreamViewModel) {
    val session by vm.session.collectAsStateWithLifecycle()
    val roadmap by vm.roadmap.collectAsStateWithLifecycle()
    val done by vm.doneIds.collectAsStateWithLifecycle()
    val enrolled by vm.enrolledIds.collectAsStateWithLifecycle()
    val interested by vm.interestIds.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val lang by vm.lang.collectAsStateWithLifecycle()
    val theme by vm.theme.collectAsStateWithLifecycle()
    val since = rememberEntrance()
    val palette = DreamTheme.colors
    val s = session ?: return
    val goal = Goal.of(s.goal)

    Column(Modifier.fillMaxSize()) {
        DreamHeader(title = s.fullName.ifBlank { stringResource(R.string.tab_profile) }, subtitle = s.email, glyph = "나") {
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(s.fullName.ifBlank { s.email }, size = 56.dp, tone = 1)
                Spacer(Modifier.width(12.dp))
                Tag(
                    stringResource(if (vm.online) R.string.online_mode else R.string.local_mode),
                    Color.White.copy(alpha = 0.18f),
                    Color.White,
                    dot = if (vm.online) palette.accent else Color.White
                )
            }
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.enter(0, since)) {
                StatTile(roadmap.count { it.id in done }, stringResource(R.string.stat_steps, roadmap.size), 0, Modifier.weight(1f))
                StatTile(enrolled.size, stringResource(R.string.stat_courses), 1, Modifier.weight(1f))
                StatTile(interested.size, stringResource(R.string.stat_unis), 3, Modifier.weight(1f))
            }

            DreamCard(modifier = Modifier.enter(1, since)) {
                Text(stringResource(R.string.my_goal), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.my_goal_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Goal.entries.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pair.forEach { g ->
                                Pill(stringResource(g.label), selected = goal == g, onClick = { vm.setGoal(g.key) })
                            }
                        }
                    }
                }
            }

            DreamCard(modifier = Modifier.enter(2, since)) {
                Text(stringResource(R.string.language), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                LanguageSwitch(current = lang, onPick = vm::setLang, full = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(18.dp))
                Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                Segmented(
                    options = ThemeMode.entries,
                    selected = theme,
                    label = {
                        stringResource(
                            when (it) {
                                ThemeMode.SYSTEM -> R.string.theme_system
                                ThemeMode.LIGHT -> R.string.theme_light
                                ThemeMode.DARK -> R.string.theme_dark
                            }
                        )
                    },
                    onSelect = vm::setTheme,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(Modifier.enter(3, since), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(
                    stringResource(if (busy) R.string.syncing else R.string.sync),
                    onClick = { vm.refresh() },
                    enabled = !busy,
                    icon = Icons.Filled.Refresh
                )
                TextButton(onClick = { vm.logout() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.error)
                }
                Text(
                    stringResource(R.string.about_line),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

/** Big number counting up, with a coloured strip on top. */
@Composable
private fun StatTile(value: Int, label: String, tone: Int, modifier: Modifier = Modifier) {
    val palette = DreamTheme.colors
    Column(
        modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Box(
            Modifier
                .width(26.dp)
                .height(5.dp)
                .background(palette.tileBrush(tone), RoundedCornerShape(50))
        )
        Spacer(Modifier.height(10.dp))
        CountingText(value)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
    }
}
