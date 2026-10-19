package br.com.rotacerta.colaborador.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.rotacerta.colaborador.ui.components.FormCard
import br.com.rotacerta.colaborador.ui.components.FormField
import br.com.rotacerta.colaborador.ui.components.RcButton
import br.com.rotacerta.colaborador.ui.components.RcTopBar
import br.com.rotacerta.colaborador.ui.theme.RcColors

@Composable
fun LoginScreen(viewModel: LoginViewModel, onForgotPassword: () -> Unit) {
    var showPassword by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(RcColors.Background)) {
        RcTopBar(
            navigation = null,
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("App do colaborador", color = RcColors.TextStrong, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Acompanhe o seu fretado, do ponto de embarque até a empresa.",
                        color = RcColors.TextMenu,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                IconButton(onClick = { showServerDialog = true }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Configurar servidor", tint = RcColors.TextMuted)
                }
            }

            FormCard(title = "Entrar", subtitle = "Use o e-mail corporativo e a senha recebidos da empresa.") {
                FormField(
                    label = "E-mail corporativo",
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    keyboardType = KeyboardType.Email,
                    leadingIcon = Icons.Filled.Email,
                    placeholder = "matricula@empresa.com",
                )
                FormField(
                    label = "Senha",
                    value = viewModel.password,
                    onValueChange = { viewModel.password = it },
                    password = !showPassword,
                    keyboardType = KeyboardType.Password,
                    leadingIcon = Icons.Filled.Lock,
                    trailing = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showPassword) "Ocultar senha" else "Mostrar senha",
                            )
                        }
                    },
                )
                viewModel.error?.let { Text(it, color = RcColors.MatWarn, fontSize = 14.sp) }
                RcButton(
                    text = "Entrar",
                    onClick = viewModel::submit,
                    enabled = viewModel.canSubmit,
                    loading = viewModel.loading,
                    icon = Icons.AutoMirrored.Filled.Login,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(onClick = onForgotPassword, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Esqueci minha senha", color = RcColors.MatPrimary, fontWeight = FontWeight.Medium)
                }
            }

            Text(
                "Seu e-mail é a sua matrícula + domínio da empresa. No primeiro acesso você deverá trocar a senha.",
                color = RcColors.TextMuted,
                fontSize = 13.sp,
            )
        }
    }

    if (showServerDialog) {
        ServerUrlDialog(
            current = viewModel.serverUrl,
            onDismiss = { showServerDialog = false },
            onSave = { viewModel.updateServerUrl(it); showServerDialog = false },
        )
    }
}

@Composable
private fun ServerUrlDialog(current: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var value by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RcColors.Panel,
        title = { Text("Servidor da API", color = RcColors.TextStrong) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Emulador: http://10.0.2.2:8000/api/ · Celular: use o IP do computador na mesma rede Wi-Fi.",
                    color = RcColors.TextLabel,
                    fontSize = 13.sp,
                )
                OutlinedTextField(value = value, onValueChange = { value = it }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onSave(value) }, enabled = value.startsWith("http")) { Text("Salvar", color = RcColors.Accent) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = RcColors.TextMenu) } },
    )
}
