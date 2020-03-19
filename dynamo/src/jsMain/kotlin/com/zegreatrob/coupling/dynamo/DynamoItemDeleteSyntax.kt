package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.Json
import kotlin.js.json

interface DynamoItemDeleteSyntax : DynamoDatatypeSyntax, DynamoDBSyntax, DynamoTableNameSyntax, DynamoItemGetSyntax,
    DynamoLoggingSyntax, DynamoItemPutSyntax {
    suspend fun performDelete(id: String, recordJson: Json, tribeId: TribeId? = null) = logAsync("deleteItem") {
        try {
            val current = performGetSingleItemQuery(id, tribeId)
            if (current == null) {
                false
            } else {
                logAsync("delete record add") {
                    val updatedCurrent = current.add(recordJson)
                        .add(json("isDeleted" to true))
                    performPutItem(updatedCurrent)
                }
                true
            }
        } catch (uhOh: Throwable) {
            println("message: ${uhOh.message} uh oh ${JSON.stringify(uhOh)}")
            false
        }
    }

    private inline fun Json.deleteItemParams() = json(
        "TableName" to tableName,
        "Item" to this
    )
}
