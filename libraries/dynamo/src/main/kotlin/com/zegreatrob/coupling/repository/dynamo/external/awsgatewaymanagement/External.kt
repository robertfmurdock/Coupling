@file:JsModule("@aws-sdk/client-apigatewaymanagementapi")

package com.zegreatrob.coupling.repository.dynamo.external.awsgatewaymanagement

import kotlin.js.Json
import kotlin.js.Promise

external class ApiGatewayManagementApiClient(options: Json) {

    fun send(command: Any): Promise<Json>
}

external class PostToConnectionCommand(input: Json)

external class DeleteConnectionCommand(input: Json)
