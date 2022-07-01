package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.repository.dynamo.external.put
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface DynamoItemPutSyntax : DynamoDBSyntax, DynamoTableNameSyntax, DynamoLoggingSyntax {

    suspend fun performPutItem(itemJson: Json) = logAsync("putItem") {
        try {
            dynamoDBClient.put(putItemParams(itemJson)).await()
        } catch (bad: Exception) {
            logger.warn(bad) { "Failed to put ${JSON.stringify(itemJson)}" }
        }
    }

    private fun putItemParams(itemJson: Json) = json(
        "TableName" to prefixedTableName,
        "Item" to itemJson
    )
}
