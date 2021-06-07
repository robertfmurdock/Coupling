@file:JsModule("aws-sdk")
@file:Suppress("unused")


package com.zegreatrob.coupling.dynamo.external

import kotlin.js.Json
import kotlin.js.Promise


external val config: AwsConfig

external interface AwsConfig {
    fun update(data: Json)
}

external class DynamoDB {

    fun describeTable(params: Json): AwsPromisable<Json>

    fun listTables(): AwsPromisable<dynamic>

    fun query(params: Json): AwsPromisable<Json>
    fun scan(params: Json): AwsPromisable<Json>

    fun putItem(itemParams: Json): AwsPromisable<dynamic>
    fun createTable(json: Json): AwsPromisable<dynamic>

}


external interface AwsPromisable<T> {
    fun promise(): Promise<T>
}

external class ApiGatewayManagementApi(option: Json) {
    fun postToConnection(json: Json): AwsPromisable<Json>
    fun deleteConnection(json: Json): AwsPromisable<Json>
}
