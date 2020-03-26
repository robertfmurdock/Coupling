@file:JsModule("monk")
@file:JsNonModule

package com.zegreatrob.coupling.mongo.external.monk

external val default: (String) -> MonkDb

external interface MonkDb {
    fun get(collection: String): MonkCollection
    fun close()
}

external interface MonkCollection {

}
