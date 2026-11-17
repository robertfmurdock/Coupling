@file:JsModule("@testing-library/user-event")

package com.zegreatrob.coupling.client

import kotlin.js.Promise

external val userEvent: KeyboardUserEventFactory

external interface KeyboardUserEventFactory {
    fun setup(): KeyboardUserEvent
}

external interface KeyboardUserEvent {
    fun tab(): Promise<Unit>
    fun keyboard(text: String): Promise<Unit>
}
