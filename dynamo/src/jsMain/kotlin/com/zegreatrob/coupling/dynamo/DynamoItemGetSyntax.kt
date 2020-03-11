package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.json

interface DynamoItemGetSyntax : DynamoScanSyntax,
    DynamoQuerySyntax,
    DynamoDatatypeSyntax,
    DynamoItemSyntax,
    DynamoTableNameSyntax,
    DynamoLoggingSyntax {

    suspend fun performGetSingleItemQuery(id: String, tribeId: TribeId? = null) = logAsync("getSingleItem") {
        performQuery(queryParams(tribeId, id))
            .itemsNode()
            .sortByRecordTimestamp()
            .lastOrNull()
            ?.let(::excludeDeleted)
    }

    private fun queryParams(tribeId: TribeId?, id: String) = if (tribeId != null) json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(
            ":tribeId" to tribeId.value,
            ":id" to id
        ),
        "KeyConditionExpression" to "tribeId = :tribeId",
        "FilterExpression" to "id = :id"
    ) else json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(
            ":id" to id
        ),
        "KeyConditionExpression" to "id = :id"
    )

    private fun singleScanParams(id: String, tribeId: TribeId?) = if (tribeId == null) json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(
            ":id" to id
        ),
        "FilterExpression" to "id = :id"
    )
    else json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(
            ":id" to id,
            ":tribeId" to tribeId.value
        ),
        "FilterExpression" to "id = :id AND tribeId = :tribeId"
    )

}