package uz.dream.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.dream.app.ui.theme.Saffron
import uz.dream.app.ui.theme.SaffronInk
import uz.dream.app.ui.theme.SaffronSoft
import uz.dream.app.ui.theme.Turquoise
import uz.dream.app.ui.theme.TurquoiseDeep
import uz.dream.app.ui.theme.TurquoiseSoft
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Turquoise header at the top of every screen. It also covers the status bar area. */
@Composable
fun DreamHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    extra: @Composable (ColumnScope.() -> Unit)? = null
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(TurquoiseDeep, Turquoise)))
            .statusBarsPadding()
            .padding(start = if (onBack == null) 20.dp else 6.dp, end = 20.dp, top = 12.dp, bottom = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                }
            }
            Column(Modifier.padding(top = if (onBack == null) 0.dp else 8.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                if (subtitle != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.88f))
                }
                if (extra != null) extra()
            }
        }
    }
}

/** White rounded card. With onClick the whole card can be tapped. */
@Composable
fun DreamCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    var m = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(MaterialTheme.colorScheme.surface)
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
    if (onClick != null) m = m.clickable(onClick = onClick)
    Column(m.padding(16.dp), content = content)
}

/** Big hangul sign on a coloured tile: Dream's signature element. */
@Composable
fun GlyphTile(glyph: String, size: Dp = 56.dp, color: Color = TurquoiseDeep) {
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3.5f))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(glyph, color = Color.White, fontSize = (size.value * 0.5f).sp, fontWeight = FontWeight.Bold)
    }
}

/** "Проверено 2026-11-12" / "Нужна проверка" / "Совет Dream". Every fact card shows one. */
@Composable
fun StatusChip(status: String, verifiedAt: String) {
    val label: String
    val back: Color
    val front: Color
    when (status) {
        "verified" -> {
            label = "Проверено $verifiedAt"; back = TurquoiseSoft; front = TurquoiseDeep
        }
        "tip" -> {
            label = "Совет Dream"
            back = MaterialTheme.colorScheme.surfaceVariant
            front = MaterialTheme.colorScheme.onSurfaceVariant
        }
        else -> {
            label = "Нужна проверка"; back = SaffronSoft; front = SaffronInk
        }
    }
    Tag(label, back, front)
}

@Composable
fun Tag(text: String, back: Color, front: Color) {
    Text(
        text,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(back)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = front,
        style = MaterialTheme.typography.labelMedium
    )
}

/** A choice button that can be on or off (used for the study goal). */
@Composable
fun Pill(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Text(
        text,
        modifier = Modifier
            .clip(shape)
            .background(if (selected) TurquoiseDeep else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (selected) TurquoiseDeep else MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.labelLarge
    )
}

/** Saffron progress line. fraction is 0..1. */
@Composable
fun ProgressLine(fraction: Float, modifier: Modifier = Modifier, track: Color = Color.White.copy(alpha = 0.3f)) {
    Box(
        modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(track)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(Saffron)
        )
    }
}

/** Title of a group of cards with its hangul tile and an optional "room" link on the right. */
@Composable
fun SectionTitle(glyph: String, title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            GlyphTile(glyph, size = 36.dp, color = Turquoise)
            Spacer(Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
        if (action != null && onAction != null) {
            Text(
                action,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                color = TurquoiseDeep,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/** 349000 -> "349 000 сум" */
fun uzs(value: Long): String = String.format(Locale.US, "%,d", value).replace(',', ' ') + " сум"

private val clock = DateTimeFormatter.ofPattern("dd.MM HH:mm")

/** epoch millis -> "12.11 21:40" in the phone's time zone */
fun shortTime(millis: Long): String = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(clock)
