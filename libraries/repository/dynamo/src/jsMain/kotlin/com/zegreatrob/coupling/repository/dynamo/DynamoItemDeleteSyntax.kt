package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.repository.dynamo.external.deleteIt
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface DynamoItemDeleteSyntax : DynamoDBSyntax, DynamoTableNameSyntax, DynamoLoggingSyntax {

    suspend fun performDeleteItem(keyJson: Json): Unit = logAsync("deleteItem") {
        try {
            dynamoDBClient.deleteIt(deleteItemParams(keyJson)).await()
        } catch (bad: Exception) {
            logger.warn(bad) { "Failed to delete ${JSON.stringify(keyJson)}" }
        }
    }

    private fun deleteItemParams(keyJson: Json) = json(
        "TableName" to prefixedTableName,
        "Key" to keyJson,
    )
}
