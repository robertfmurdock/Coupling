package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.repository.dynamo.external.batchExecuteStatement
import com.zegreatrob.coupling.repository.dynamo.external.executeStatement
import com.zegreatrob.coupling.repository.dynamo.external.query
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface DynamoQuerySyntax :
    DynamoDBSyntax,
    DynamoTableNameSyntax,
    DynamoItemSyntax {
    suspend fun performQuery(query: Json): Json = dynamoDBClient.query(query).await()

    suspend fun executeStatement(query: Json): Array<Json> = dynamoDBClient.executeStatement(query).await()
        .itemsNode()

    suspend fun batchExecuteStatement(query: Json): Array<Json> = dynamoDBClient.batchExecuteStatement(query).await()
        .also { println(JSON.stringify(it)) }
        .itemsNode()

    suspend fun queryAllRecords(
        params: Json = json("TableName" to prefixedTableName),
        limited: Boolean = false,
    ): Array<Json> = performQuery(params).let {
        if (limited) {
            it.itemsNode()
        } else {
            it.continueQuery(params)
        }
    }

    suspend fun Json.continueQuery(params: Json): Array<Json> = if (this["LastEvaluatedKey"] != null) {
        itemsNode() + performQuery(
            params.add(json("ExclusiveStartKey" to this["LastEvaluatedKey"])),
        )
            .continueQuery(params)
    } else {
        itemsNode()
    }
}
