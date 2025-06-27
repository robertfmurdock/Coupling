package com.zegreatrob.coupling.client.components

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private val dateTimeFormat = LocalDateTime.Format {
    hour()
    chars(":")
    minute()
    chars(":")
    second()
    chars(", ")
    year()
    chars("-")
    monthNumber()
    chars("-")
    day()
}

fun Instant.format() = dateTimeFormat.format(toLocalDateTime(TimeZone.currentSystemDefault()))
