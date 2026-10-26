package br.com.rotacerta.colaborador.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.rotacerta.colaborador.data.model.EmployeeProfile
import br.com.rotacerta.colaborador.ui.components.Badge
import br.com.rotacerta.colaborador.ui.components.BadgeKind
import br.com.rotacerta.colaborador.ui.components.ConfirmDialog
import br.com.rotacerta.colaborador.ui.components.InfoRow
import br.com.rotacerta.colaborador.ui.components.LoadingState
import br.com.rotacerta.colaborador.ui.components.MessageState
import br.com.rotacerta.colaborador.ui.components.Panel
import br.com.rotacerta.colaborador.ui.components.RcAvatar
import br.com.rotacerta.colaborador.ui.components.RcButton
import br.com.rotacerta.colaborador.ui.components.RcStrokedButton
import br.com.rotacerta.colaborador.ui.theme.RcColors

@Composable
fun ProfileScreen(
    profile: EmployeeProfile?,
    error: String?,
    updatingWhatsapp: Boolean,
    onWhatsappChange: (Boolean) -> Unit,
    onRetry: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit,
) {
    var confirmLogout by remember { mutableStateOf(false) }
    if (confirmLogout) {
        ConfirmDialog(
            message = "Deseja sair da conta?",
            description = "Você precisará entrar novamente com e-mail e senha.",
            confirmLabel = "Sair",
            onConfirm = onLogout,
            onCancel = { confirmLogout = false },
        )
    }
    when {
        profile != null -> ProfileContent(profile, updatingWhatsapp, onWhatsappChange, onChangePassword) { confirmLogout = true }
        // Mesmo sem perfil, o usuário precisa conseguir tentar de novo ou sair.
        error != null -> Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MessageState(
                "Não foi possível carregar o perfil",
                error,
                modifier = Modifier.weight(1f, fill = false),
                actionLabel = "Tentar novamente",
                onAction = onRetry,
            )
            RcButton("Sair da conta", { confirmLogout = true }, Modifier.fillMaxWidth(), icon = Icons.AutoMirrored.Filled.Logout, color = RcColors.MatWarn)
        }
        else -> LoadingState()
    }
}

@Composable
private fun ProfileContent(
    profile: EmployeeProfile,
    updatingWhatsapp: Boolean,
    onWhatsappChange: (Boolean) -> Unit,
    onChangePassword: () -> Unit,
    onLogoutClick: () -> Unit,
) {

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RcAvatar(profile.name, size = 64)
                Column(Modifier.padding(start = 16.dp).weight(1f)) {
                    Text(profile.name, color = RcColors.TextStrong, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(profile.companyName, color = RcColors.TextMuted, fontSize = 14.sp)
                    Badge("ativo", BadgeKind.SUCCESS, Modifier.padding(top = 8.dp))
                }
            }
        }
        Panel(title = "Meus dados") {
            profile.employeeCode?.let { InfoRow(Icons.Filled.Badge, "Matrícula", it) }
            profile.email?.let { InfoRow(Icons.Filled.Email, "E-mail de acesso", it) }
            InfoRow(Icons.Filled.Phone, "Telefone", profile.phone)
            InfoRow(Icons.Filled.Business, "Empresa", profile.companyName)
            profile.homeAddress?.label?.let { InfoRow(Icons.Filled.Home, "Endereço", it) }
        }
        WhatsappPanel(profile, updatingWhatsapp, onWhatsappChange)
        Text(
            "Dados incorretos? Procure o RH ou a gestão de transporte da empresa.",
            color = RcColors.TextMuted,
            fontSize = 13.sp,
        )
        RcStrokedButton("Alterar senha", onChangePassword, Modifier.fillMaxWidth(), icon = Icons.Filled.Lock)
        RcButton("Sair da conta", onLogoutClick, Modifier.fillMaxWidth(), icon = Icons.AutoMirrored.Filled.Logout, color = RcColors.MatWarn)
    }
}

/** Consentimento para os avisos da van pelo WhatsApp (o colaborador liga e desliga quando quiser). */
@Composable
private fun WhatsappPanel(profile: EmployeeProfile, updating: Boolean, onChange: (Boolean) -> Unit) {
    // Mesmo indisponível no servidor, quem já ativou precisa conseguir desativar.
    val canToggle = (profile.whatsappAvailable || profile.whatsappOptIn) && !updating
    Panel(title = "Avisos") {
        InfoRow(
            Icons.AutoMirrored.Filled.Chat,
            "WhatsApp",
            if (profile.whatsappOptIn) "Ativado" else "Desativado",
        ) {
            Switch(
                checked = profile.whatsappOptIn,
                onCheckedChange = onChange,
                enabled = canToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = RcColors.White,
                    checkedTrackColor = RcColors.Accent,
                    uncheckedThumbColor = RcColors.TextLabel,
                    uncheckedTrackColor = RcColors.Background,
                    uncheckedBorderColor = RcColors.Divider,
                ),
            )
        }
        Text(
            if (profile.whatsappAvailable || profile.whatsappOptIn) {
                "Receba no WhatsApp (${profile.phone}) quando a van estiver a poucos minutos e chegando ao seu ponto. " +
                    "Ao ativar, você autoriza essas mensagens e pode desativar quando quiser."
            } else {
                "Os avisos pelo WhatsApp ainda não estão disponíveis para a sua conta. " +
                    "Confira se o telefone do cadastro está correto ou fale com a gestão de transporte."
            },
            color = RcColors.TextMuted,
            fontSize = 13.sp,
        )
    }
}
