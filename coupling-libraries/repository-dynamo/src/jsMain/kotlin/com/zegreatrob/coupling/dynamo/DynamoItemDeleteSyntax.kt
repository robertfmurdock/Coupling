package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.dynamo.external.delete
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface DynamoItemDeleteSyntax : DynamoDBSyntax, DynamoTableNameSyntax, DynamoLoggingSyntax {

    suspend fun performDeleteItem(keyJson: Json): Unit = logAsync("deleteItem") {
        try {
            dynamoDBClient.delete(deleteItemParams(keyJson)).await()
        } catch (bad: Exception) {
            logger.warn(bad) { "Failed to delete ${JSON.stringify(keyJson)}" }
        }
    }

    private fun deleteItemParams(keyJson: Json) = json(
        "TableName" to prefixedTableName,
        "Key" to keyJson
    )

}
