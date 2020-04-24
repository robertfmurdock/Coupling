@file:JsModule("express")
@file:JsNonModule

package com.zegreatrob.coupling.server.external.express

import kotlin.js.Json

external class Router(params: RouterParams) {
    fun route(path: String): Route
    fun use(path: String, router: Router)
}

external interface Route {
    fun all(handler: (Request, Response, () -> Unit) -> Unit): Route
    fun get(handler: (Request, Response) -> Unit): Route
    fun put(handler: (Request, Response) -> Unit): Route
    fun post(handler: (Request, Response) -> Unit): Route
    fun delete(handler: (Request, Response) -> Unit): Route
}

external interface RouterParams {
    val mergeParams: Boolean
}

external fun static(path: String, options: Json): Handler