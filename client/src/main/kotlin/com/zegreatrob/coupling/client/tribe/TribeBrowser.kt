package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.ConfigHeader
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.red
import com.zegreatrob.coupling.client.dom.white
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.loadMarkdownString
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactmarkdown.markdown
import com.zegreatrob.coupling.client.external.reactpopup.popup
import com.zegreatrob.coupling.client.svgPath
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.sdk.EndpointFinder.gqlEndpoint
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.css.*
import react.buildElement
import react.create
import react.dom.attrs
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.dom.i
import react.dom.img
import react.dom.span
import react.router.dom.Link
import styled.css
import styled.styledDiv
import styled.styledSpan
import kotlin.js.json

data class TribeBrowser(val tribe: Tribe) : DataProps<TribeBrowser> {
    override val component: TMFC<TribeBrowser> get() = tribeBrowser
}

private val styles = useStyles("tribe/TribeBrowser")

val tribeBrowser = tmFC<TribeBrowser> { (tribe) ->
    div {
        className = styles.className
        ConfigHeader {
            this.tribe = tribe
            span {
                className = styles["headerContents"]
                span {
                    className = styles["headerText"]
                    +(tribe.name ?: "")
                }
                child(tribeControlButtons())
            }
        }
    }
}

private fun notificationSection() = buildElement {
    styledSpan {
        css { position = Position.relative }
        styledSpan {
            css { float = Float.left; position = Position.absolute; top = (-5).px; right = (-80).px }
            child(popupRecentInfo())
        }
    }
}

private fun popupRecentInfo() = popup(
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

private fun notificationButton(open: Boolean) = buildElement {
    styledDiv {
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
}

private fun tribeControlButtons() = span.create {
    className = styles["controlButtons"]
    tribeSelectButton()
    logoutButton()
    gqlButton()
    notificationSection()
}

private fun logoutButton() = Link.create {
    to = "/logout"
    child(CouplingButton(large, red, styles["logoutButton"]) {
        i(classes = "fa fa-sign-out-alt") {}
        span { +"Sign Out" }
    })
}

private fun gqlButton() = a.create {
    href = gqlEndpoint
    child(CouplingButton(large, white, styles["gqlButton"]) {
        img(src = svgPath("graphql")) {
            attrs {
                height = "18"
                width = "18"
            }
        }
    })
}

private fun tribeSelectButton() = Link.create {
    to = "/tribes/"
    child(CouplingButton(large, className = styles["tribeSelectButton"]) {
        i(classes = "fa fa-arrow-circle-up") {}
        span { +"Tribe select" }
    })
}
