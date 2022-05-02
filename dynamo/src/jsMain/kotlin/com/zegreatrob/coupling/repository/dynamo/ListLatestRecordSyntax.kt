package com.zegreatrob.coupling.repository.dynamo

import kotlin.js.Json

interface ListLatestRecordSyntax : DynamoItemSyntax {

    fun Array<Json>.fullList() = sortByRecordTimestamp()
        .groupBy { it.getDynamoStringValue("id") }
        .map { it.value.last() }
}
