@file:JsModule("node:crypto")

package com.zegreatrob.coupling.repository.dynamo.external

external fun createHash(algorithm: String): NodeHash

external interface NodeHash {
    fun update(value: String): NodeHash
    fun digest(encoding: String): String
}
