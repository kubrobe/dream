package uz.dream.app.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import uz.dream.app.data.Message
import uz.dream.app.data.Rooms
import uz.dream.app.data.Sections
import uz.dream.app.data.University
import uz.dream.app.ui.DreamCard
import uz.dream.app.ui.DreamHeader
import uz.dream.app.ui.DreamViewModel
import uz.dream.app.ui.Pill
import uz.dream.app.ui.Tag
import uz.dream.app.ui.shortTime
import uz.dream.app.ui.theme.SaffronInk
import uz.dream.app.ui.theme.SaffronSoft
import uz.dream.app.ui.theme.TurquoiseDeep
import uz.dream.app.ui.theme.TurquoiseSoft

/** A community room: students help each other. New messages are fetched every few seconds. */
@Composable
fun RoomScreen(vm: DreamViewModel, roomId: String, onBack: () -> Unit) {
    val session by vm.session.collectAsStateWithLifecycle()
    val messages by remember(roomId) { vm.messages(roomId) }.collectAsStateWithLifecycle(initialValue = emptyList())
    val course by remember(roomId) { vm.course(Rooms.courseId(roomId)) }.collectAsStateWithLifecycle(initialValue = null)
    var text by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val title = if (Rooms.isCourse(roomId)) (course?.title ?: "Комната курса") else Sections.title(Rooms.sectionKey(roomId))

    // keep asking the server for new messages while this screen is open
    LaunchedEffect(roomId) {
        while (true) {
            vm.pollRoom(roomId)
            delay(4_000)
        }
    }
    // jump to the newest message when one arrives
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
            subtitle = if (vm.online) "Комната: здесь помогают друг другу" else "Локальный режим: сообщения видны только на этом телефоне",
            onBack = onBack
        )
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item(key = "empty") {
                    Text(
                        "Пока тихо. Задай первый вопрос — кто-то уже проходил этот шаг.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(messages, key = { it.id }) { m ->
                Bubble(m, mine = m.userId == session?.userId)
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text, onValueChange = { text = it.take(1000) },
                placeholder = { Text("Сообщение") },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp), maxLines = 4
            )
            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = { vm.sendMessage(roomId, text); text = "" },
                enabled = text.isNotBlank()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send, contentDescription = "Отправить",
                    tint = if (text.isNotBlank()) TurquoiseDeep else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun Bubble(m: Message, mine: Boolean) {
    Box(Modifier.fillMaxWidth(), contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart) {
        Column(
            Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (mine) TurquoiseSoft else MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                (if (mine) "Ты" else m.author.ifBlank { "Студент" }) + " · " + shortTime(m.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(m.body, style = MaterialTheme.typography.bodyLarge)
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

    Column(Modifier.fillMaxSize()) {
        DreamHeader(title = "Вузы", subtitle = "Покажи университету, что он тебе интересен")
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "demo_note") {
                Tag("Демо-список: эти вузы пока не подключены к Dream и не видят заявки", SaffronSoft, SaffronInk)
            }
            items(universities, key = { it.id }) { u ->
                val on = u.id in interested
                DreamCard {
                    Text(u.name, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(2.dp))
                    Text(u.city + " · " + u.type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (on) {
                            OutlinedButton(onClick = { vm.setInterest(u.id, false) }, shape = RoundedCornerShape(14.dp)) {
                                Text("Отозвать интерес")
                            }
                        } else {
                            Button(onClick = { asking = u }, shape = RoundedCornerShape(14.dp)) {
                                Text("Мне интересно")
                            }
                        }
                        if (u.url.isNotBlank()) {
                            TextButton(onClick = {
                                try {
                                    uriHandler.openUri(u.url)
                                } catch (e: Exception) {
                                    // no browser on the device
                                }
                            }) { Text("Сайт") }
                        }
                    }
                    if (on) {
                        Spacer(Modifier.height(6.dp))
                        Tag("Ты в списке интересующихся", TurquoiseSoft, TurquoiseDeep)
                    }
                }
            }
        }
    }

    val target = asking
    if (target != null) {
        ConsentDialog(
            university = target,
            onConfirm = { vm.setInterest(target.id, true); asking = null },
            onDismiss = { asking = null }
        )
    }
}

/** Interest is saved only after the student ticks the consent box. */
@Composable
private fun ConsentDialog(university: University, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    var agreed by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(university.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Мы добавим тебя в список студентов, которым интересен этот вуз.")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = agreed, onCheckedChange = { agreed = it })
                    Text(
                        "Я согласен, что вуз увидит моё имя, email и цель обучения",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    "Отозвать согласие можно в любой момент.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { Button(onClick = onConfirm, enabled = agreed) { Text("Подтвердить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

private val goals = listOf("Бакалавриат", "Магистратура", "Языковые курсы", "Обмен")

/** Tab 5: who is signed in, the study goal, numbers, sync and logout. */
@Composable
fun ProfileScreen(vm: DreamViewModel) {
    val session by vm.session.collectAsStateWithLifecycle()
    val roadmap by vm.roadmap.collectAsStateWithLifecycle()
    val done by vm.doneIds.collectAsStateWithLifecycle()
    val enrolled by vm.enrolledIds.collectAsStateWithLifecycle()
    val interested by vm.interestIds.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val s = session ?: return

    Column(Modifier.fillMaxSize()) {
        DreamHeader(title = s.fullName.ifBlank { "Профиль" }, subtitle = s.email)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (vm.online) {
                Tag("Онлайн: аккаунт и данные хранятся в Supabase", TurquoiseSoft, TurquoiseDeep)
            } else {
                Tag("Локальный режим: данные только на этом телефоне", SaffronSoft, SaffronInk)
            }

            DreamCard {
                Text("Моя цель", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    goals.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pair.forEach { goal ->
                                Pill(goal, selected = s.goal == goal, onClick = { vm.setGoal(goal) })
                            }
                        }
                    }
                }
            }

            DreamCard {
                Text("Мой прогресс", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Stat("Шагов пути сделано", "${roadmap.count { it.id in done }} из ${roadmap.size}")
                Stat("Курсов куплено", enrolled.size.toString())
                Stat("Вузов в списке интереса", interested.size.toString())
            }

            OutlinedButton(
                onClick = { vm.refresh() }, enabled = !busy,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
            ) { Text(if (busy) "Обновляю…" else "Обновить данные") }

            TextButton(onClick = { vm.logout() }, modifier = Modifier.fillMaxWidth()) {
                Text("Выйти из аккаунта", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, color = Color.Unspecified)
    }
}
