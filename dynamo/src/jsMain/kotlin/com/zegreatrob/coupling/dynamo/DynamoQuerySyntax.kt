package com.zegreatrob.coupling.dynamo

import kotlinx.coroutines.await
import kotlin.js.Json

interface DynamoQuerySyntax : DynamoDBSyntax {
    suspend fun performQuery(query: Json) = documentClient.query(query).promise().await()
}