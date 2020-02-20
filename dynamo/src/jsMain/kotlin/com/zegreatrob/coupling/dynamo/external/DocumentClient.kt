@file:JsModule("aws-sdk")
@file:JsNonModule
@file:JsQualifier("DynamoDB")

package com.zegreatrob.coupling.dynamo.external

import kotlin.js.Json

external class DocumentClient {
    fun put(params: Json): AwsPromisable<Unit>
    fun scan(params: Json): AwsPromisable<Json>
    fun query(params: Json): AwsPromisable<Json>
    fun batchGet(params: Json): AwsPromisable<Json>
}