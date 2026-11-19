package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.Editor
import com.zegreatrob.coupling.client.components.configInput
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import react.create
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.span
import web.html.InputType
import web.html.text
import kotlin.math.pow
import kotlin.test.AfterTest
import kotlin.test.Test

class EditorContrastTest {
    @AfterTest
    fun cleanUp() = cleanup()

    @Test
    fun labelsAndHintsMeetNormalTextContrast() = setup() exercise {
        render(
            Editor.create {
                li {
                    configInput("Name", "contrast-name", "name", "", InputType.text, {})
                    span { +"The full party name!" }
                }
            },
        )
        listOf("Name", "The full party name!").associateWith { text ->
            screen.getByText(text).textContrast()
        }
    } verify { ratios ->
        ratios.forEach { (text, ratio) ->
            (ratio >= 4.5).assertIsEqualTo(true, "$text contrast is $ratio; normal text requires at least 4.5:1. Rendered ratios: $ratios")
        }
    }
}

private fun HTMLElement.textContrast(): Double {
    val style = window.getComputedStyle(this)
    val foreground = style.color.relativeLuminance()
    val background = style.backgroundColor.relativeLuminance()
    return (maxOf(foreground, background) + 0.05) / (minOf(foreground, background) + 0.05)
}

private fun String.relativeLuminance(): Double {
    val channels = removePrefix("rgb(").removeSuffix(")").split(",").map { it.trim().toDouble() / 255 }
    val linear = channels.map { if (it <= 0.04045) it / 12.92 else ((it + 0.055) / 1.055).pow(2.4) }
    return 0.2126 * linear[0] + 0.7152 * linear[1] + 0.0722 * linear[2]
}
