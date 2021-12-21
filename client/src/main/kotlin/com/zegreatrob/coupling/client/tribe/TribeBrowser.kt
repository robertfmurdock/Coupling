package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.configHeader
import com.zegreatrob.coupling.client.dom.couplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.red
import com.zegreatrob.coupling.client.dom.white
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.loadMarkdownString
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactmarkdown.markdown
import com.zegreatrob.coupling.client.external.reactpopup.popup
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.client.svgPath
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.sdk.EndpointFinder.gqlEndpoint
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import kotlinx.css.*
import react.RBuilder
import react.dom.*
import react.router.dom.Link
import styled.css
import styled.styledDiv
import styled.styledSpan
import kotlin.js.json

data class TribeBrowser(val tribe: Tribe) : DataProps<TribeBrowser> {
    override val component: TMFC<TribeBrowser> get() = tribeBrowser
}

private val styles = useStyles("tribe/TribeBrowser")

val tribeBrowser = reactFunction<TribeBrowser> { (tribe) ->
    div(styles.className) {
        configHeader(tribe) {
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
        backgroundColor = Color.darkCyan
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
    gqlButton()
    notificationSection()
}

private fun RBuilder.logoutButton() = Link {
    attrs.to = "/logout"
    couplingButton(large, red, styles["logoutButton"]) {
        i(classes = "fa fa-sign-out-alt") {}
        span { +"Sign Out" }
    }
}

private fun RBuilder.gqlButton() = a(href = gqlEndpoint) {
    couplingButton(large, white, styles["gqlButton"]) {
        img(src = svgPath("graphql")) {
            attrs {
                height = "18"
                width = "18"
            }
        }
    }
}

private fun RBuilder.tribeSelectButton() = Link {
    attrs.to = "/tribes/"
    couplingButton(large, className = styles["tribeSelectButton"]) {
        i(classes = "fa fa-arrow-circle-up") {}
        span { +"Tribe select" }
    }
}
