package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.json

interface TribeIdDynamoItemListGetSyntax : DynamoItemListGetSyntax {

    suspend fun TribeId.scanForItemList() = scanForItemList(scanParams())

    suspend fun TribeId.scanForDeletedItemList() =
        DynamoPlayerRepository.scanForDeletedItemList(scanParams())

    private fun TribeId.scanParams() =
        json(
            "TableName" to DynamoPlayerRepository.tableName,
            "ExpressionAttributeValues" to json(":tribeId" to value.dynamoString()),
            "FilterExpression" to "tribeId = :tribeId"
        )

}