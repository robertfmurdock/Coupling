package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.CouplingLink
import com.zegreatrob.coupling.client.components.Editor
import com.zegreatrob.coupling.client.components.configInput
import com.zegreatrob.coupling.client.components.stats.CouplingInput
import com.zegreatrob.coupling.client.components.stats.CouplingSelect
import com.zegreatrob.coupling.client.components.white
import com.zegreatrob.coupling.client.components.yellow
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import js.objects.unsafeJso
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.option
import web.html.InputType
import web.html.text
import kotlin.test.AfterTest
import kotlin.test.Test

class ControlFocusTest {
    @AfterTest
    fun cleanUp() = cleanup()

    @Test
    fun editorFieldKeepsVisibleKeyboardFocus() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        render(
            Editor.create {
                li {
                    configInput("Name", "focus-name", "name", "", InputType.text, {})
                }
            },
        )
    } exercise {
        actor.tab().await()
    } verify {
        screen.getByRole("textbox", RoleOptions(name = "Name")).assertVisibleFocus()
    }

    @Test
    fun statisticsSelectKeepsVisibleKeyboardFocus() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        render(
            CouplingSelect.create {
                label = ReactNode("Time Window")
                selectProps = unsafeJso {}
                option { +"All" }
            },
        )
    } exercise {
        actor.tab().await()
    } verify {
        screen.getByRole("combobox", RoleOptions(name = "Time Window")).assertVisibleFocus()
    }

    @Test
    fun secretInputKeepsVisibleKeyboardFocus() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        render(
            CouplingInput.create {
                label = ReactNode("Secret name")
                inputProps = unsafeJso {}
            },
        )
    } exercise {
        actor.tab().await()
    } verify {
        screen.getByRole("textbox", RoleOptions(name = "Secret name")).assertVisibleFocus()
    }

    @Test
    fun whiteButtonKeepsVisibleKeyboardFocus() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        render(
            CouplingButton.create {
                colorRuleSet = white
                +"Save changes"
            },
        )
    } exercise {
        actor.tab().await()
    } verify {
        screen.getByRole("button", RoleOptions(name = "Save changes")).assertVisibleFocus()
    }

    @Test
    fun yellowLinkKeepsVisibleKeyboardFocus() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        renderNavigation {
            CouplingLink {
                to = "/parties"
                colorRuleSet = yellow
                +"Parties"
            }
        }
    } exercise {
        actor.tab().await()
    } verify {
        screen.getByRole("link", RoleOptions(name = "Parties")).assertVisibleFocus()
    }
}

private fun HTMLElement.assertVisibleFocus() {
    document.activeElement.assertIsEqualTo(this)
    val style = window.getComputedStyle(this)
    (style.outlineColor == "rgba(0, 0, 0, 0)").assertIsEqualTo(false)
    (style.outlineStyle == "none").assertIsEqualTo(false)
    (style.outlineWidth == "0px").assertIsEqualTo(false)
}
