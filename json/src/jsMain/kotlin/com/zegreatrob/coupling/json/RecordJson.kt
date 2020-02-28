package com.zegreatrob.coupling.json

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.Record
import kotlin.js.Json
import kotlin.js.json

fun <T> Json.recordFor(data: T) = Record(
    data = data,
    modifyingUserEmail = this["modifyingUserEmail"].toString(),
    isDeleted = false,
    timestamp = DateTime(this["timestamp"].unsafeCast<String>().toLong())
)

fun Record<*>.toJson() = json(
    "timestamp" to timestamp.unixMillisLong.toString(),
    "modifyingUserEmail" to modifyingUserEmail
)

val recordJsonKeys
    get() = Record(null, "", false, DateTime.EPOCH)
        .toJson()
        .getKeys()