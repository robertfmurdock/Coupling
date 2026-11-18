@file:JsModule("@testing-library/react")

package com.zegreatrob.coupling.client

import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import react.ReactNode

external fun cleanup()
external fun render(element: ReactNode)
external fun waitFor(callback: () -> Unit): kotlin.js.Promise<Unit>
external val screen: NavigationScreen

external interface NavigationScreen {
    fun getByText(text: String): HTMLElement
    fun getByRole(role: String, options: RoleOptions): HTMLElement
    fun getByRole(role: String, options: DescribedRoleOptions): HTMLElement
}

@JsPlainObject
external interface DescribedRoleOptions {
    val name: String
    val description: String
}
