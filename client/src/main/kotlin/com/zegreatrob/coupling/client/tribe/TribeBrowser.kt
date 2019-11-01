package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.model.tribe.KtTribe
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.*

object TribeBrowser : RComponent<TribeBrowserProps>(provider()), TribeBrowserBuilder

val RBuilder.tribeBrowser get() = TribeBrowser.render(this)

data class TribeBrowserProps(val tribe: KtTribe, val pathSetter: (String) -> Unit) : RProps

interface TribeBrowserCss {
    val className: String
    val statisticsButton: String
    val tribeSelectButton: String
    val logoutButton: String
    val iconButtonText: String
}

interface TribeBrowserBuilder : StyledComponentRenderer<TribeBrowserProps, TribeBrowserCss> {

    override val componentPath: String get() = "tribe/TribeBrowser"

    override fun StyledRContext<TribeBrowserProps, TribeBrowserCss>.render(): ReactElement {
        val (tribe, pathSetter) = props

        return reactElement {
            div(classes = styles.className) {
                span {
                    tribeCard(TribeCardProps(tribe = tribe, pathSetter = pathSetter, size = 50))
                    h1 { +(tribe.name ?: "") }
                }
                tribeControlButtons(tribe, styles)
            }
        }
    }

    private fun RBuilder.tribeControlButtons(tribe: KtTribe, styles: TribeBrowserCss) {
        span {
            span {
                a(href = "/${tribe.id.value}/statistics", classes = "large gray button") {
                    attrs { classes += styles.statisticsButton }
                    span(classes = styles.iconButtonText) { +"Statistics" }
                }
                a(href = "/tribes/", classes = "large gray button") {
                    attrs { classes += styles.tribeSelectButton }
                    i(classes = "fa fa-arrow-circle-up") {}
                    span(classes = styles.iconButtonText) { +"Tribe select" }
                }
            }
            span {
                a(href = "/logout", classes = "large red button") {
                    attrs { classes += styles.logoutButton }
                    i(classes = "fa fa-sign-out") {}
                    span(classes = styles.iconButtonText) { +"Sign Out" }
                }
            }
        }
    }
}

