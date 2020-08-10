@file:JsModule("@wdio/cli")

package com.zegreatrob.coupling.e2e.external.wdio

import kotlin.js.Json
import kotlin.js.Promise

@JsName("default")
external class Launcher(configPath: String, options: Json) {
    fun run(): Promise<Int>
}