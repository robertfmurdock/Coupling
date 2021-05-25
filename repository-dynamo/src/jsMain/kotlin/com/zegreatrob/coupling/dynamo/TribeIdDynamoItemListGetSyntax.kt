package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.json

interface TribeIdDynamoItemListGetSyntax : DynamoQueryItemListGetSyntax, DynamoLoggingSyntax {

    suspend fun TribeId.queryForItemList() = logAsync("itemList") { queryForItemList(itemListQueryParams()) }

    suspend fun TribeId.queryForDeletedItemList() =
        logAsync("getDeleteItems") { queryForDeletedItemList(itemListQueryParams()) }

    fun TribeId.itemListQueryParams() = json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(":tribeId" to value),
        "KeyConditionExpression" to "tribeId = :tribeId"
    )

}