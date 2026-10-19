package br.com.rotacerta.colaborador.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
private fun FormScaffold(title: String, onBack: (() -> Unit)?, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(RcColors.Background)) {
        RcTopBar(
            title = title,
            navigation = onBack?.let {
                {
                    IconButton(onClick = it) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = RcColors.White)
                    }
                }
            },
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) { content() }
    }
}

@Composable
fun ChangePasswordScreen(
    viewModel: ChangePasswordViewModel,
    forced: Boolean,
    onBack: (() -> Unit)? = null,
    onDone: () -> Unit = {},
) {
    LaunchedEffect(viewModel.success) { if (viewModel.success) onDone() }
    FormScaffold(if (forced) "Primeiro acesso" else "Alterar senha", onBack) {
        FormCard(
            title = if (forced) "Defina sua senha" else "Alterar senha",
            subtitle = if (forced) "Por segurança, defina uma nova senha antes de continuar." else null,
        ) {
            FormField("Senha atual", viewModel.current, { viewModel.current = it }, password = true)
            FormField("Nova senha", viewModel.new, { viewModel.new = it }, password = true)
            FormField("Confirmar nova senha", viewModel.confirm, { viewModel.confirm = it }, password = true)
            Text(
                "Mínimo de 8 caracteres, evitando senhas comuns ou só com números.",
                color = RcColors.CardText,
                fontSize = 13.sp,
            )
            viewModel.error?.let { Text(it, color = RcColors.MatWarn, fontSize = 14.sp) }
            RcButton("Salvar", viewModel::submit, loading = viewModel.loading, modifier = Modifier.fillMaxWidth())
            if (forced) {
                TextButton(onClick = viewModel::logout, modifier = Modifier.fillMaxWidth()) {
                    Text("Sair", color = RcColors.MatWarn, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(viewModel: ForgotPasswordViewModel, onBack: () -> Unit) {
    FormScaffold("Recuperar senha", onBack) {
        when (viewModel.step) {
            ForgotPasswordViewModel.Step.REQUEST -> FormCard(
                title = "Recuperar senha",
                subtitle = "Informe seu e-mail corporativo para receber um código de verificação.",
            ) {
                FormField("E-mail corporativo", viewModel.email, { viewModel.email = it }, keyboardType = KeyboardType.Email)
                viewModel.error?.let { Text(it, color = RcColors.MatWarn, fontSize = 14.sp) }
                RcButton("Enviar código", viewModel::requestCode, loading = viewModel.loading, modifier = Modifier.fillMaxWidth())
            }
            ForgotPasswordViewModel.Step.RESET -> FormCard(
                title = "Nova senha",
                subtitle = "Digite o código recebido e escolha uma nova senha.",
            ) {
                viewModel.devCode?.let {
                    Text(
                        "Ambiente de testes: seu código é $it",
                        color = RcColors.Topbar,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                            .background(RcColors.TopbarText, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                    )
                }
                FormField(
                    "Código de verificação",
                    viewModel.code,
                    { viewModel.code = it.filter(Char::isDigit) },
                    keyboardType = KeyboardType.Number,
                )
                FormField("Nova senha", viewModel.newPassword, { viewModel.newPassword = it }, password = true)
                viewModel.error?.let { Text(it, color = RcColors.MatWarn, fontSize = 14.sp) }
                RcButton("Redefinir senha", viewModel::reset, loading = viewModel.loading, modifier = Modifier.fillMaxWidth())
            }
            ForgotPasswordViewModel.Step.DONE -> FormCard(title = "Senha redefinida!") {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = RcColors.SuccessBg, modifier = Modifier.size(48.dp))
                Text("Agora é só entrar com a nova senha.", color = RcColors.CardText, fontSize = 15.sp)
                RcButton("Voltar ao login", onBack, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
