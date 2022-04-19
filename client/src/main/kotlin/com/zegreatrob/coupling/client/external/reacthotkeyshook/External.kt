@file:JsModule("react-hotkeys-hook")
@file:Suppress("unused")

package com.zegreatrob.coupling.client.external.reacthotkeyshook

import kotlin.js.Json

external fun useHotkeys(
    key: String,
    onPress: (keyboardEvent: dynamic, handler: dynamic) -> Unit,
    options: Json = definedExternally
)

external fun useIsHotkeyPressed(): (key: String) -> Boolean
