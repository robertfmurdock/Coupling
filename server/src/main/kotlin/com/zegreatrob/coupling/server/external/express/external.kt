package com.zegreatrob.coupling.server.external.express

import com.zegreatrob.coupling.server.route.WS

@JsModule("express")
@JsNonModule
external fun express(): Express

external interface Express {
    fun use(middlewareFunction: dynamic)
    fun set(key: String, value: dynamic)
    fun get(key: String): dynamic
    fun listen(port: Int, function: () -> Unit)
    fun get(path: String, function: Handler)
    fun all(path: String, function: Handler)
    fun use(path: String, router: Router)
    fun post(path: String, vararg handlers: Handler)
    fun ws(path: String, handler: WSHandler)
}

typealias Handler = (Request, Response, Next) -> Unit
typealias WSHandler = (WS, Request) -> Unit
typealias Next = () -> Unit