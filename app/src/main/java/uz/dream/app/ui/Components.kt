package uz.dream.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import uz.dream.app.R
import uz.dream.app.data.Lang
import uz.dream.app.ui.theme.DreamTheme
import uz.dream.app.ui.theme.HeadingFont

/**
 * Gradient header at the top of every screen. It also covers the status bar.
 * A big hangul sign floats in the corner as a watermark: Dream's signature.
 */
@Composable
fun DreamHeader(
    title: String,
    subtitle: String? = null,
    glyph: String = "꿈",
    onBack: (() -> Unit)? = null,
    extra: @Composable (ColumnScope.() -> Unit)? = null
) {
    val drift = rememberFloat(periodMillis = 7000)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            .background(DreamTheme.colors.headerBrush)
    ) {
        Text(
            glyph,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 28.dp, y = (6 + drift * 6).dp)
                .rotate(-8f + drift * 2f)
                .alpha(0.13f),
            color = Color.White,
            fontSize = 150.sp,
            fontWeight = FontWeight.Black
        )
        Column(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = if (onBack == null) 22.dp else 10.dp, end = 22.dp, top = 10.dp, bottom = 22.dp)
        ) {
            if (onBack != null) {
                Box(
                    Modifier
                        .size(42.dp)
                        .bounceClick(shape = CircleShape, onClick = onBack)
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
                }
                Spacer(Modifier.height(10.dp))
            } else {
                Spacer(Modifier.height(8.dp))
            }
            Column(Modifier.padding(start = if (onBack == null) 0.dp else 12.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.86f))
                }
                if (extra != null) extra()
            }
        }
    }
}

/** Rounded card. With onClick the whole card springs when tapped. */
@Composable
fun DreamCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    background: Color = MaterialTheme.colorScheme.surface,
    border: Color = DreamTheme.colors.cardBorder,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    var m = modifier.fillMaxWidth()
    if (onClick != null) m = m.bounceClick(shape = shape, onClick = onClick)
    m = m
        .clip(shape)
        .background(background)
        .border(1.dp, border, shape)
    Column(m.padding(padding), content = content)
}

/** Big hangul sign on a gradient tile. tone picks one of four brand gradients. */
@Composable
fun GlyphTile(glyph: String, size: Dp = 56.dp, tone: Int = 0, modifier: Modifier = Modifier) {
    val corner = size / 3.4f
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(DreamTheme.colors.tileBrush(tone)),
        contentAlignment = Alignment.Center
    ) {
        // soft light in the top-left corner gives the tile some depth
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    // diagonal: light in the top-left corner, gone by the middle
                    Brush.linearGradient(listOf(Color.White.copy(alpha = 0.26f), Color.Transparent, Color.Transparent))
                )
        )
        Text(glyph, color = Color.White, fontSize = (size.value * 0.48f).sp, fontWeight = FontWeight.Bold)
    }
}

/** "Checked 2026-11-12" / "Needs checking" / "Dream tip". Every fact card shows one. */
@Composable
fun StatusChip(status: String, verifiedAt: String) {
    val p = DreamTheme.colors
    when (status) {
        "verified" -> Tag(stringResource(R.string.status_verified, verifiedAt), p.brandSoft, p.onBrandSoft, dot = MaterialTheme.colorScheme.primary)
        "tip" -> Tag(
            stringResource(R.string.status_tip),
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            dot = MaterialTheme.colorScheme.onSurfaceVariant
        )
        else -> Tag(stringResource(R.string.status_check), p.accentSoft, p.onAccentSoft, dot = p.accent)
    }
}

/** Small rounded label, optionally with a coloured dot in front. */
@Composable
fun Tag(text: String, back: Color, front: Color, modifier: Modifier = Modifier, dot: Color? = null) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(back)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (dot != null) {
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dot)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(text, color = front, style = MaterialTheme.typography.labelMedium)
    }
}

/** A choice chip that can be on or off. Colours slide between the two states. */
@Composable
fun Pill(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(50)
    val back by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface, tween(220), label = "pill-bg"
    )
    val front by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, tween(220), label = "pill-fg"
    )
    val line by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, tween(220), label = "pill-line"
    )
    Text(
        text,
        modifier = modifier
            .bounceClick(shape = shape, onClick = onClick)
            .background(back)
            .border(1.dp, line, shape)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        color = front,
        style = MaterialTheme.typography.labelLarge,
        maxLines = 1
    )
}

/** Saffron progress line that grows smoothly. fraction is 0..1. */
@Composable
fun ProgressLine(
    fraction: Float,
    modifier: Modifier = Modifier,
    track: Color = Color.White.copy(alpha = 0.25f),
    fill: Color = DreamTheme.colors.accent,
    height: Dp = 8.dp
) {
    val animated by animateFloatAsState(
        fraction.coerceIn(0f, 1f), spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow), label = "progress"
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(track)
    ) {
        Box(
            Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(fill)
        )
    }
}

/** Round progress with the percent counting up in the middle. */
@Composable
fun ProgressRing(
    fraction: Float,
    size: Dp = 76.dp,
    stroke: Dp = 8.dp,
    track: Color = Color.White.copy(alpha = 0.22f),
    fill: Color = DreamTheme.colors.accent,
    textColor: Color = Color.White
) {
    val animated by animateFloatAsState(
        fraction.coerceIn(0f, 1f), spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessVeryLow), label = "ring"
    )
    val percent by animateIntAsState((fraction.coerceIn(0f, 1f) * 100).toInt(), tween(900, easing = FastOutSlowInEasing), label = "ring-pct")
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = stroke.toPx()
            drawArc(track, startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(w))
            drawArc(fill, startAngle = -90f, sweepAngle = 360f * animated, useCenter = false, style = Stroke(w, cap = StrokeCap.Round))
        }
        Text("$percent%", color = textColor, fontFamily = HeadingFont, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.22f).sp)
    }
}

