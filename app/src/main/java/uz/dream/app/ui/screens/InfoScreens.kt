package uz.dream.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.dream.app.R
import uz.dream.app.data.InfoCard
import uz.dream.app.data.Rooms
import uz.dream.app.data.Sections
import uz.dream.app.data.body
import uz.dream.app.data.source
import uz.dream.app.data.title
import uz.dream.app.ui.DreamCard
import uz.dream.app.ui.DreamHeader
import uz.dream.app.ui.DreamViewModel
import uz.dream.app.ui.GlyphTile
import uz.dream.app.ui.LocalLang
import uz.dream.app.ui.Pill
import uz.dream.app.ui.PrimaryButton
import uz.dream.app.ui.ProgressRing
import uz.dream.app.ui.SecondaryButton
import uz.dream.app.ui.SectionTitle
import uz.dream.app.ui.SkeletonCards
import uz.dream.app.ui.StatusChip
import uz.dream.app.ui.bounceClick
import uz.dream.app.ui.enter
import uz.dream.app.ui.rememberEntrance
import uz.dream.app.ui.rememberPulse
import uz.dream.app.ui.theme.DreamTheme

/** Tab 1: the road from Uzbekistan to a Korean university, drawn as a timeline. */
@Composable
fun RoadmapScreen(vm: DreamViewModel, onCard: (String) -> Unit, onRoom: (String) -> Unit) {
    val cards by vm.roadmap.collectAsStateWithLifecycle()
    val done by vm.doneIds.collectAsStateWithLifecycle()
    val lang = LocalLang.current
    val since = rememberEntrance()
    val doneCount = cards.count { it.id in done }
    val fraction = if (cards.isEmpty()) 0f else doneCount.toFloat() / cards.size
    val next = cards.firstOrNull { it.id !in done }

    Column(Modifier.fillMaxSize()) {
        DreamHeader(title = stringResource(R.string.roadmap_title), subtitle = stringResource(R.string.roadmap_subtitle), glyph = "길") {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(fraction)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        stringResource(R.string.done_of, doneCount, cards.size),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        stringResource(
                            when {
                                fraction >= 1f -> R.string.roadmap_mood_done
                                fraction >= 0.5f -> R.string.roadmap_mood_half
                                doneCount > 0 -> R.string.roadmap_mood_started
                                else -> R.string.roadmap_mood_start
                            }
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                }
            }
        }
        if (cards.isEmpty()) {
            Box(Modifier.padding(16.dp)) { SkeletonCards() }
            return@Column
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
        ) {
            item(key = "next") {
                AnimatedContent(
                    targetState = next?.id,
                    transitionSpec = { (fadeIn(tween(300)) + scaleIn(initialScale = 0.94f)) togetherWith (fadeOut(tween(200)) + scaleOut(targetScale = 1.04f)) },
                    label = "next-step",
                    modifier = Modifier.enter(0, since)
                ) { nextId ->
                    val card = cards.firstOrNull { it.id == nextId }
                    if (card != null) NextStepCard(card.title(lang), Sections.glyph(card.section)) { onCard(card.id) }
                    else AllDoneCard()
                }
            }
            var index = 1
            Sections.of("roadmap").forEachIndexed { sectionIndex, section ->
                val inSection = cards.filter { it.section == section.key }
                if (inSection.isNotEmpty()) {
                    val i = index++
                    item(key = "section_" + section.key) {
                        Box(Modifier.enter(i, since)) {
                            SectionTitle(
                                section.glyph, stringResource(section.title), tone = sectionIndex,
                                action = stringResource(R.string.room_link)
                            ) { onRoom(Rooms.topic(section.key)) }
                        }
                    }
                    itemsIndexed(inSection, key = { _, c -> c.id }) { pos, card ->
                        val j = index++
                        TimelineStep(
                            card = card,
                            checked = card.id in done,
                            isFirst = pos == 0,
                            isLast = pos == inSection.lastIndex,
                            isNext = card.id == next?.id,
                            onCheck = { vm.setStepDone(card.id, it) },
                            onOpen = { onCard(card.id) },
                            modifier = Modifier
                                .enter(j, since)
                                .animateItem()
                        )
                    }
                }
            }
        }
    }
}

