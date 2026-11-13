package uz.dream.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.dream.app.R
import uz.dream.app.ui.DreamViewModel
import uz.dream.app.ui.GlyphTile
import uz.dream.app.ui.LanguageSwitch
import uz.dream.app.ui.PrimaryButton
import uz.dream.app.ui.Tag
import uz.dream.app.ui.rememberFloat
import uz.dream.app.ui.shakeOn
import uz.dream.app.ui.theme.DreamTheme

/** Shown while nobody is signed in: login or registration, switching with a slide. */
@Composable
fun AuthFlow(vm: DreamViewModel) {
    var showRegister by rememberSaveable { mutableStateOf(false) }
    val lang by vm.lang.collectAsStateWithLifecycle()
    BackHandler(enabled = showRegister) {
        vm.clearAuthMessage()
        showRegister = false
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        Hero(lang = lang, onLang = vm::setLang)
        // the form sheet slides up over the hero
        Column(
            Modifier
                .offset(y = (-30).dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .padding(horizontal = 22.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!vm.online) {
                Tag(
                    stringResource(R.string.local_mode_long),
                    DreamTheme.colors.accentSoft,
                    DreamTheme.colors.onAccentSoft,
                    dot = DreamTheme.colors.accent
                )
            }
            AnimatedContent(
                targetState = showRegister,
                transitionSpec = {
                    val dir = if (targetState) 1 else -1
                    (slideInHorizontally(tween(360, easing = FastOutSlowInEasing)) { dir * it / 2 } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { -dir * it / 2 } + fadeOut(tween(200)))
                },
                label = "login-register"
            ) { register ->
                if (register) {
                    RegisterForm(vm, onLoginClick = { vm.clearAuthMessage(); showRegister = false })
                } else {
                    LoginForm(vm, onRegisterClick = { vm.clearAuthMessage(); showRegister = true })
                }
            }
        }
    }
}

/** Gradient top with the name, a tagline and four hangul tiles drifting at their own pace. */
@Composable
private fun Hero(lang: uz.dream.app.data.Lang, onLang: (uz.dream.app.data.Lang) -> Unit) {
    // the hero fades and rises in once
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }

    Box(
        Modifier
            .fillMaxWidth()
            .background(DreamTheme.colors.headerBrush)
            .statusBarsPadding()
            .padding(bottom = 46.dp)
    ) {
        FloatingTile("한", 46.dp, tone = 2, x = 246.dp, y = 62.dp, phase = 0.1f, alpha = 0.55f)
        FloatingTile("길", 38.dp, tone = 0, x = 300.dp, y = 126.dp, phase = 0.55f, alpha = 0.5f)
        FloatingTile("학", 34.dp, tone = 3, x = 236.dp, y = 168.dp, phase = 0.8f, alpha = 0.45f)

        Column(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                LanguageSwitch(current = lang, onPick = onLang, onDark = true, modifier = Modifier.fillMaxWidth(0.52f))
            }
            Spacer(Modifier.height(26.dp))
            Column(
                Modifier.graphicsLayer {
                    alpha = appear.value
                    translationY = (1f - appear.value) * 40.dp.toPx()
                }
            ) {
                val drift = rememberFloat(4000)
                GlyphTile(
                    "꿈", size = 76.dp, tone = 1,
                    modifier = Modifier.graphicsLayer {
                        translationY = drift * 5.dp.toPx()
                        rotationZ = drift * 3f
                    }
                )
                Spacer(Modifier.height(18.dp))
                Text("Dream", style = MaterialTheme.typography.displaySmall, color = Color.White)
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.app_tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.fillMaxWidth(0.72f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.dream_meaning),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun FloatingTile(glyph: String, size: Dp, tone: Int, x: Dp, y: Dp, phase: Float, alpha: Float) {
    val drift = rememberFloat(periodMillis = 4600, phase = phase)
    GlyphTile(
        glyph, size = size, tone = tone,
        modifier = Modifier
            .offset(x = x, y = y)
            .graphicsLayer {
                this.alpha = alpha
                translationY = drift * 8.dp.toPx()
                rotationZ = drift * 6f
            }
    )
}

@Composable
private fun LoginForm(vm: DreamViewModel, onRegisterClick: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val busy by vm.busy.collectAsStateWithLifecycle()
    val message by vm.authMessage.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.shakeOn(message)) {
        Text(stringResource(R.string.auth_login_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(R.string.auth_login_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Field(email, { email = it }, stringResource(R.string.field_email), Icons.Filled.Email, KeyboardType.Email)
        Field(password, { password = it }, stringResource(R.string.field_password), Icons.Filled.Lock, KeyboardType.Password, secret = true, last = true)
        AuthMessage(message)
        Spacer(Modifier.height(2.dp))
        PrimaryButton(stringResource(R.string.auth_login_button), onClick = { vm.login(email, password) }, busy = busy)
        TextButton(onClick = onRegisterClick, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(stringResource(R.string.auth_to_register))
        }
    }
}

@Composable
private fun RegisterForm(vm: DreamViewModel, onLoginClick: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val busy by vm.busy.collectAsStateWithLifecycle()
    val message by vm.authMessage.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.shakeOn(message)) {
        Text(stringResource(R.string.auth_register_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(R.string.auth_register_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Field(name, { name = it }, stringResource(R.string.field_name), Icons.Filled.Person, KeyboardType.Text)
        Field(email, { email = it }, stringResource(R.string.field_email), Icons.Filled.Email, KeyboardType.Email)
        Field(password, { password = it }, stringResource(R.string.field_password_new), Icons.Filled.Lock, KeyboardType.Password, secret = true, last = true)
        PasswordMeter(password)
        AuthMessage(message)
        Spacer(Modifier.height(2.dp))
        PrimaryButton(stringResource(R.string.auth_register_button), onClick = { vm.register(name, email, password) }, busy = busy)
        TextButton(onClick = onLoginClick, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(stringResource(R.string.auth_to_login))
        }
    }
}

@Composable
private fun Field(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboard: KeyboardType,
    secret: Boolean = false,
    last: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard, imeAction = if (last) ImeAction.Done else ImeAction.Next)
    )
}

/** Three bars that fill up as the password gets longer. */
@Composable
private fun PasswordMeter(password: String) {
    AnimatedVisibility(visible = password.isNotEmpty(), enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
        val level = when {
            password.length >= 10 -> 3
            password.length >= 6 -> 2
            else -> 1
        }
        val colors = listOf(MaterialTheme.colorScheme.error, DreamTheme.colors.accent, MaterialTheme.colorScheme.primary)
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { i ->
                    uz.dream.app.ui.ProgressLine(
                        fraction = if (i < level) 1f else 0f,
                        modifier = Modifier.weight(1f),
                        track = MaterialTheme.colorScheme.surfaceVariant,
                        fill = colors[level - 1],
                        height = 5.dp
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(
                    when (level) {
                        1 -> R.string.password_weak
                        2 -> R.string.password_ok
                        else -> R.string.password_strong
                    }
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ColumnScope.AuthMessage(message: uz.dream.app.data.UiText?) {
    val context = LocalContext.current
    AnimatedVisibility(visible = message != null, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
        Text(
            message?.resolve(context).orEmpty(),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        )
    }
}
