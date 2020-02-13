package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface DynamoItemDeleteSyntax : DynamoDatatypeSyntax, DynamoDBSyntax, DynamoTableNameSyntax, DynamoItemGetSyntax {
    suspend fun performDelete(id: String, tribeId: TribeId? = null) = try {
        val current = performGetSingleItemQuery(id, tribeId)
        if (current == null) {
            false
        } else {
            dynamoDB.putItem(current.deleteItemParams()).promise().await()
            true
        }
    } catch (uhOh: Throwable) {
        println("message: ${uhOh.message} uh oh ${JSON.stringify(uhOh)}")
        false
    }

    private inline fun Json.deleteItemParams() = json(
        "TableName" to tableName,
        "Item" to add(
            json(
                "timestamp" to DateTime.now().isoWithMillis().dynamoString(),
                "isDeleted" to true.dynamoBool()
            )
        )
    )
}
