package br.com.rotacerta.colaborador.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import br.com.rotacerta.colaborador.data.model.EmployeeProfile
import br.com.rotacerta.colaborador.ui.components.InfoRow
import br.com.rotacerta.colaborador.ui.components.LoadingState
import br.com.rotacerta.colaborador.ui.components.SectionCard
import br.com.rotacerta.colaborador.ui.theme.BrandNavy
import br.com.rotacerta.colaborador.ui.theme.DangerRed

@Composable
fun ProfileScreen(profile: EmployeeProfile?, onChangePassword: () -> Unit, onLogout: () -> Unit) {
    if (profile == null) { LoadingState(); return }
    var confirmLogout by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(80.dp).background(BrandNavy, CircleShape), contentAlignment = Alignment.Center) {
                Text(initials(profile.name), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
            Text(profile.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 10.dp))
            Text(profile.companyName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SectionCard(title = "Meus dados") {
            profile.employeeCode?.let { InfoRow(Icons.Outlined.Badge, "Matrícula", it) }
            profile.email?.let { InfoRow(Icons.Outlined.Email, "E-mail de acesso", it) }
            InfoRow(Icons.Outlined.Phone, "Telefone", profile.phone)
            InfoRow(Icons.Outlined.Business, "Empresa", profile.companyName)
            profile.homeAddress?.label?.let { InfoRow(Icons.Outlined.Home, "Endereço", it) }
        }
        Text(
            "Dados incorretos? Procure o RH ou a gestão de transporte da empresa.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(onClick = onChangePassword, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("  Alterar senha")
        }
        TextButton(onClick = { confirmLogout = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
            Text("  Sair da conta", color = DangerRed)
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Sair da conta?") },
            text = { Text("Você precisará entrar novamente com e-mail e senha.") },
            confirmButton = { TextButton(onClick = onLogout) { Text("Sair", color = DangerRed) } },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Cancelar") } },
        )
    }
}

private fun initials(name: String) =
    name.split(' ').filter { it.isNotBlank() }.let { parts ->
        (parts.firstOrNull()?.take(1).orEmpty() + if (parts.size > 1) parts.last().take(1) else "").uppercase()
    }
