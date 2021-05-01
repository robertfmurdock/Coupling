@file:JsModule("aws-sdk")

@file:JsQualifier("DynamoDB")
@file:Suppress("unused")

package com.zegreatrob.coupling.dynamo.external

import kotlin.js.Json

external class DocumentClient(json: Json?) {
    fun put(params: Json): AwsPromisable<Unit>
    fun scan(params: Json): AwsPromisable<Json>
    fun query(params: Json): AwsPromisable<Json>
    fun batchGet(params: Json): AwsPromisable<Json>
}