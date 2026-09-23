package br.com.rotacerta.colaborador.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.rotacerta.colaborador.data.RotaCertaRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: RotaCertaRepository) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    val serverUrl: String get() = repository.session.serverUrl

    val canSubmit get() = email.contains("@") && password.isNotBlank() && !loading

    fun submit() {
        if (!canSubmit) return
        loading = true
        error = null
        viewModelScope.launch {
            repository.login(email, password).onFailure { error = it.message }
            loading = false
        }
    }

    fun updateServerUrl(url: String) = repository.session.updateServerUrl(url)
}

class ChangePasswordViewModel(private val repository: RotaCertaRepository) : ViewModel() {
    var current by mutableStateOf("")
    var new by mutableStateOf("")
    var confirm by mutableStateOf("")
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var success by mutableStateOf(false)
        private set

    fun submit() {
        error = when {
            current.isBlank() || new.isBlank() -> "Preencha todos os campos."
            new.length < 8 -> "A nova senha deve ter pelo menos 8 caracteres."
            new != confirm -> "As senhas não conferem."
            else -> null
        }
        if (error != null) return
        loading = true
        viewModelScope.launch {
            repository.changePassword(current, new)
                .onSuccess { success = true; current = ""; new = ""; confirm = "" }
                .onFailure { error = it.message }
            loading = false
        }
    }

    fun logout() = repository.logout()
}

class ForgotPasswordViewModel(private val repository: RotaCertaRepository) : ViewModel() {
    enum class Step { REQUEST, RESET, DONE }

    var step by mutableStateOf(Step.REQUEST)
        private set
    var email by mutableStateOf("")
    var code by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    /** Código devolvido pelo backend em modo DEBUG (não há envio de e-mail/SMS ainda). */
    var devCode by mutableStateOf<String?>(null)
        private set

    fun requestCode() {
        if (!email.contains("@")) { error = "Informe um e-mail válido."; return }
        loading = true
        error = null
        viewModelScope.launch {
            repository.requestPasswordReset(email)
                .onSuccess { devCode = it.verificationCode; step = Step.RESET }
                .onFailure { error = it.message }
            loading = false
        }
    }

    fun reset() {
        if (code.isBlank() || newPassword.length < 8) {
            error = "Informe o código e uma senha com pelo menos 8 caracteres."
            return
        }
        loading = true
        error = null
        viewModelScope.launch {
            repository.completePasswordReset(email, code, newPassword)
                .onSuccess { step = Step.DONE }
                .onFailure { error = it.message }
            loading = false
        }
    }
}
