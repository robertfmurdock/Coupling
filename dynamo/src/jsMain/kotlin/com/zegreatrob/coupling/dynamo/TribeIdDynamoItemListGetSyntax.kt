package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.json

interface TribeIdDynamoItemListGetSyntax : DynamoItemListGetSyntax {

    suspend fun TribeId.scanForItemList() = scanForItemList(scanParams())

    suspend fun TribeId.scanForDeletedItemList() = scanForDeletedItemList(scanParams())

    private fun TribeId.scanParams() = json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(":tribeId" to value),
        "FilterExpression" to "tribeId = :tribeId"
    )

}