/** A number that counts up to its new value. */
@Composable
fun CountingText(value: Int, color: Color = MaterialTheme.colorScheme.onSurface, sizeSp: Int = 26) {
    val shown by animateIntAsState(value, tween(700, easing = FastOutSlowInEasing), label = "count")
    Text(shown.toString(), color = color, fontFamily = HeadingFont, fontWeight = FontWeight.Bold, fontSize = sizeSp.sp)
}

/** Title of a group of cards with its hangul tile and an optional "room" link on the right. */
@Composable
fun SectionTitle(glyph: String, title: String, tone: Int = 0, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlyphTile(glyph, size = 36.dp, tone = tone)
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            Text(
                action,
                modifier = Modifier
                    .bounceClick(shape = RoundedCornerShape(50), onClick = onAction)
                    .background(DreamTheme.colors.brandSoft)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                color = DreamTheme.colors.onBrandSoft,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/** Main button. While busy the label turns into a spinner. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    busy: Boolean = false,
    icon: ImageVector? = null,
    container: Color = MaterialTheme.colorScheme.primary,
    content: Color = MaterialTheme.colorScheme.onPrimary
) {
    Button(
        onClick = onClick,
        enabled = enabled && !busy,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content)
    ) {
        AnimatedContent(
            targetState = busy,
            transitionSpec = { (fadeIn(tween(180)) + scaleIn(initialScale = 0.8f)) togetherWith fadeOut(tween(120)) },
            label = "button-busy"
        ) { isBusy ->
            if (isBusy) {
                CircularProgressIndicator(Modifier.size(22.dp), color = content, strokeWidth = 2.5.dp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(text, style = MaterialTheme.typography.labelLarge, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * Segmented control with a sliding highlight, e.g. UZ | RU | EN.
 * onDark = true draws it for a gradient header.
 */
@Composable
fun <T> Segmented(
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    onDark: Boolean = false,
    height: Dp = 44.dp
) {
    val index = options.indexOf(selected).coerceAtLeast(0)
    val track = if (onDark) Color.White.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant
    val thumb = if (onDark) Color.White else MaterialTheme.colorScheme.surface
    val on = if (onDark) DreamTheme.colors.headerStart else MaterialTheme.colorScheme.primary
    val off = if (onDark) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
    BoxWithConstraints(
        modifier
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(track)
            .padding(4.dp)
    ) {
        val itemWidth = maxWidth / options.size
        val x by animateDpAsState(itemWidth * index, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow), label = "seg")
        Box(
            Modifier
                .offset(x = x)
                .width(itemWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(thumb)
        )
        Row(Modifier.fillMaxSize()) {
            options.forEachIndexed { i, option ->
                val color by animateColorAsState(if (i == index) on else off, tween(200), label = "seg-text")
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .bounceClick(shape = RoundedCornerShape(50)) { onSelect(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(label(option), color = color, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                }
            }
        }
    }
}

/** UZ | RU | EN switch. */
@Composable
fun LanguageSwitch(current: Lang, onPick: (Lang) -> Unit, modifier: Modifier = Modifier, onDark: Boolean = false, full: Boolean = false) {
    Segmented(
        options = Lang.entries,
        selected = current,
        label = { if (full) it.nativeName else it.short },
        onSelect = onPick,
        modifier = modifier,
        onDark = onDark,
        height = if (full) 46.dp else 38.dp
    )
}

/** Grey placeholder blocks with a light sweeping across, shown while content loads. */
@Composable
fun SkeletonCards(count: Int = 4) {
    val pulse = rememberPulse(900)
    val base = MaterialTheme.colorScheme.surfaceVariant
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(count) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(base.copy(alpha = 0.6f + 0.4f * pulse))
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.75f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(50))
                            .background(base.copy(alpha = 0.6f + 0.4f * pulse))
                    )
                    Box(
                        Modifier
                            .fillMaxWidth(0.45f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(base.copy(alpha = 0.5f + 0.4f * (1f - pulse)))
                    )
                }
            }
        }
    }
}

/** A friendly empty screen: a floating tile and one line of text. */
@Composable
fun EmptyState(glyph: String, text: String, modifier: Modifier = Modifier) {
    val drift = rememberFloat(4200)
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlyphTile(glyph, size = 72.dp, tone = 1, modifier = Modifier.graphicsLayer { translationY = drift * 6.dp.toPx() })
        Spacer(Modifier.height(16.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

/** Circle with initials, e.g. "NK" for Najmiddin Kurganov. */
@Composable
fun Avatar(name: String, size: Dp = 40.dp, tone: Int = 0) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(DreamTheme.colors.tileBrush(tone)),
        contentAlignment = Alignment.Center
    ) {
        Text(initials(name), color = Color.White, fontFamily = HeadingFont, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.36f).sp)
    }
}

fun initials(name: String): String {
    val parts = name.trim().split(" ", "-").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}

/** A stable colour for a name, so the same person always gets the same avatar. */
fun toneOf(key: String): Int = (key.hashCode() and 0x7fffffff) % 4

/** 349000 -> "349 000". The currency word comes from strings.xml (so‘m / сум / UZS). */
fun formatSum(value: Long): String = String.format(Locale.US, "%,d", value).replace(',', ' ')

/** "349 000 so‘m" in the current language. */
@Composable
fun money(value: Long): String = stringResource(R.string.money, formatSum(value))

private val clock = DateTimeFormatter.ofPattern("dd.MM HH:mm")

/** epoch millis -> "12.11 21:40" in the phone's time zone */
fun shortTime(millis: Long): String = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(clock)
