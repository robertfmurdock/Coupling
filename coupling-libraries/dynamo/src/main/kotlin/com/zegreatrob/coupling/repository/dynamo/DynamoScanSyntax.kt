package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.repository.dynamo.external.scan
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface DynamoScanSyntax : DynamoDBSyntax, DynamoTableNameSyntax, DynamoItemSyntax {

    suspend fun performScan(scanParams: Json) = dynamoDBClient.scan(scanParams).await()

    suspend fun scanAllRecords(params: Json = json("TableName" to prefixedTableName)): Array<Json> =
        performScan(params)
            .continueScan(params)

    suspend fun Json.continueScan(params: Json): Array<Json> = if (this["LastEvaluatedKey"] != null) {
        itemsNode() + performScan(
            params.add(json("ExclusiveStartKey" to this["LastEvaluatedKey"]))
        )
            .continueScan(params)
    } else {
        itemsNode()
    }
}
