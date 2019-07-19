package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.component
import com.zegreatrob.coupling.client.reactFunctionComponent
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import kotlinx.html.classes
import loadStyles
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

    companion object {

        private val styles = loadStyles<TribeBrowserCss>("tribe/TribeBrowser")

        private val tribeBrowser = reactFunctionComponent { props: TribeBrowserProps ->
            val (tribe, pathSetter) = props
            div(classes = styles.className) {
                span {
                    component(TribeCardRenderer.tribeCard, TribeCardProps(tribe = tribe, pathSetter = pathSetter, size = 50))
                    h1 { +(tribe.name ?: "") }
                }
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
}

