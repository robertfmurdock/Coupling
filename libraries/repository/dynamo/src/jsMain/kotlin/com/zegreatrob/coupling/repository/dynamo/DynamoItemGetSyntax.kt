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
        queryAllRecords(queryParams(partyId, id))
            .sortByRecordTimestamp()
            .lastOrNull()
            ?.let(::excludeDeleted)
    }

    private fun queryParams(partyId: PartyId?, id: String) = if (partyId != null) {
        json(
            "TableName" to prefixedTableName,
            "ExpressionAttributeValues" to json(
                ":tribeId" to partyId.value.toString(),
                ":id" to id,
            ),
            "KeyConditionExpression" to "tribeId = :tribeId",
            "FilterExpression" to "id = :id",
        )
    } else {
        json(
            "TableName" to prefixedTableName,
            "ExpressionAttributeValues" to json(
                ":id" to id,
            ),
            "KeyConditionExpression" to "id = :id",
        )
    }
}
