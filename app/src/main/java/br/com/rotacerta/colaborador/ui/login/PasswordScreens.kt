package br.com.rotacerta.colaborador.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.rotacerta.colaborador.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    viewModel: ChangePasswordViewModel,
    forced: Boolean,
    onBack: (() -> Unit)? = null,
    onDone: () -> Unit = {},
) {
    LaunchedEffect(viewModel.success) { if (viewModel.success) onDone() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (forced) "Primeiro acesso" else "Alterar senha") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).imePadding().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (forced) {
                Text(
                    "Por segurança, defina uma nova senha antes de continuar.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            PasswordField("Senha atual", viewModel.current) { viewModel.current = it }
            PasswordField("Nova senha", viewModel.new) { viewModel.new = it }
            PasswordField("Confirmar nova senha", viewModel.confirm) { viewModel.confirm = it }
            Text(
                "Mínimo de 8 caracteres, evitando senhas comuns ou só com números.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            viewModel.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            PrimaryButton("Salvar nova senha", viewModel.loading, viewModel::submit)
            if (forced) {
                TextButton(onClick = viewModel::logout, modifier = Modifier.fillMaxWidth()) { Text("Sair") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(viewModel: ForgotPasswordViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar senha") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).imePadding().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when (viewModel.step) {
                ForgotPasswordViewModel.Step.REQUEST -> {
                    Text("Informe seu e-mail corporativo para receber um código de verificação.")
                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = { viewModel.email = it },
                        label = { Text("E-mail corporativo") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    viewModel.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    PrimaryButton("Enviar código", viewModel.loading, viewModel::requestCode)
                }
                ForgotPasswordViewModel.Step.RESET -> {
                    viewModel.devCode?.let {
                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(12.dp)) {
                            Text(
                                "Ambiente de testes: seu código é $it",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    Text("Digite o código recebido e escolha uma nova senha.")
                    OutlinedTextField(
                        value = viewModel.code,
                        onValueChange = { viewModel.code = it.filter(Char::isDigit) },
                        label = { Text("Código de verificação") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    PasswordField("Nova senha", viewModel.newPassword) { viewModel.newPassword = it }
                    viewModel.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    PrimaryButton("Redefinir senha", viewModel.loading, viewModel::reset)
                }
                ForgotPasswordViewModel.Step.DONE -> {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(56.dp))
                    Text("Senha redefinida!", style = MaterialTheme.typography.titleLarge)
                    Text("Agora é só entrar com a nova senha.")
                    Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Voltar ao login") }
                }
            }
        }
    }
}

@Composable
private fun PasswordField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PrimaryButton(text: String, loading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
        } else {
            Text(text)
        }
    }
}
