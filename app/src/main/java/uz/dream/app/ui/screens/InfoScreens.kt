package uz.dream.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.dream.app.data.InfoCard
import uz.dream.app.data.Rooms
import uz.dream.app.data.Sections
import uz.dream.app.ui.DreamCard
import uz.dream.app.ui.DreamHeader
import uz.dream.app.ui.DreamViewModel
import uz.dream.app.ui.ProgressLine
import uz.dream.app.ui.SectionTitle
import uz.dream.app.ui.StatusChip

/** Tab 1: the road from Uzbekistan to a Korean university, as a checklist. */
@Composable
fun RoadmapScreen(vm: DreamViewModel, onCard: (String) -> Unit, onRoom: (String) -> Unit) {
    val cards by vm.roadmap.collectAsStateWithLifecycle()
    val done by vm.doneIds.collectAsStateWithLifecycle()
    val doneCount = cards.count { it.id in done }
    val fraction = if (cards.isEmpty()) 0f else doneCount.toFloat() / cards.size

    Column(Modifier.fillMaxSize()) {
        DreamHeader(title = "Путь в Корею", subtitle = "Узбекистан → Южная Корея, шаг за шагом") {
            Spacer(Modifier.height(14.dp))
            ProgressLine(fraction)
            Spacer(Modifier.height(6.dp))
            Text(
                "Сделано $doneCount из ${cards.size}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Sections.of("roadmap").forEach { section ->
                val inSection = cards.filter { it.section == section.key }
                if (inSection.isNotEmpty()) {
                    item(key = "section_" + section.key) {
                        SectionTitle(section.glyph, section.title, action = "Комната →") {
                            onRoom(Rooms.topic(section.key))
                        }
                    }
                    items(inSection, key = { it.id }) { card ->
                        StepRow(
                            card = card,
                            checked = card.id in done,
                            onCheck = { vm.setStepDone(card.id, it) },
                            onOpen = { onCard(card.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepRow(card: InfoCard, checked: Boolean, onCheck: (Boolean) -> Unit, onOpen: () -> Unit) {
    DreamCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = onCheck)
            Spacer(Modifier.width(4.dp))
            Column(Modifier.weight(1f)) {
                Text(card.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                StatusChip(card.status, card.verifiedAt)
            }
        }
    }
}

/** Tab 2: what to do after landing in Korea. */
@Composable
fun GuideScreen(vm: DreamViewModel, onCard: (String) -> Unit, onRoom: (String) -> Unit) {
    val cards by vm.guide.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        DreamHeader(title = "После прилёта", subtitle = "Что сделать в Корее в первые недели")
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Sections.of("guide").forEach { section ->
                val inSection = cards.filter { it.section == section.key }
                if (inSection.isNotEmpty()) {
                    item(key = "section_" + section.key) {
                        SectionTitle(section.glyph, section.title, action = "Комната →") {
                            onRoom(Rooms.topic(section.key))
                        }
                    }
                    items(inSection, key = { it.id }) { card ->
                        DreamCard(onClick = { onCard(card.id) }) {
                            Text(card.title, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                card.body.substringBefore("\n"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                            Spacer(Modifier.height(8.dp))
                            StatusChip(card.status, card.verifiedAt)
                        }
                    }
                }
            }
        }
    }
}

/** One fact card in full: text, where it comes from, when it was checked. */
@Composable
fun CardScreen(vm: DreamViewModel, cardId: String, onBack: () -> Unit, onRoom: (String) -> Unit) {
    val card by remember(cardId) { vm.card(cardId) }.collectAsStateWithLifecycle(initialValue = null)
    val done by vm.doneIds.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val c = card

    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        DreamHeader(
            title = c?.title ?: "",
            subtitle = if (c == null) null else Sections.title(c.section),
            onBack = onBack
        )
        if (c != null) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusChip(c.status, c.verifiedAt)
                Text(c.body, style = MaterialTheme.typography.bodyLarge)

                DreamCard {
                    Text("Источник", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(c.source, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Последняя проверка", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        when (c.status) {
                            "verified" -> c.verifiedAt
                            "tip" -> "Это совет, а не факт"
                            else -> "Ещё не проверено. Сверь с источником перед тем, как действовать."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (c.sourceUrl.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                try {
                                    uriHandler.openUri(c.sourceUrl)
                                } catch (e: Exception) {
                                    // no browser on the device: nothing to open
                                }
                            },
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Открыть источник") }
                    }
                }

                if (c.kind == "roadmap") {
                    val isDone = c.id in done
                    Button(
                        onClick = { vm.setStepDone(c.id, !isDone) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text(if (isDone) "Отметить как не сделано" else "Отметить как сделано") }
                }
                OutlinedButton(
                    onClick = { onRoom(Rooms.topic(c.section)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Обсудить в комнате «${Sections.title(c.section)}»") }
            }
        }
    }
}
