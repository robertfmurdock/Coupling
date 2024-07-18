package com.zegreatrob.coupling.repository.dynamo

import kotlin.js.Json

interface DynamoQueryItemListGetSyntax :
    DynamoDBSyntax,
    DynamoTableNameSyntax,
    DynamoQuerySyntax,
    ListLatestRecordSyntax {

    suspend fun queryForItemList(queryParams: Json, limited: Boolean = false) = queryAllRecords(queryParams, limited)
        .fullList()
        .filterNot(::isDeleted)

    suspend fun queryForDeletedItemList(queryParams: Json) = queryAllRecords(queryParams)
        .fullList()
        .filter(::isDeleted)
}
