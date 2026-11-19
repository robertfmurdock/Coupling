@file:JsModule("@testing-library/user-event")

package com.zegreatrob.coupling.client.components

import org.w3c.dom.HTMLElement
import kotlin.js.Promise

external val userEvent: KeyboardUserEventFactory

external interface KeyboardUserEventFactory {
    fun setup(): KeyboardUserEvent
}

external interface KeyboardUserEvent {
    fun click(element: HTMLElement): Promise<Unit>
    fun tab(): Promise<Unit>
    fun keyboard(text: String): Promise<Unit>
}
