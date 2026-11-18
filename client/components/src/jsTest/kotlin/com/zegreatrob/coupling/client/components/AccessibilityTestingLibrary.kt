@file:JsModule("@testing-library/react")

package com.zegreatrob.coupling.client.components

import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement

@JsName("screen")
external val accessibilityScreen: AccessibilityScreen

external interface AccessibilityScreen {
    fun getByRole(role: String, options: RoleOptions): HTMLElement
    fun getByRole(role: String, options: DescribedRoleOptions): HTMLElement
}

@JsPlainObject
external interface DescribedRoleOptions {
    val name: String
    val description: String
}
