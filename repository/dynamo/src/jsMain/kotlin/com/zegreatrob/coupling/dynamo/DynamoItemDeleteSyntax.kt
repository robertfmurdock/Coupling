package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.Json

interface DynamoItemDeleteSyntax : DynamoDatatypeSyntax, DynamoDBSyntax, DynamoTableNameSyntax, DynamoItemGetSyntax,
    DynamoLoggingSyntax, DynamoItemPutSyntax {
    suspend fun <T> performDelete(
        id: String,
        tribeId: TribeId? = null,
        now: DateTime,
        toRecord: Json.() -> Record<T>,
        recordToJson: Record<T>.() -> Json
    ) = logAsync("deleteItem") {
        try {
            val current = performGetSingleItemQuery(id, tribeId)
            if (current == null) {
                false
            } else {
                logAsync("delete record add") {
                    val updatedCurrent = toRecord(current)
                        .copy(isDeleted = true, timestamp = now)
                        .recordToJson()
                    performPutItem(updatedCurrent)
                }
                true
            }
        } catch (uhOh: Throwable) {
            println("message: ${uhOh.message} uh oh ${JSON.stringify(uhOh)}")
            false
        }
    }

}
