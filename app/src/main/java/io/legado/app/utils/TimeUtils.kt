package io.legado.app.utils

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.SignStyle
import java.time.temporal.ChronoField
import kotlin.math.abs

fun Long.toTimeAgo(): String {
    val curTime = System.currentTimeMillis()
    val time = this
    val seconds = abs(System.currentTimeMillis() - time) / 1000f
    val end = if (time < curTime) "前" else "后"

    val start = when {
        seconds < 60 -> "${seconds.toInt()}秒"
        seconds < 3600 -> {
            val minutes = seconds / 60f
            "${minutes.toInt()}分钟"
        }
        seconds < 86400 -> {
            val hours = seconds / 3600f
            "${hours.toInt()}小时"
        }
        seconds < 604800 -> {
            val days = seconds / 86400f
            "${days.toInt()}天"
        }
        seconds < 2_628_000 -> {
            val weeks = seconds / 604800f
            "${weeks.toInt()}周"
        }
        seconds < 31_536_000 -> {
            val months = seconds / 2_628_000f
            "${months.toInt()}月"
        }
        else -> {
            val years = seconds / 31_536_000f
            "${years.toInt()}年"
        }
    }
    return start + end
}

private val TAG_TIMESTAMP_REGEX = Regex("""\b\d{10,13}\b""")
private val TAG_DATETIME_REGEX = Regex("""\d{4}-\d{1,2}-\d{1,2}(\s+\d{1,2}:\d{1,2}(:\d{1,2})?)?""")

private val FLEXIBLE_FORMATTER = DateTimeFormatterBuilder()
    .appendValue(ChronoField.YEAR, 4)
    .appendLiteral('-')
    .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NOT_NEGATIVE)
    .appendLiteral('-')
    .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
    .optionalStart()
        .appendLiteral(' ')
        .appendValue(ChronoField.HOUR_OF_DAY, 1, 2, SignStyle.NOT_NEGATIVE)
        .appendLiteral(':')
        .appendValue(ChronoField.MINUTE_OF_HOUR, 1, 2, SignStyle.NOT_NEGATIVE)
        .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 1, 2, SignStyle.NOT_NEGATIVE)
        .optionalEnd()
    .optionalEnd()
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
    .toFormatter()

fun String.parseTagTime(): Long? {
    val text = trim()
    if (text.isEmpty()) return null

    TAG_TIMESTAMP_REGEX.find(text)?.value?.toLongOrNull()?.let { value ->
        return if (value < 100_000_000_000L) value * 1000 else value
    }

    TAG_DATETIME_REGEX.find(text)?.value?.let { dateText ->
        return runCatching {
            val ldt = LocalDateTime.parse(dateText, FLEXIBLE_FORMATTER)
            ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }.getOrNull()
    }

    return null
}

fun Int.toDurationTime(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
