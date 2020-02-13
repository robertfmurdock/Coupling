package com.zegreatrob.coupling.dynamo

import kotlin.js.json

interface DynamoItemGetSyntax : DynamoQuerySyntax,
    DynamoDatatypeSyntax,
    DynamoItemSyntax,
    DynamoTableNameSyntax {

    suspend fun performGetSingleItemQuery(id: String) = performQuery(singleQuery(id))
        .itemsNode()
        .sortByRecordTimestamp()
        .lastOrNull()
        ?.let(::excludeDeleted)

    private fun singleQuery(id: String) = json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(":id" to json("S" to id)),
        "KeyConditionExpression" to "id = :id"
    )

}