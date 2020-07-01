@file:JsModule("monk")
@file:JsNonModule

package com.zegreatrob.coupling.mongo.external.monk

import kotlin.js.Json

external val default: (String) -> MonkDb

external interface MonkDb {
    fun get(collection: String): MonkCollection
    fun close()
}

external interface MonkCollection {
    fun createIndex(config: Json)
}
