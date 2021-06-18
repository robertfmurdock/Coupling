package com.zegreatrob.coupling.dynamo.external

import kotlin.js.Promise

external interface AwsPromisable<T> {
    fun promise(): Promise<T>
}
