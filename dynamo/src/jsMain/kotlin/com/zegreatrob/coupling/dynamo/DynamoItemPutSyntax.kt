package com.zegreatrob.coupling.dynamo

import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface DynamoItemPutSyntax : DynamoDBSyntax, DynamoTableNameSyntax, DynamoLoggingSyntax {

    suspend fun performPutItem(itemJson: Json) = logAsync("putItem") {
        try {
            documentClient.put(putItemParams(itemJson)).promise().await()
        } catch (bad: Exception) {
            logger.warn(bad) { "Failed to put ${JSON.stringify(itemJson)}" }
        }
    }

    private fun putItemParams(itemJson: Json) = json(
        "TableName" to tableName,
        "Item" to itemJson
    )

}
