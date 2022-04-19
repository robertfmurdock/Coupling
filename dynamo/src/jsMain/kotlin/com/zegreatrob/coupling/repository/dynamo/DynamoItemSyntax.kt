package com.zegreatrob.coupling.repository.dynamo

import kotlin.js.Json

interface DynamoItemSyntax : DynamoDatatypeSyntax {

    fun excludeDeleted(it: Json) = if (isDeleted(it)) null else it

    fun isDeleted(it: Json) = it.getDynamoBoolValue("isDeleted") == true

    fun Array<Json>.sortByRecordTimestamp() = sortedBy { it.getDynamoStringValue("timestamp") }
}
