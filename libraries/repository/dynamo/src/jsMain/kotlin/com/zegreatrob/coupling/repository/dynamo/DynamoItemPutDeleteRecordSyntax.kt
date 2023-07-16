package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.datetime.Instant
import kotlin.js.Json

interface DynamoItemPutDeleteRecordSyntax :
    DynamoDatatypeSyntax,
    DynamoDBSyntax,
    DynamoTableNameSyntax,
    DynamoItemGetSyntax,
    DynamoLoggingSyntax,
    DynamoItemPutSyntax {
    suspend fun <T> performDelete(
        id: String,
        partyId: PartyId? = null,
        now: Instant,
        toRecord: Json.() -> Record<T>?,
        recordToJson: Record<T>.() -> Json,
    ) = logAsync("deleteItem") {
        try {
            val current = performGetSingleItemQuery(id, partyId)
            if (current == null) {
                false
            } else {
                logAsync("delete record add") {
                    toRecord(current)
                        ?.copy(isDeleted = true, timestamp = now)
                        ?.recordToJson()
                        ?.let { performPutItem(it) }
                }
                true
            }
        } catch (uhOh: Throwable) {
            println("message: ${uhOh.message} uh oh ${JSON.stringify(uhOh)}")
            false
        }
    }
}
