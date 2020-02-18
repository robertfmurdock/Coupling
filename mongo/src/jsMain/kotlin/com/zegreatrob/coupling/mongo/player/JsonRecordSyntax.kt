package com.zegreatrob.coupling.mongo.player

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.mongo.JsonTimestampSyntax
import com.zegreatrob.coupling.mongo.pin.JsonStringValueSyntax
import kotlin.js.Json

interface JsonRecordSyntax : JsonTimestampSyntax, JsonStringValueSyntax {
    fun <T> Json.toDbRecord(data: T) = Record(
        data = data,
        timestamp = timeStamp() ?: DateTime.EPOCH,
        modifyingUserEmail = stringValue("modifiedByUsername") ?: "NOT RECORDED",
        isDeleted = this["isDeleted"]?.unsafeCast<Boolean>() ?: false
    )
}