package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.party.PartyId
import kotlin.js.json

interface DynamoItemGetSyntax :
    DynamoScanSyntax,
    DynamoQuerySyntax,
    DynamoDatatypeSyntax,
    DynamoItemSyntax,
    DynamoTableNameSyntax,
    DynamoLoggingSyntax {

    suspend fun performGetSingleItemQuery(id: String, partyId: PartyId? = null) = logAsync("getSingleItem") {
        performQuery(queryParams(partyId, id))
            .itemsNode()
            .sortByRecordTimestamp()
            .lastOrNull()
            ?.let(::excludeDeleted)
    }

    private fun queryParams(partyId: PartyId?, id: String) = if (partyId != null) json(
        "TableName" to prefixedTableName,
        "ExpressionAttributeValues" to json(
            ":tribeId" to partyId.value,
            ":id" to id
        ),
        "KeyConditionExpression" to "tribeId = :tribeId",
        "FilterExpression" to "id = :id"
    ) else json(
        "TableName" to prefixedTableName,
        "ExpressionAttributeValues" to json(
            ":id" to id
        ),
        "KeyConditionExpression" to "id = :id"
    )

    private fun singleScanParams(id: String, partyId: PartyId?) = if (partyId == null) json(
        "TableName" to prefixedTableName,
        "ExpressionAttributeValues" to json(
            ":id" to id
        ),
        "FilterExpression" to "id = :id"
    )
    else json(
        "TableName" to prefixedTableName,
        "ExpressionAttributeValues" to json(
            ":id" to id,
            ":tribeId" to partyId.value
        ),
        "FilterExpression" to "id = :id AND tribeId = :tribeId"
    )
}
