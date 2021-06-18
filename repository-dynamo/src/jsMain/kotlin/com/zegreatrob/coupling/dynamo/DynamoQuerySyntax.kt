package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.dynamo.external.query
import kotlinx.coroutines.await
import kotlin.js.Json

interface DynamoQuerySyntax : DynamoDBSyntax {
    suspend fun performQuery(query: Json) = dynamoDBClient.query(query).await()
}
