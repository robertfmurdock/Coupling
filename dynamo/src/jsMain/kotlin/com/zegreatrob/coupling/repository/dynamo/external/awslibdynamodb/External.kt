@file:JsModule("@aws-sdk/lib-dynamodb")
package com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb

import com.zegreatrob.coupling.repository.dynamo.external.DynamoDBClient
import kotlin.js.Json
import kotlin.js.Promise

external object DynamoDBDocumentClient {
    fun from(also: DynamoDBClient) : DynamoDBDocumentClient
    fun <T> send(command: dynamic, options: Json = definedExternally): Promise<T>
}

external class PutCommand(options: Json)
external class ScanCommand(options: Json)
external class QueryCommand(options: Json)
external class BatchGetCommand(options: Json)
external class DeleteCommand(options: Json)
