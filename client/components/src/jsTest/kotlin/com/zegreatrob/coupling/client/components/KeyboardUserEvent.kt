@file:JsModule("@testing-library/user-event")

package com.zegreatrob.coupling.client.components

import kotlin.js.Promise

external val userEvent: KeyboardUserEventFactory

external interface KeyboardUserEventFactory {
    fun setup(): KeyboardUserEvent
}

external interface KeyboardUserEvent {
    fun tab(): Promise<Unit>
    fun keyboard(text: String): Promise<Unit>
}
