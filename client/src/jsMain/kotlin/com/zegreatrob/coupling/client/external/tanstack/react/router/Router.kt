@file:JsModule("@tanstack/react-router")

package com.zegreatrob.coupling.client.external.tanstack.react.router

import tanstack.router.core.BuildLocationOptions
import tanstack.router.core.ParsedLocation

external class Router {
    fun buildLocation(options: BuildLocationOptions): ParsedLocation
}

external fun redirect(options: BuildLocationOptions): Throwable
