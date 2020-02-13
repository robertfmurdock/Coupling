package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.json

interface DynamoItemGetSyntax : DynamoQuerySyntax,
    DynamoDatatypeSyntax,
    DynamoItemSyntax,
    DynamoTableNameSyntax {

    suspend fun performGetSingleItemQuery(id: String, tribeId: TribeId? = null) = performQuery(singleQuery(id, tribeId))
        .itemsNode()
        .sortByRecordTimestamp()
        .lastOrNull()
        ?.let(::excludeDeleted)

    private fun singleQuery(id: String, tribeId: TribeId?) = if (tribeId == null) json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(
            ":id" to id.dynamoString()
        ),
        "KeyConditionExpression" to "id = :id"
    )
    else json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(
            ":id" to id.dynamoString(),
            ":tribeId" to tribeId.value.dynamoString()
        ),
        "KeyConditionExpression" to "id = :id AND tribeId = :tribeId"
    )

}