package com.zegreatrob.coupling.dynamo

import kotlinx.coroutines.await
import kotlin.js.Json

interface DynamoScanSyntax : DynamoDBSyntax {
    suspend fun performScan(scanParams: Json) = documentClient.scan(scanParams).promise().await()
}