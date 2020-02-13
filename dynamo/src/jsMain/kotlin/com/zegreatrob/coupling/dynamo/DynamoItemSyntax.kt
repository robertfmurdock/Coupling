package com.zegreatrob.coupling.dynamo

import kotlin.js.Json

interface DynamoItemSyntax : DynamoDatatypeSyntax {
    fun excludeDeleted(it: Json) = if (it.getDynamoBoolValue("isDeleted") == true)
        null
    else
        it

    fun Array<Json>.sortByRecordTimestamp() = sortedBy { it.getDynamoStringValue("timestamp") }

}