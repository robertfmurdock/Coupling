package com.zegreatrob.coupling.dynamo

import kotlinx.coroutines.await
import kotlin.js.Json

interface DynamoQuerySyntax : DynamoDBSyntax {
    suspend fun performQuery(query: Json) = dynamoDB.query(query).promise().await()
}