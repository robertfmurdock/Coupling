package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.json

interface TribeIdDynamoItemListGetSyntax : DynamoQueryItemListGetSyntax {

    suspend fun TribeId.scanForItemList() = queryForItemList(queryParams())

    suspend fun TribeId.scanForDeletedItemList() = queryForDeletedItemList(queryParams())

    private fun TribeId.queryParams() = json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(":tribeId" to value),
        "KeyConditionExpression" to "tribeId = :tribeId"
    )

}