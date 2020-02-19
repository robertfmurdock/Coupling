package com.zegreatrob.coupling.dynamo

import kotlin.js.Json

interface DynamoItemListGetSyntax : DynamoItemSyntax, DynamoDBSyntax, DynamoTableNameSyntax, DynamoScanSyntax {

    suspend fun scanForItemList(scanParams: Json) = fullList(scanParams).filterNot(::isDeleted)
    suspend fun scanForDeletedItemList(scanParams: Json) = fullList(scanParams).filter(::isDeleted)

    private suspend fun fullList(scanParams: Json) = performScan(scanParams)
        .itemsNode()
        .sortByRecordTimestamp()
        .groupBy { it.getDynamoStringValue("id") }
        .map { it.value.last() }

}

