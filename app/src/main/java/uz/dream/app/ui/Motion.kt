package uz.dream.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// Small, reusable pieces of motion. Every screen uses the same few so the app feels like one thing.

/**
 * Tap feedback: the element sinks a little while pressed and springs back.
 * Pass the element's shape so the ripple and the content are clipped to it.
 */
fun Modifier.bounceClick(enabled: Boolean = true, shape: Shape? = null, onClick: () -> Unit): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.965f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
        label = "press"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            if (shape != null) {
                this.shape = shape
                clip = true
            }
        }
        .clickable(
            interactionSource = source,
            indication = LocalIndication.current,
            enabled = enabled,
            onClick = onClick
        )
}

/** The moment a screen first appeared. Survives going to another screen and coming back. */
@Composable
fun rememberEntrance(): Long = rememberSaveable { System.currentTimeMillis() }

/**
 * Items fade and slide up one after another when a screen opens.
 * Items that appear later (scrolling, new data) are shown at once, so scrolling never feels slow.
 */
fun Modifier.enter(index: Int, since: Long): Modifier = composed {
    val skip = remember { System.currentTimeMillis() - since > 900 }
    val progress = remember { Animatable(if (skip) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!skip) {
            delay(index.coerceIn(0, 8) * 55L)
            progress.animateTo(1f, tween(durationMillis = 460, easing = FastOutSlowInEasing))
        }
    }
    graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 28.dp.toPx()
    }
}

/** Slow up-and-down drift, used for the floating hangul tiles. phase shifts tiles apart. */
@Composable
fun rememberFloat(periodMillis: Int = 3600, phase: Float = 0f): Float {
    val transition = rememberInfiniteTransition(label = "float")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "float-t"
    )
    return kotlin.math.sin(((t + phase) * 2f * Math.PI).toFloat())
}

/** 0 -> 1 -> 0 ... used for shimmer and pulsing highlights. */
@Composable
fun rememberPulse(periodMillis: Int = 1200): Float {
    val transition = rememberInfiniteTransition(label = "pulse")
    val v by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMillis, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse-v"
    )
    return v
}

/** Shakes sideways when key changes to a non-null value (used for login errors). */
fun Modifier.shakeOn(key: Any?): Modifier = composed {
    val offset = remember { Animatable(0f) }
    LaunchedEffect(key) {
        if (key != null) {
            for (x in listOf(14f, -12f, 9f, -6f, 3f, 0f)) {
                offset.animateTo(x, tween(durationMillis = 45))
            }
        }
    }
    graphicsLayer { translationX = offset.value * density }
}
