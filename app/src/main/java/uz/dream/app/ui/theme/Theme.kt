package uz.dream.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.dream.app.R

// Dream brand: turquoise (primary) + saffron (accent)
val Turquoise = Color(0xFF12A8A0)
val TurquoiseDeep = Color(0xFF0B7F7A)
val TurquoiseSoft = Color(0xFFD3F1EE)
val Saffron = Color(0xFFF4B030)
val SaffronSoft = Color(0xFFFFEBC2)
val SaffronInk = Color(0xFF5A3D00)
val Ink = Color(0xFF0E2A2A)
val Coral = Color(0xFFE5674F)

/** Unbounded: wide, confident headings. Onest: calm, readable body text. Both cover Latin (Oʻzbek) and Cyrillic. */
val HeadingFont = FontFamily(
    Font(R.font.unbounded_600, FontWeight.SemiBold),
    Font(R.font.unbounded_700, FontWeight.Bold),
    Font(R.font.unbounded_800, FontWeight.ExtraBold)
)
val BodyFont = FontFamily(
    Font(R.font.onest_400, FontWeight.Normal),
    Font(R.font.onest_500, FontWeight.Medium),
    Font(R.font.onest_600, FontWeight.SemiBold),
    Font(R.font.onest_700, FontWeight.Bold)
)

/** Colours Material does not have a slot for. Read them with DreamTheme.colors. */
@Immutable
data class DreamPalette(
    val isDark: Boolean,
    val headerStart: Color,
    val headerEnd: Color,
    val accent: Color,          // saffron
    val onAccent: Color,
    val accentSoft: Color,
    val onAccentSoft: Color,
    val brandSoft: Color,       // pale turquoise
    val onBrandSoft: Color,
    val cardBorder: Color,
    val track: Color,           // empty part of progress bars and timeline lines
    val tiles: List<Pair<Color, Color>>   // gradients for hangul tiles
) {
    val headerBrush: Brush get() = Brush.linearGradient(listOf(headerStart, headerEnd))
    fun tileBrush(index: Int): Brush {
        val (a, b) = tiles[((index % tiles.size) + tiles.size) % tiles.size]
        return Brush.linearGradient(listOf(a, b))
    }
}

private val LightPalette = DreamPalette(
    isDark = false,
    headerStart = Color(0xFF075F5B),
    headerEnd = Color(0xFF14B3A9),
    accent = Saffron, onAccent = Ink,
    accentSoft = SaffronSoft, onAccentSoft = SaffronInk,
    brandSoft = TurquoiseSoft, onBrandSoft = Color(0xFF05504C),
    cardBorder = Color(0xFFDCE7E5),
    track = Color(0xFFDCE7E5),
    tiles = listOf(
        Color(0xFF0B7F7A) to Color(0xFF16B5AB),
        Color(0xFFE59A12) to Color(0xFFF6C453),
        Color(0xFF173B3B) to Color(0xFF2F6463),
        Color(0xFFD9573F) to Color(0xFFF08C62)
    )
)

private val DarkPalette = DreamPalette(
    isDark = true,
    headerStart = Color(0xFF053D3B),
    headerEnd = Color(0xFF0E8A82),
    accent = Color(0xFFF6BE4F), onAccent = Color(0xFF241800),
    accentSoft = Color(0xFF3D2C07), onAccentSoft = Color(0xFFFFDDA0),
    brandSoft = Color(0xFF113F3C), onBrandSoft = Color(0xFFB4EEE7),
    cardBorder = Color(0xFF1F3A39),
    track = Color(0xFF244241),
    tiles = listOf(
        Color(0xFF0A6F6A) to Color(0xFF15A79E),
        Color(0xFFC98309) to Color(0xFFF0B640),
        Color(0xFF24504F) to Color(0xFF3D7A78),
        Color(0xFFB84A35) to Color(0xFFE07A55)
    )
)

