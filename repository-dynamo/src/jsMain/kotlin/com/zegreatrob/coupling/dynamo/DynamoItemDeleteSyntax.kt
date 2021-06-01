package com.zegreatrob.coupling.dynamo

import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface DynamoItemDeleteSyntax : DynamoDBSyntax, DynamoTableNameSyntax, DynamoLoggingSyntax {

    suspend fun performDeleteItem(keyJson: Json) = logAsync("deleteItem") {
        try {
            documentClient.delete(deleteItemParams(keyJson)).promise().await()
        } catch (bad: Exception) {
            logger.warn(bad) { "Failed to delete ${JSON.stringify(keyJson)}" }
        }
    }

    private fun deleteItemParams(keyJson: Json) = json(
        "TableName" to tableName,
        "Key" to keyJson
    )

}