/** The first step that is not done yet, with a soft pulsing border. */
@Composable
private fun NextStepCard(title: String, glyph: String, onOpen: () -> Unit) {
    val pulse = rememberPulse(1400)
    val palette = DreamTheme.colors
    DreamCard(
        onClick = onOpen,
        background = palette.accentSoft,
        border = palette.accent.copy(alpha = 0.35f + 0.45f * pulse),
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlyphTile(glyph, size = 52.dp, tone = 1)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.next_step), style = MaterialTheme.typography.labelMedium, color = palette.onAccentSoft)
                Spacer(Modifier.height(2.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = palette.onAccentSoft)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = palette.onAccentSoft)
        }
    }
}

@Composable
private fun AllDoneCard() {
    val palette = DreamTheme.colors
    DreamCard(background = palette.brandSoft, border = palette.brandSoft) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlyphTile("축", size = 52.dp, tone = 0)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.all_done_title), style = MaterialTheme.typography.titleMedium, color = palette.onBrandSoft)
                Text(stringResource(R.string.all_done_text), style = MaterialTheme.typography.bodySmall, color = palette.onBrandSoft)
            }
        }
    }
}

/**
 * One step of the road: a node on a vertical line + the card.
 * Tapping the node ticks the step: the node fills with saffron and pops.
 */
@Composable
private fun TimelineStep(
    card: InfoCard,
    checked: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    isNext: Boolean,
    onCheck: (Boolean) -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalLang.current
    val haptic = LocalHapticFeedback.current
    val palette = DreamTheme.colors
    val primary = MaterialTheme.colorScheme.primary
    val lineDone = if (checked) primary else palette.track
    val nodeColor by animateColorAsState(if (checked) palette.accent else MaterialTheme.colorScheme.surface, tween(250), label = "node")
    val ringColor by animateColorAsState(
        when {
            checked -> palette.accent
            isNext -> primary
            else -> MaterialTheme.colorScheme.outline
        },
        tween(250), label = "node-ring"
    )
    val pop by animateFloatAsState(
        if (checked) 1f else 0.82f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMediumLow), label = "node-pop"
    )
    val track = palette.track

    Row(modifier.height(IntrinsicSize.Min)) {
        // the line and the node
        Box(
            Modifier
                .width(44.dp)
                .fillMaxHeight()
                .drawBehind {
                    val x = size.width / 2
                    val nodeY = 34.dp.toPx()
                    val w = 3.dp.toPx()
                    if (!isFirst) drawLine(track, Offset(x, 0f), Offset(x, nodeY), w, StrokeCap.Round)
                    if (!isLast) drawLine(lineDone, Offset(x, nodeY), Offset(x, size.height), w, StrokeCap.Round)
                },
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                Modifier
                    .padding(top = 20.dp)
                    .size(28.dp)
                    .graphicsLayer {
                        scaleX = pop
                        scaleY = pop
                    }
                    .bounceClick(shape = CircleShape) {
                        haptic.performHapticFeedback(if (checked) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn)
                        onCheck(!checked)
                    }
                    .background(nodeColor, CircleShape)
                    .border(2.5.dp, ringColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = checked,
                    transitionSpec = { scaleIn(spring(dampingRatio = 0.4f)) + fadeIn() togetherWith scaleOut() + fadeOut() },
                    label = "tick"
                ) { on ->
                    if (on) Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.mark_undone), tint = palette.onAccent, modifier = Modifier.size(17.dp))
                    else Box(Modifier.size(17.dp))
                }
            }
        }
        Box(Modifier.weight(1f).padding(vertical = 6.dp)) {
            DreamCard(onClick = onOpen, padding = 14.dp) {
                Text(
                    card.title(lang),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                StatusChip(card.status, card.verifiedAt)
            }
        }
    }
}

