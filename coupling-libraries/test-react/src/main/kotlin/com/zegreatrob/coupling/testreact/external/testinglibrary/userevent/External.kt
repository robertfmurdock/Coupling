package com.zegreatrob.coupling.testreact.external.testinglibrary.userevent

import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.waitFor
import org.w3c.dom.HTMLElement
import kotlin.js.Json
import kotlin.js.Promise

@JsModule("@testing-library/user-event")
private external val userEvent: UserEventLib

class UserEvent(private val innerUser: TLUserEvent) {

    suspend fun click(element: HTMLElement?) = waitFor { innerUser.click(element) }
    suspend fun type(element: HTMLElement?, text: String) = waitFor { innerUser.type(element, text) }
    suspend fun selectOptions(element: HTMLElement, vararg value: String) = waitFor {
        innerUser.selectOptions(element, value)
    }

    companion object {
        fun setup() = UserEvent(userEvent.default.setup())
    }
}

external interface UserEventLib {
    val default: UserEventLib

    fun setup(json: Json = definedExternally): TLUserEvent
}

external interface TLUserEvent {
    fun click(element: HTMLElement?): Promise<Unit>
    fun type(element: HTMLElement?, text: String): Promise<Unit>
    fun selectOptions(element: HTMLElement, values: Array<out String>): Promise<Unit>
}