private val LightColors = lightColorScheme(
    primary = TurquoiseDeep, onPrimary = Color.White,
    primaryContainer = TurquoiseSoft, onPrimaryContainer = Color(0xFF05403D),
    secondary = Saffron, onSecondary = Ink,
    secondaryContainer = SaffronSoft, onSecondaryContainer = SaffronInk,
    tertiary = Coral, onTertiary = Color.White,
    background = Color(0xFFF3F8F7), onBackground = Ink,
    surface = Color.White, onSurface = Ink,
    surfaceVariant = Color(0xFFE8F1EF), onSurfaceVariant = Color(0xFF4B6260),
    outline = Color(0xFFB5C7C4), outlineVariant = Color(0xFFDCE7E5),
    surfaceContainer = Color.White, surfaceContainerHigh = Color.White, surfaceContainerHighest = Color(0xFFF0F6F5),
    inverseSurface = Ink, inverseOnSurface = Color.White,
    error = Color(0xFFC62828)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4FD6C9), onPrimary = Color(0xFF00332F),
    primaryContainer = Color(0xFF0F4C49), onPrimaryContainer = Color(0xFFBFF1EB),
    secondary = Color(0xFFF6BE4F), onSecondary = Color(0xFF241800),
    secondaryContainer = Color(0xFF3D2C07), onSecondaryContainer = Color(0xFFFFDDA0),
    tertiary = Color(0xFFFF8A70), onTertiary = Color(0xFF3A0B00),
    background = Color(0xFF081716), onBackground = Color(0xFFE2F1EF),
    surface = Color(0xFF0F2322), onSurface = Color(0xFFE2F1EF),
    surfaceVariant = Color(0xFF173130), onSurfaceVariant = Color(0xFF9DB9B6),
    outline = Color(0xFF3F5E5C), outlineVariant = Color(0xFF1F3A39),
    surfaceContainer = Color(0xFF0F2322), surfaceContainerHigh = Color(0xFF142A29), surfaceContainerHighest = Color(0xFF1A3332),
    inverseSurface = Color(0xFFE2F1EF), inverseOnSurface = Color(0xFF0F2322),
    error = Color(0xFFFF8A80)
)

private fun heading(size: Int, line: Int, weight: FontWeight = FontWeight.Bold) = TextStyle(
    fontFamily = HeadingFont, fontWeight = weight, fontSize = size.sp, lineHeight = line.sp, letterSpacing = (-0.3).sp
)

private fun body(size: Int, line: Int, weight: FontWeight = FontWeight.Normal, spacing: Float = 0f) = TextStyle(
    fontFamily = BodyFont, fontWeight = weight, fontSize = size.sp, lineHeight = line.sp, letterSpacing = spacing.sp
)

private val DreamTypography = Typography(
    displaySmall = heading(32, 38, FontWeight.ExtraBold),
    headlineLarge = heading(28, 34, FontWeight.ExtraBold),
    headlineMedium = heading(24, 30),
    headlineSmall = heading(21, 27),
    titleLarge = heading(18, 24),
    titleMedium = body(16, 22, FontWeight.SemiBold),
    titleSmall = body(14, 20, FontWeight.SemiBold),
    bodyLarge = body(16, 25),
    bodyMedium = body(14, 21),
    bodySmall = body(12, 17),
    labelLarge = body(14, 20, FontWeight.SemiBold, 0.1f),
    labelMedium = body(12, 16, FontWeight.Medium, 0.2f),
    labelSmall = body(11, 14, FontWeight.Medium, 0.3f)
)

private val DreamShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val LocalDreamPalette = staticCompositionLocalOf { LightPalette }

@Composable
fun DreamTheme(dark: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDreamPalette provides if (dark) DarkPalette else LightPalette) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = DreamTypography,
            shapes = DreamShapes,
            content = content
        )
    }
}

object DreamTheme {
    val colors: DreamPalette
        @Composable get() = LocalDreamPalette.current
}
