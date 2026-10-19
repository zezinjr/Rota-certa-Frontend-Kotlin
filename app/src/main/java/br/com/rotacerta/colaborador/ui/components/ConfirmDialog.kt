package br.com.rotacerta.colaborador.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.rotacerta.colaborador.ui.theme.RcColors

/** Mesmo layout do `DialogComponent` do painel: mensagem em negrito, descrição, "Cancelar" + botão warn. */
@Composable
fun ConfirmDialog(message: String, description: String, confirmLabel: String, onConfirm: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = RcColors.CardWhite,
        shape = RoundedCornerShape(4.dp),
        text = {
            androidx.compose.foundation.layout.Column {
                Text(message, color = RcColors.CardTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(description, color = RcColors.CardText, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp))
            }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar", color = RcColors.MatPrimary) } },
        confirmButton = { RcButton(confirmLabel, onConfirm, color = RcColors.MatWarn) },
    )
}
