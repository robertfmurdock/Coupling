package com.zegreatrob.coupling.repository.dynamo

import kotlin.js.Json

interface ListLatestRecordSyntax : DynamoItemSyntax {

    fun Json.fullList() = itemsNode()
        .sortByRecordTimestamp()
        .groupBy { it.getDynamoStringValue("id") }
        .map { it.value.last() }
}
