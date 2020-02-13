package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.DateTime
import kotlinx.coroutines.await
import kotlin.js.json

interface DynamoItemDeleteSyntax : DynamoDatatypeSyntax,
    DynamoDBSyntax,
    DynamoTableNameSyntax {
    suspend fun performDelete(id: String) = try {
        dynamoDB.putItem(deleteItemJson(id)).promise().await()
        true
    } catch (uhOh: Throwable) {
        false
    }

    private fun deleteItemJson(id: String) = json(
        "TableName" to tableName,
        "Item" to json(
            "id" to id.dynamoString(),
            "timestamp" to DateTime.now().isoWithMillis().dynamoString(),
            "isDeleted" to true.dynamoBool()
        )
    )

}