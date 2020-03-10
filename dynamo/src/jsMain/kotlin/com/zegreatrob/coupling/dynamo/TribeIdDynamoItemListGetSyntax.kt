package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.json

interface TribeIdDynamoItemListGetSyntax : DynamoQueryItemListGetSyntax, DynamoLoggingSyntax {

    suspend fun TribeId.scanForItemList() = logAsync("itemList") { queryForItemList(queryParams()) }

    suspend fun TribeId.scanForDeletedItemList() = logAsync("deleteItem") { queryForDeletedItemList(queryParams()) }

    private fun TribeId.queryParams() = json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(":tribeId" to value),
        "KeyConditionExpression" to "tribeId = :tribeId"
    )

}