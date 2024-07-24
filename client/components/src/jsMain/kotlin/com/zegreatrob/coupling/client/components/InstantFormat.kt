package com.zegreatrob.coupling.client.components

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
    dayOfMonth()
}

fun Instant.format() = dateTimeFormat.format(toLocalDateTime(TimeZone.currentSystemDefault()))
