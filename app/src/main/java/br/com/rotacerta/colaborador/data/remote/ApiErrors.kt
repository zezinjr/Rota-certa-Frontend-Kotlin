package br.com.rotacerta.colaborador.data.remote

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException

/** Converte erros do DRF ({"detail": ...} ou {"campo": ["msg"]}) em texto amigável. */
fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> parseDrfError(response()?.errorBody()?.string()) ?: when (code()) {
        401 -> "Sessão expirada. Entre novamente."
        403 -> "Você não tem permissão para acessar este recurso."
        404 -> "Registro não encontrado."
        in 500..599 -> "O servidor está indisponível no momento."
        else -> "Erro inesperado (${code()})."
    }
    is IOException -> "Não foi possível conectar ao servidor. Verifique a internet ou o endereço do servidor."
    else -> message ?: "Erro inesperado."
}

internal fun parseDrfError(body: String?): String? {
    if (body.isNullOrBlank()) return null
    val obj = runCatching { AppJson.parseToJsonElement(body) as? JsonObject }.getOrNull() ?: return null
    (obj["detail"] as? JsonPrimitive)?.let { return it.content }
    val first = obj.values.firstOrNull() ?: return null
    return when (first) {
        is JsonArray -> first.firstOrNull()?.jsonPrimitive?.content
        is JsonPrimitive -> first.content
        else -> null
    }
}
