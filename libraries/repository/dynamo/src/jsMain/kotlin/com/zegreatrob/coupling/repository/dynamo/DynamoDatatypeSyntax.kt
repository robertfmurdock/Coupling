package com.zegreatrob.coupling.repository.dynamo

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.js.Json
import kotlin.js.json

// private val dateFormat = PatternDateFormat("YYYYMMddHHmmss.SSS")

interface DynamoDatatypeSyntax {

    fun Json.itemsNode() = this["Items"].unsafeCast<Array<Json>>()

    fun Instant.isoWithMillis() = toLocalDateTime(TimeZone.UTC).run {
        "${year}${padMonth}${padDay}${padHour}${padMinute}$padSecond"
    } + ".$padMillis"

    val LocalDateTime.padMonth get() = "$monthNumber".padStart(2, '0')
    val LocalDateTime.padDay get() = "$dayOfMonth".padStart(2, '0')
    val LocalDateTime.padHour get() = "$hour".padStart(2, '0')
    val LocalDateTime.padMinute get() = "$minute".padStart(2, '0')
    val LocalDateTime.padSecond get() = "$second".padStart(2, '0')
    val Instant.padMillis get() = "${this.toEpochMilliseconds() % 1000}".padStart(3, '0')

    fun Json.getDynamoDateTimeValue(property: String) = this[property].unsafeCast<String?>()
        ?.let {
            LocalDateTime(
                year = it.slice(0..3).toInt(),
                month = it.slice(4..5).toInt().let { monthNumber -> Month.values()[monthNumber - 1] },
                dayOfMonth = it.slice(6..7).toInt(),
                hour = it.slice(8..9).toInt(),
                minute = it.slice(10..11).toInt(),
                second = it.slice(12..13).toInt(),
                nanosecond = it.slice(15..17).toInt() * 1000000,
            )
                .toInstant(TimeZone.UTC)
        }

    fun Json.getDynamoStringValue(property: String) = this[property].unsafeCast<String?>()

    fun Json.getDynamoNumberValue(property: String) = this[property].unsafeCast<Number?>()

    fun Json.getDynamoBoolValue(property: String) = this[property].unsafeCast<Boolean?>()

    fun Json.getDynamoListValue(property: String) = this[property].unsafeCast<Array<Json>?>()

    fun nullFreeJson(vararg pairs: Pair<String, Any?>) = json(
        *pairs.toMap()
            .filterValues { it != null && it != "" }
            .toList()
            .toTypedArray(),
    )
}
