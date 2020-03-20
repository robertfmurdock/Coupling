package com.zegreatrob.coupling.dynamo

import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface DynamoItemPutSyntax : DynamoDBSyntax, DynamoTableNameSyntax, DynamoLoggingSyntax {

    suspend fun performPutItem(itemJson: Json) = logAsync("putItem") {
        documentClient.put(putItemParams(itemJson)).promise().await()
    }

    private fun putItemParams(itemJson: Json) = json(
        "TableName" to tableName,
        "Item" to itemJson
    )

}
