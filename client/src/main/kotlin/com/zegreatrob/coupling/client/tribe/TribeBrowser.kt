package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.ConfigHeader.configHeader
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.model.tribe.Tribe
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.dom.a
import react.dom.div
import react.dom.i
import react.dom.span

object TribeBrowser : RComponent<TribeBrowserProps>(provider()), TribeBrowserBuilder

val RBuilder.tribeBrowser get() = TribeBrowser.render(this)

data class TribeBrowserProps(val tribe: Tribe, val pathSetter: (String) -> Unit) : RProps

interface TribeBrowserCss {
    val className: String
    val tribeSelectButton: String
    val logoutButton: String
    val iconButtonText: String
    val controlButtons: String
    val headerContents: String
    val headerText: String
}

interface TribeBrowserBuilder : StyledComponentRenderer<TribeBrowserProps, TribeBrowserCss> {

    override val componentPath: String get() = "tribe/TribeBrowser"

    override fun StyledRContext<TribeBrowserProps, TribeBrowserCss>.render() = reactElement {
        val (tribe, pathSetter) = props
        div(classes = styles.className) {
            configHeader(tribe, pathSetter) {
                span(styles.headerContents) {
                    span(styles.headerText) { +(tribe.name ?: "") }
                    tribeControlButtons(styles)
                }
            }
        }
    }

    private fun RBuilder.tribeControlButtons(styles: TribeBrowserCss) {
        span(classes = styles.controlButtons) {
            span {
                tribeSelectButton(styles.tribeSelectButton, styles.iconButtonText)
            }
            span {
                logoutButton(styles.logoutButton, styles.iconButtonText)
            }
        }
    }

    private fun RBuilder.logoutButton(className: String, iconButtonTextClassName: String) =
        a(href = "/logout", classes = "large red button") {
            attrs { classes += className }
            i(classes = "fa fa-sign-out") {}
            span(classes = iconButtonTextClassName) { +"Sign Out" }
        }

    private fun RBuilder.tribeSelectButton(className: String, iconButtonClassName: String) =
        a(href = "/tribes/", classes = "large gray button") {
            attrs { classes += className }
            i(classes = "fa fa-arrow-circle-up") {}
            span(classes = iconButtonClassName) { +"Tribe select" }
        }

}

