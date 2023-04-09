package com.zegreatrob.coupling.components.external.reactdndhtml5backend

import js.core.jso
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlin.js.Promise

external class DnDBackend

val reactDndHtml5BackendPromise = if (js("global.IS_JSDOM") == true) {
    Promise.resolve(jso<ReactDndHtml5BackendModule> {})
} else {
    js("import(\"react-dnd-html5-backend\")")
}

val html5BackendDeferred = MainScope().async {
    reactDndHtml5BackendPromise.unsafeCast<Promise<ReactDndHtml5BackendModule>>()
        .await()
}

external interface ReactDndHtml5BackendModule {
    val HTML5Backend: DnDBackend
}
