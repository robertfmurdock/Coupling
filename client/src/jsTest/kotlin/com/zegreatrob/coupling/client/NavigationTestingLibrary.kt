@file:JsModule("@testing-library/react")

package com.zegreatrob.coupling.client

import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import org.w3c.dom.HTMLElement
import react.ReactNode

external fun cleanup()
external fun render(element: ReactNode)
external val screen: NavigationScreen

external interface NavigationScreen {
    fun getByRole(role: String, options: RoleOptions): HTMLElement
}
