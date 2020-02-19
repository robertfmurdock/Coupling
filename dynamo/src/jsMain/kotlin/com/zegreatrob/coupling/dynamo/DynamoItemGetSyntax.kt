package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.json

interface DynamoItemGetSyntax : DynamoScanSyntax,
    DynamoDatatypeSyntax,
    DynamoItemSyntax,
    DynamoTableNameSyntax {

    suspend fun performGetSingleItemQuery(id: String, tribeId: TribeId? = null) =
        performScan(singleScanParams(id, tribeId))
            .itemsNode()
            .sortByRecordTimestamp()
            .lastOrNull()
            ?.let(::excludeDeleted)

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