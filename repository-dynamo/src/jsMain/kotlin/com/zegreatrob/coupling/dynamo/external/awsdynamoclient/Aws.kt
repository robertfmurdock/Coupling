@file:JsModule("@aws-sdk/client-dynamodb")
@file:Suppress("unused")

package com.zegreatrob.coupling.dynamo.external.awsdynamoclient

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
