package com.zegreatrob.coupling.dynamo

import kotlin.js.Json

interface DynamoQueryItemListGetSyntax : DynamoDBSyntax, DynamoTableNameSyntax, DynamoQuerySyntax,
    ListLatestRecordSyntax {

    suspend fun queryForItemList(queryParams: Json) = performQuery(queryParams).fullList().filterNot(::isDeleted)
    suspend fun queryForDeletedItemList(queryParams: Json) = performQuery(queryParams).fullList().filter(::isDeleted)

}
