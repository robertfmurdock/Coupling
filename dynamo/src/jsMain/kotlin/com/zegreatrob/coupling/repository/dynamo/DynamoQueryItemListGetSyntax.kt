package com.zegreatrob.coupling.repository.dynamo

import kotlin.js.Json

interface DynamoQueryItemListGetSyntax :
    DynamoDBSyntax,
    DynamoTableNameSyntax,
    DynamoQuerySyntax,
    ListLatestRecordSyntax {

    suspend fun queryForItemList(queryParams: Json) = queryAllRecords(queryParams).fullList().filterNot(::isDeleted)
    suspend fun queryForDeletedItemList(queryParams: Json) = queryAllRecords(queryParams).fullList().filter(::isDeleted)
}
