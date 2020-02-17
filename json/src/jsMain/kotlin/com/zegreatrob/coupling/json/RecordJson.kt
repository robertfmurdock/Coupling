package com.zegreatrob.coupling.json

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.Record
import kotlin.js.Json
import kotlin.js.json

fun <T> Json.recordFor(data: T) = Record(
    data = data,
    timestamp = DateTime(this["timestamp"].unsafeCast<String>().toLong()),
    modifyingUserEmail = this["modifyingUserEmail"].toString(),
    isDeleted = false
)

fun Record<*>.toJson() = json(
    "timestamp" to timestamp.unixMillisLong.toString(),
    "modifyingUserEmail" to modifyingUserEmail
)

val recordJsonKeys
    get() = Record(null, DateTime.EPOCH, false, "")
        .toJson()
        .getKeys()