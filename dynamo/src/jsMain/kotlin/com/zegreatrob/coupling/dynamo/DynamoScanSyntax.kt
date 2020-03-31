package com.zegreatrob.coupling.dynamo

import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface DynamoScanSyntax : DynamoDBSyntax, DynamoTableNameSyntax, DynamoItemSyntax {

    suspend fun performScan(scanParams: Json) = documentClient.scan(scanParams).promise().await()

    suspend fun scanAllRecords(): Array<Json> = performScan(json("TableName" to tableName))
        .continueScan()

    suspend fun Json.continueScan(): Array<Json> = if (this["LastEvaluatedKey"] != null) {
        itemsNode() + performScan(
            json("TableName" to tableName, "ExclusiveStartKey" to this["LastEvaluatedKey"])
        ).continueScan()
    } else
        itemsNode()

}