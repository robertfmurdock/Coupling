@file:JsModule("@testing-library/react")

package com.zegreatrob.coupling.testreact.external.testinglibrary.react

import org.w3c.dom.Element
import org.w3c.dom.HTMLCollection
import org.w3c.dom.HTMLElement
import react.ReactNode
import kotlin.js.Json
import kotlin.js.Promise

external val screen: Screen

external fun render(node: ReactNode, options: Json = definedExternally): Result

external val fireEvent: FireEvent

external class FireEvent {

    fun click(element: Element)
}

external class Result {
    fun getByText(s: String): HTMLElement
    val baseElement: HTMLElement
    val container: HTMLElement
}

external class Container {
    fun querySelector(selector: String): Element
    fun querySelectorAll(selector: String): HTMLCollection
}

external class Screen {
    fun getByText(s: String): HTMLElement
    fun queryByText(s: String): HTMLElement?
    fun queryAllByAltText(s: String): Array<HTMLElement>
}

external fun within(element: Element?): Screen

external fun waitFor(callback: () -> Any?): Promise<Unit>
