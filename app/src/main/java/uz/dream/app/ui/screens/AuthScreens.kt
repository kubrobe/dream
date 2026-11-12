package uz.dream.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.dream.app.ui.DreamHeader
import uz.dream.app.ui.DreamViewModel
import uz.dream.app.ui.GlyphTile
import uz.dream.app.ui.Tag
import uz.dream.app.ui.theme.Saffron
import uz.dream.app.ui.theme.SaffronInk
import uz.dream.app.ui.theme.SaffronSoft

/** Shown while nobody is signed in: login, or registration. */
@Composable
fun AuthFlow(vm: DreamViewModel) {
    var showRegister by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showRegister) {
        vm.clearAuthMessage()
        showRegister = false
    }
    if (showRegister) {
        RegisterScreen(vm, onLoginClick = { vm.clearAuthMessage(); showRegister = false })
    } else {
        LoginScreen(vm, onRegisterClick = { vm.clearAuthMessage(); showRegister = true })
    }
}

@Composable
private fun LoginScreen(vm: DreamViewModel, onRegisterClick: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val busy by vm.busy.collectAsStateWithLifecycle()
    val message by vm.authMessage.collectAsStateWithLifecycle()

    AuthLayout(vm, title = "Dream", subtitle = "Учёба в Корее без платных консультантов") {
        Text("Вход", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = email, onValueChange = { email = it }, label = { Text("Email") },
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        OutlinedTextField(
            value = password, onValueChange = { password = it }, label = { Text("Пароль") },
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        AuthMessage(message)
        SubmitButton(text = "Войти", busy = busy) { vm.login(email, password) }
        TextButton(onClick = onRegisterClick, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Нет аккаунта? Зарегистрироваться")
        }
    }
}

@Composable
private fun RegisterScreen(vm: DreamViewModel, onLoginClick: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val busy by vm.busy.collectAsStateWithLifecycle()
    val message by vm.authMessage.collectAsStateWithLifecycle()

    AuthLayout(vm, title = "Dream", subtitle = "Создай аккаунт и начни свой путь в Корею") {
        Text("Регистрация", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = name, onValueChange = { name = it }, label = { Text("Имя") },
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)
        )
        OutlinedTextField(
            value = email, onValueChange = { email = it }, label = { Text("Email") },
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        OutlinedTextField(
            value = password, onValueChange = { password = it }, label = { Text("Пароль (минимум 6 символов)") },
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        AuthMessage(message)
        SubmitButton(text = "Создать аккаунт", busy = busy) { vm.register(name, email, password) }
        TextButton(onClick = onLoginClick, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Уже есть аккаунт? Войти")
        }
    }
}

@Composable
private fun AuthLayout(
    vm: DreamViewModel,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        DreamHeader(title = title, subtitle = subtitle) {
            Spacer(Modifier.height(16.dp))
            GlyphTile("꿈", size = 64.dp, color = Saffron)
            Spacer(Modifier.height(6.dp))
            Text("꿈 — «мечта» по-корейски", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!vm.online) {
                Tag("Локальный режим: сервер не подключён, данные только на этом телефоне", SaffronSoft, SaffronInk)
            }
            content()
        }
    }
}

@Composable
private fun AuthMessage(message: String?) {
    if (message != null) {
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SubmitButton(text: String, busy: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !busy,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (busy) {
            CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(text)
        }
    }
}
