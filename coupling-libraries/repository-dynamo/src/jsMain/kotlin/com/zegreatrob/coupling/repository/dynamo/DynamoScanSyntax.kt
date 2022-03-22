package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.repository.dynamo.external.scan
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface DynamoScanSyntax : DynamoDBSyntax, DynamoTableNameSyntax, DynamoItemSyntax {

    suspend fun performScan(scanParams: Json) = dynamoDBClient.scan(scanParams).await()

    suspend fun scanAllRecords(): Array<Json> = performScan(json("TableName" to prefixedTableName))
        .continueScan()

    suspend fun Json.continueScan(): Array<Json> = if (this["LastEvaluatedKey"] != null) {
        itemsNode() + performScan(
            json("TableName" to prefixedTableName, "ExclusiveStartKey" to this["LastEvaluatedKey"])
        ).continueScan()
    } else
        itemsNode()

}