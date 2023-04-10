package com.zegreatrob.coupling.testreact.external.testinglibrary.react

import org.w3c.dom.Element
import org.w3c.dom.HTMLCollection
import org.w3c.dom.HTMLElement
import react.ReactNode
import kotlin.js.Json
import kotlin.js.Promise

@JsModule("@testing-library/react")
external val testingLibraryReact: TestingLibraryReact

external interface TestingLibraryReact {
    val screen: Screen
    fun render(node: ReactNode, options: Json = definedExternally): Result
    val fireEvent: FireEvent
    fun act(block: () -> Unit)
    fun within(element: Element?): Screen
    fun waitFor(callback: () -> Any?): Promise<Unit>
}

external class FireEvent {
    fun click(element: Element)
    fun submit(element: Element)
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
    fun getByLabelText(s: String): HTMLElement
    fun queryByText(s: String): HTMLElement?
    fun queryByLabelText(s: String): HTMLElement?
    fun queryAllByAltText(s: String): Array<HTMLElement>
    fun getByRole(role: String, options: Json = definedExternally): HTMLElement
    fun findByText(text: String): Promise<HTMLElement>
}
