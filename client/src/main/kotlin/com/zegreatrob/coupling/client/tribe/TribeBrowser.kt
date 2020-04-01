package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.ConfigHeader.configHeader
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactmarkdown.markdown
import com.zegreatrob.coupling.client.external.reactpopup.popup
import com.zegreatrob.coupling.model.tribe.Tribe
import kotlinx.css.*
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.dom.a
import react.dom.div
import react.dom.i
import react.dom.span
import styled.css
import styled.styledDiv
import styled.styledSpan
import kotlin.js.json

data class TribeBrowserProps(val tribe: Tribe, val pathSetter: (String) -> Unit) : RProps

object TribeBrowser : FRComponent<TribeBrowserProps>(provider()) {

    fun RBuilder.tribeBrowser(tribe: Tribe, pathSetter: (String) -> Unit) =
        render(this)(TribeBrowserProps(tribe, pathSetter))

    val styles = useStyles("tribe/TribeBrowser")

    override fun render(props: TribeBrowserProps) = reactElement {
        val (tribe, pathSetter) = props

        div(styles.className) {
            configHeader(tribe, pathSetter) {
                span(styles["headerContents"]) {
                    span(styles["headerText"]) { +(tribe.name ?: "") }
                    tribeControlButtons()
                }
            }
        }
    }

    private fun RBuilder.notificationSection() = styledSpan {
        css { position = Position.relative }
        styledSpan {
            css { float = Float.left; position = Position.absolute; top = (-5).px; right = (-80).px }
            popup(
                trigger = { open -> notificationButton(open) },
                modal = true,
                on = arrayOf("click"),
                handler = {
                    styledDiv {
                        css {
                            fontSize = 14.px
                            fontWeight = FontWeight.normal
                            verticalAlign = VerticalAlign.baseline
                            borderRadius = 20.px
                            "*" {
                                verticalAlign = VerticalAlign.baseline
                            }
                            marginLeft = 15.px
                            marginRight = 15.px
                            marginBottom = 15.px
                        }
                        markdown(loadMarkdownString("recent-info"))
                    }
                },
                contentStyle = json(
                    "border-radius" to "30px",
                    "border-color" to "black",
                    "border-width" to "1px"
                )
            )

        }
    }

    private fun RBuilder.notificationButton(open: Boolean) = styledDiv {
        css {
            backgroundColor = Color.black
            borderColor = Color.black
            color = if (open) Color.darkGray else Color.white
            borderRadius = 40.px
            height = 50.px
            width = 50.px
            textAlign = TextAlign.center
            verticalAlign = VerticalAlign.middle
        }
        i(classes = "fa fa-exclamation-circle") {}
    }

    private fun RBuilder.tribeControlButtons() = span(classes = styles["controlButtons"]) {
        tribeSelectButton()
        logoutButton()
        notificationSection()
    }

    private fun RBuilder.logoutButton() = a(href = "/logout", classes = "large red button") {
        attrs { classes += styles["logoutButton"] }
        i(classes = "fa fa-sign-out-alt") {}
        span { +"Sign Out" }
    }

    private fun RBuilder.tribeSelectButton() = a(href = "/tribes/", classes = "large gray button") {
        attrs { classes += styles["tribeSelectButton"] }
        i(classes = "fa fa-arrow-circle-up") {}
        span { +"Tribe select" }
    }

}

