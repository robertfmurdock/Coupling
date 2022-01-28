@file:JsModule("@testing-library/user-event")

package com.zegreatrob.coupling.client.external.testinglibrary.userevent

import org.w3c.dom.HTMLElement

@JsName("default")
external val userEvent: UserEvent

external class UserEvent {
    fun click(element: HTMLElement)
}