/** Tab 2: what to do after landing in Korea. Chips on top filter by topic. */
@Composable
fun GuideScreen(vm: DreamViewModel, onCard: (String) -> Unit, onRoom: (String) -> Unit) {
    val cards by vm.guide.collectAsStateWithLifecycle()
    val lang = LocalLang.current
    val since = rememberEntrance()
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    val sections = Sections.of("guide")

    Column(Modifier.fillMaxSize()) {
        DreamHeader(title = stringResource(R.string.guide_title), subtitle = stringResource(R.string.guide_subtitle), glyph = "삶")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "all") { Pill(stringResource(R.string.filter_all), selected = filter == null, onClick = { filter = null }) }
            items(sections, key = { it.key }) { s ->
                Pill(s.glyph + "  " + stringResource(s.title), selected = filter == s.key, onClick = { filter = s.key })
            }
        }
        if (cards.isEmpty()) {
            Box(Modifier.padding(horizontal = 16.dp)) { SkeletonCards() }
            return@Column
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            var index = 0
            sections.forEachIndexed { sectionIndex, section ->
                val inSection = cards.filter { it.section == section.key }
                if (inSection.isNotEmpty() && (filter == null || filter == section.key)) {
                    val i = index++
                    item(key = "section_" + section.key) {
                        Box(Modifier.enter(i, since).animateItem()) {
                            SectionTitle(
                                section.glyph, stringResource(section.title), tone = sectionIndex,
                                action = stringResource(R.string.room_link)
                            ) { onRoom(Rooms.topic(section.key)) }
                        }
                    }
                    items(inSection, key = { it.id }) { card ->
                        val j = index++
                        DreamCard(
                            onClick = { onCard(card.id) },
                            modifier = Modifier
                                .enter(j, since)
                                .animateItem()
                        ) {
                            Text(card.title(lang), style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                card.body(lang).substringBefore("\n"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusChip(card.status, card.verifiedAt)
                                Spacer(Modifier.weight(1f))
                                Text(
                                    stringResource(R.string.read_more),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
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
    val haptic = LocalHapticFeedback.current
    val lang = LocalLang.current
    val since = rememberEntrance()
    val c = card
    val palette = DreamTheme.colors

    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        DreamHeader(
            title = c?.title(lang) ?: "",
            subtitle = if (c == null) null else stringResource(Sections.title(c.section)),
            glyph = if (c == null) "꿈" else Sections.glyph(c.section),
            onBack = onBack
        )
        if (c != null) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(Modifier.enter(0, since)) { StatusChip(c.status, c.verifiedAt) }
                Text(c.body(lang), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.enter(1, since))

                DreamCard(modifier = Modifier.enter(2, since), background = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            if (c.status == "verified") Icons.Filled.CheckCircle else if (c.status == "tip") Icons.Filled.Star else Icons.Filled.Info,
                            contentDescription = null,
                            tint = if (c.status == "check") palette.accent else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.source), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(2.dp))
                            Text(c.source(lang), style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(10.dp))
                            Text(stringResource(R.string.last_checked), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                when (c.status) {
                                    "verified" -> c.verifiedAt
                                    "tip" -> stringResource(R.string.tip_not_fact)
                                    else -> stringResource(R.string.not_checked_yet)
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    if (c.sourceUrl.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        SecondaryButton(stringResource(R.string.open_source), onClick = {
                            try {
                                uriHandler.openUri(c.sourceUrl)
                            } catch (e: Exception) {
                                // no browser on the device: nothing to open
                            }
                        })
                    }
                }

                Column(Modifier.enter(3, since), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (c.kind == "roadmap") {
                        val isDone = c.id in done
                        PrimaryButton(
                            text = stringResource(if (isDone) R.string.step_done_undo else R.string.mark_done),
                            icon = if (isDone) Icons.Filled.CheckCircle else Icons.Filled.Check,
                            container = if (isDone) palette.accent else MaterialTheme.colorScheme.primary,
                            content = if (isDone) palette.onAccent else MaterialTheme.colorScheme.onPrimary,
                            onClick = {
                                haptic.performHapticFeedback(if (isDone) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn)
                                vm.setStepDone(c.id, !isDone)
                            }
                        )
                    }
                    SecondaryButton(
                        stringResource(R.string.discuss_in_room, stringResource(Sections.title(c.section))),
                        onClick = { onRoom(Rooms.topic(c.section)) }
                    )
                }
            }
        }
    }
}
