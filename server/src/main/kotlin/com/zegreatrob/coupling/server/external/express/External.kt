package com.zegreatrob.coupling.server.external.express

@JsModule("express")
@JsNonModule
external fun express(): Express

external interface Express {
    fun use(middlewareFunction: Handler)
    fun set(key: String, value: dynamic)
    fun get(key: String): dynamic
    fun listen(port: Int, function: () -> Unit)
    fun get(path: String, function: Handler)
    fun all(path: String, function: Handler)
    fun use(path: String, router: Router)
    fun use(path: String, function: Handler)
    fun post(path: String, vararg handlers: Handler)
}

typealias Handler = (Request, Response, Next) -> Unit
typealias Next = () -> Unit
