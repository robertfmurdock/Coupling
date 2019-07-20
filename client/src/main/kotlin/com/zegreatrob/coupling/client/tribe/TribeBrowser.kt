package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.component
import com.zegreatrob.coupling.client.styledComponent
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.dom.*

data class TribeBrowserProps(val tribe: KtTribe, val pathSetter: (String) -> Unit) : RProps

interface TribeBrowserCss {
    val className: String
    val statisticsButton: String
    val tribeSelectButton: String
    val logoutButton: String
}

interface TribeBrowserRenderer {

    fun RBuilder.tribeBrowser(props: TribeBrowserProps) = component(tribeBrowser, props)

    companion object : TribeCardRenderer {

        private val tribeBrowser = styledComponent(
                "tribe/TribeBrowser"
        ) { props: TribeBrowserProps, styles: TribeBrowserCss ->
            val (tribe, pathSetter) = props
            div(classes = styles.className) {
                span {
                    tribeCard(TribeCardProps(tribe = tribe, pathSetter = pathSetter, size = 50))
                    h1 { +(tribe.name ?: "") }
                }
                tribeControlButtons(tribe, styles)
            }
        }

        private fun RBuilder.tribeControlButtons(tribe: KtTribe, styles: TribeBrowserCss) {
            span {
                span {
                    a(href = "/${tribe.id.value}/statistics", classes = "large gray button") {
                        attrs { classes += styles.statisticsButton }
                        span(classes = "icon-button-text") { +"Statistics" }
                    }
                    a(href = "/tribes/", classes = "large gray button") {
                        attrs { classes += styles.tribeSelectButton }
                        i(classes = "fa fa-arrow-circle-up") {}
                        span(classes = "icon-button-text") { +"Tribe select" }
                    }
                }
                span {
                    a(href = "/logout", classes = "large red button") {
                        attrs { classes += styles.logoutButton }
                        i(classes = "fa fa-sign-out") {}
                        span(classes = "icon-button-text") { +"Sign Out" }
                    }
                }
            }
        }
    }
}

