package com.zegreatrob.coupling.repository.dynamo

import com.soywiz.klock.DateTime
import com.soywiz.klock.PatternDateFormat
import com.soywiz.klock.parse
import kotlin.js.Json
import kotlin.js.json


private val dateFormat = PatternDateFormat("YYYYMMddHHmmss.SSS")

interface DynamoDatatypeSyntax {

    fun Json.itemsNode() = this["Items"].unsafeCast<Array<Json>>()

    fun DateTime.isoWithMillis() = format(dateFormat)

    fun Json.getDynamoDateTimeValue(property: String) = this[property].unsafeCast<String?>()
        ?.let(dateFormat::parse)

    fun Json.getDynamoStringValue(property: String) = this[property].unsafeCast<String?>()

    fun Json.getDynamoNumberValue(property: String) = this[property].unsafeCast<Number?>()

    fun Json.getDynamoBoolValue(property: String) = this[property].unsafeCast<Boolean?>()

    fun Json.getDynamoListValue(property: String) = this[property].unsafeCast<Array<Json>?>()

    fun nullFreeJson(vararg pairs: Pair<String, Any?>) = json(
        *pairs.toMap()
            .filterValues { it != null && it != "" }
            .toList()
            .toTypedArray()
    )

}
