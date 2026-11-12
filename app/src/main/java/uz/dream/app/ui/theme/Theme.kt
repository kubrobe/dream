package uz.dream.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Dream brand: turquoise (primary) + saffron (accent)
val Turquoise = Color(0xFF12A8A0)
val TurquoiseDeep = Color(0xFF0B7F7A)
val TurquoiseSoft = Color(0xFFD3F1EE)
val Saffron = Color(0xFFF4B030)
val SaffronSoft = Color(0xFFFFEBC2)
val SaffronInk = Color(0xFF5A3D00)
val Ink = Color(0xFF0E2A2A)

private val DreamColors = lightColorScheme(
    primary = TurquoiseDeep, onPrimary = Color.White,
    primaryContainer = TurquoiseSoft, onPrimaryContainer = Color(0xFF05403D),
    secondary = Saffron, onSecondary = Ink,
    secondaryContainer = SaffronSoft, onSecondaryContainer = SaffronInk,
    background = Color(0xFFF3F8F7), onBackground = Ink,
    surface = Color.White, onSurface = Ink,
    surfaceVariant = Color(0xFFE4EEEC), onSurfaceVariant = Color(0xFF4B6260),
    outline = Color(0xFFB5C7C4), outlineVariant = Color(0xFFDCE7E5),
    surfaceContainer = Color.White, surfaceContainerHigh = Color.White,
    error = Color(0xFFC62828)
)

// Fonts. Dream's identity is Unbounded (headings) + Onest (body).
// To switch them on: put the .ttf files into app/src/main/res/font/ and replace these two lines with
//   val HeadingFont = FontFamily(Font(R.font.unbounded_bold, FontWeight.Bold))
//   val BodyFont = FontFamily(Font(R.font.onest_regular), Font(R.font.onest_semibold, FontWeight.SemiBold))
val HeadingFont: FontFamily = FontFamily.Default
val BodyFont: FontFamily = FontFamily.Default

private val base = Typography()
private val DreamTypography = Typography(
    headlineLarge = base.headlineLarge.copy(fontFamily = HeadingFont, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = HeadingFont, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = HeadingFont, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = HeadingFont, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontFamily = BodyFont),
    bodyMedium = base.bodyMedium.copy(fontFamily = BodyFont),
    bodySmall = base.bodySmall.copy(fontFamily = BodyFont),
    labelLarge = base.labelLarge.copy(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold),
    labelMedium = base.labelMedium.copy(fontFamily = BodyFont, fontWeight = FontWeight.Medium),
    labelSmall = base.labelSmall.copy(fontFamily = BodyFont, fontWeight = FontWeight.Medium)
)

private val DreamShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp)
)

@Composable
fun DreamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DreamColors,
        typography = DreamTypography,
        shapes = DreamShapes,
        content = content
    )
}
