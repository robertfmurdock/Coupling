package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.DateTime
import com.soywiz.klock.ISO8601
import kotlin.js.Json

interface DynamoDatatypeSyntax {

    fun Json.itemsNode() = this["Items"].unsafeCast<Array<Json>>()

    fun DateTime.isoWithMillis() = "${format(ISO8601.DATETIME_COMPLETE)}.${format("SSS")}"

    fun Json.getDynamoStringValue(property: String) = this[property].unsafeCast<String?>()

    fun Json.getDynamoNumberValue(property: String) = this[property].unsafeCast<Number?>()

    fun Json.getDynamoBoolValue(property: String) = this[property].unsafeCast<Boolean?>()

    fun Json.getDynamoListValue(property: String) = this[property].unsafeCast<Array<Json>?>()

}