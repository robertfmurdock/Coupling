package com.zegreatrob.coupling.dynamo

import kotlinx.coroutines.await
import kotlin.js.json

interface DynamoItemListGetSyntax : DynamoItemSyntax,
    DynamoDBSyntax,
    DynamoTableNameSyntax {

    suspend fun scanForItemList() = dynamoDB.scan(scanParams()).promise().await()
        .itemsNode()
        .sortByRecordTimestamp()
        .groupBy { it.getDynamoStringValue("id") }
        .map { it.value.last() }
        .mapNotNull(::excludeDeleted)

    private fun scanParams() = json("TableName" to tableName)
}