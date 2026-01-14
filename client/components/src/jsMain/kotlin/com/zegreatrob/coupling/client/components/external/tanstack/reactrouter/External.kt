@file:JsModule("@tanstack/react-router")
package com.zegreatrob.coupling.client.components.external.tanstack.reactrouter

import kotlinx.js.JsPlainObject
import tanstack.router.core.UseNavigateResult

@JsPlainObject
external interface UseBlockerOptions {
    val shouldBlockFn: () -> Boolean
}

external fun useBlocker(
    options: UseBlockerOptions = definedExternally,
): UseNavigateResult
