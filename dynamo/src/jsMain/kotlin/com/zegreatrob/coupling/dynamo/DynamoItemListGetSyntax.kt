package com.zegreatrob.coupling.dynamo

import kotlinx.coroutines.await
import kotlin.js.Json

interface DynamoItemListGetSyntax : DynamoItemSyntax,
    DynamoDBSyntax,
    DynamoTableNameSyntax {

    suspend fun scanForItemList(scanParams: Json) = dynamoDB.scan(scanParams).promise().await()
        .itemsNode()
        .sortByRecordTimestamp()
        .groupBy { it.getDynamoStringValue("id") }
        .map { it.value.last() }
        .mapNotNull(::excludeDeleted)
}