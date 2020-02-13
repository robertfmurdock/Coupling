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
            dynamoDB.putItem(deleteItemJson(id, tribeId)).promise().await()
            true
        }
    } catch (uhOh: Throwable) {
        println("uh oh ${JSON.stringify(uhOh)}")
        false
    }

    private fun deleteItemJson(id: String, tribeId: TribeId?) = json(
        "TableName" to tableName,
        "Item" to json(
            "id" to id.dynamoString(),
            "timestamp" to DateTime.now().isoWithMillis().dynamoString(),
            "isDeleted" to true.dynamoBool()
        ).appendTribeId(tribeId)
    )

    private fun Json.appendTribeId(tribeId: TribeId?) = if (tribeId == null) {
        this
    } else {
        this.add(json("tribeId" to tribeId.value.dynamoString()))
    }

}
