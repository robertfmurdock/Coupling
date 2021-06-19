@file:JsModule("@aws-sdk/client-lambda")

package com.zegreatrob.coupling.server.external.awssdk.clientlambda

import kotlin.js.Json
import kotlin.js.Promise

external class LambdaClient(options: Json) {
    fun <T> send(command: dynamic): Promise<T>
}

external class InvokeAsyncCommand(input: Json )
external class InvokeCommand(input: Json )

external class Lambda(options: Json) {
    fun invoke(args: Json) : Promise<Json>
}
