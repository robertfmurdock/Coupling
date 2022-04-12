@file:JsModule("@aws-sdk/client-dynamodb")
@file:Suppress("unused")

package com.zegreatrob.coupling.repository.dynamo.external.awsdynamoclient

import kotlin.js.Json
import kotlin.js.Promise

external class DynamoDB(options: Json) {

    fun describeTable(params: Json): Promise<Json>

    fun listTables(): Promise<dynamic>

    fun query(params: Json): Promise<Json>
    fun scan(params: Json): Promise<Json>

    fun putItem(itemParams: Json): Promise<dynamic>
    fun createTable(json: Json): Promise<dynamic>

}

external class DynamoDBClient(options: Json) {
    fun send(command: DynamoCommand):Promise<ScanCommandOutput>
}

external interface DynamoCommand

external class ScanCommand(input : ScanCommandInput) : DynamoCommand

external interface ScanCommandInput {
    var TableName: String
    var IndexName: String?
    var ProjectionExpression: String
}
external interface ScanCommandOutput {
    val Count: Int
    val Items: Json
}