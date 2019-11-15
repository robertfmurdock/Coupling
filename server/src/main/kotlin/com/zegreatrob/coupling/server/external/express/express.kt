@file:JsModule("express")
@file:JsNonModule

package com.zegreatrob.coupling.server.external.express

external class Router(params: RouterParams) {
    fun route(path: String): Route
}

external interface Route {

    fun get(handler: (Request, Response) -> Unit): Route
    fun put(handler: (Request, Response) -> Unit): Route
    fun post(handler: (Request, Response) -> Unit): Route
    fun delete(handler: (Request, Response) -> Unit): Route

}

external interface RouterParams {
    val mergeParams: Boolean
}

