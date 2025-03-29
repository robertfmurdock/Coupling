@file:JsModule("express")
@file:Suppress("unused")

package com.zegreatrob.coupling.server.external.express

import kotlin.js.Json

external class Router(params: RouterParams) {
    fun route(path: String): Route
    fun use(path: String, router: Router)
}

external interface Route {
    fun all(handler: Handler): Route
    fun get(handler: (Request, Response) -> Unit): Route
    fun put(handler: (Request, Response) -> Unit): Route
    fun post(handler: (Request, Response) -> Unit): Route
    fun delete(handler: (Request, Response) -> Unit): Route
}

sealed external interface RouterParams {
    val mergeParams: Boolean?
}

external fun static(path: String, options: Json): Handler
external fun raw(json: Json): Handler

external fun json(): Handler
external fun urlencoded(config: Json): Handler
