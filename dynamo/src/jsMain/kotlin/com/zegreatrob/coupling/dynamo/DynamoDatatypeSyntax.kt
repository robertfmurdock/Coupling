package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.DateTime
import com.soywiz.klock.ISO8601
import kotlin.js.Json
import kotlin.js.json

interface DynamoDatatypeSyntax {

    fun Json.itemsNode() = this["Items"].unsafeCast<Array<Json>>()

    fun DateTime.isoWithMillis() = "${format(ISO8601.DATETIME_COMPLETE)}.${format("SSS")}"

    fun String.dynamoString() = json("S" to this)
    fun String?.dynamoString() = this?.dynamoString() ?: dynamoNull()
    fun Number.dynamoNumber(): Json = json("N" to "$this")
    fun Boolean.dynamoBool() = json("BOOL" to this)
    fun dynamoNull(): Json = json("NULL" to "true")

    fun Json.getDynamoStringValue(property: String) =
        this[property].unsafeCast<Json?>()?.get("S")?.unsafeCast<String?>()

    fun Json.getDynamoNumberValue(property: String) =
        this[property].unsafeCast<Json?>()?.get("N")?.unsafeCast<String?>()

    fun Json.getDynamoBoolValue(property: String) =
        this[property].unsafeCast<Json?>()?.get("BOOL")?.unsafeCast<Boolean?>()

}