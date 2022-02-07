@file:JsModule("aws-sdk")
@file:Suppress("unused")

package com.zegreatrob.coupling.dynamo.external.awsgatewaymanagement

import com.zegreatrob.coupling.dynamo.external.AwsPromisable
import kotlin.js.Json

external class ApiGatewayManagementApi(option: Json) {
    fun postToConnection(json: Json): AwsPromisable<Json>
    fun deleteConnection(json: Json): AwsPromisable<Json>
}
