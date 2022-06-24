@file:JsModule("@testing-library/user-event")

package com.zegreatrob.coupling.testreact.external.testinglibrary.userevent

import org.w3c.dom.HTMLElement
import kotlin.js.Json
import kotlin.js.Promise

@JsName("default")
external val userEvent: UserEventLib

external interface UserEventLib {
    fun setup(json: Json = definedExternally): UserEvent
}

external class UserEvent {
    fun click(element: HTMLElement?): Promise<Unit>
}
