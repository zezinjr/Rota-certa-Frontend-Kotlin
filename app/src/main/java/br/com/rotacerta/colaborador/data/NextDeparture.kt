package br.com.rotacerta.colaborador.data

import br.com.rotacerta.colaborador.data.model.ScheduleInfo
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

data class Departure(val at: LocalDateTime, val until: Duration)

private val WEEKDAY_CODES = mapOf(
    "MON" to DayOfWeek.MONDAY,
    "TUE" to DayOfWeek.TUESDAY,
    "WED" to DayOfWeek.WEDNESDAY,
    "THU" to DayOfWeek.THURSDAY,
    "FRI" to DayOfWeek.FRIDAY,
    "SAT" to DayOfWeek.SATURDAY,
    "SUN" to DayOfWeek.SUNDAY,
)

/** Próxima saída considerando os horários (dias da semana + hora) da empresa. */
fun nextDeparture(schedules: List<ScheduleInfo>, now: LocalDateTime = LocalDateTime.now()): Departure? {
    val candidates = schedules.flatMap { schedule ->
        val time = runCatching { LocalTime.parse(schedule.time) }.getOrNull() ?: return@flatMap emptyList()
        val days = schedule.weekdays.mapNotNull { WEEKDAY_CODES[it.uppercase().take(3)] }.toSet()
        (0L..7L).map { now.toLocalDate().plusDays(it).atTime(time) }
            .filter { it.isAfter(now) && it.dayOfWeek in days }
    }
    val next = candidates.minOrNull() ?: return null
    return Departure(next, Duration.between(now, next))
}

fun weekdayLabel(code: String): String = when (code.uppercase().take(3)) {
    "MON" -> "Seg"
    "TUE" -> "Ter"
    "WED" -> "Qua"
    "THU" -> "Qui"
    "FRI" -> "Sex"
    "SAT" -> "Sáb"
    "SUN" -> "Dom"
    else -> code
}

fun formatCountdown(duration: Duration): String {
    val minutes = duration.toMinutes()
    return when {
        minutes < 1 -> "agora"
        minutes < 60 -> "em $minutes min"
        minutes < 24 * 60 -> "em ${minutes / 60}h ${"%02d".format(minutes % 60)}min"
        else -> "em ${minutes / (24 * 60)} dia(s)"
    }
}
