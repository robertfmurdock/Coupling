package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.party.PartyId
import kotlin.js.json

interface TribeIdDynamoItemListGetSyntax : DynamoQueryItemListGetSyntax, DynamoLoggingSyntax {

    suspend fun PartyId.queryForItemList() = logAsync("itemList") { queryForItemList(itemListQueryParams()) }

    suspend fun PartyId.queryForDeletedItemList() =
        logAsync("getDeleteItems") { queryForDeletedItemList(itemListQueryParams()) }

    fun PartyId.itemListQueryParams() = json(
        "TableName" to prefixedTableName,
        "ExpressionAttributeValues" to json(":tribeId" to value),
        "KeyConditionExpression" to "tribeId = :tribeId",
    )
}
