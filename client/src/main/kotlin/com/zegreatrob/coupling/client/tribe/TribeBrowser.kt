package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.ConfigHeader
import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.cssSpan
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.red
import com.zegreatrob.coupling.client.dom.white
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.loadMarkdownString
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactmarkdown.Markdown
import com.zegreatrob.coupling.client.external.reactpopup.popup
import com.zegreatrob.coupling.client.svgPath
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.sdk.EndpointFinder.gqlEndpoint
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.css.*
import react.ChildrenBuilder
import react.Fragment
import react.create
import react.dom.attrs
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.dom.i
import react.dom.img
import react.dom.span
import react.router.dom.Link
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
                tribeControlButtons()
            }
        }
    }
}

private fun ChildrenBuilder.notificationSection() = cssSpan(css = { position = Position.relative }) {
    cssSpan(css = {
        float = Float.left
        position = Position.absolute
        top = (-5).px
        right = (-80).px
    }) {
        +popupRecentInfo()
    }
}

private fun popupRecentInfo() = popup(
    trigger = { open -> Fragment.create { notificationButton(open) } },
    modal = true,
    on = arrayOf("click"),
    handler = {
        cssDiv(css = {
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
        }) {
            Markdown { +loadMarkdownString("recent-info") }
        }
    },
    contentStyle = json(
        "border-radius" to "30px",
        "border-color" to "black",
        "border-width" to "1px"
    )
)

private fun ChildrenBuilder.notificationButton(open: Boolean) = cssDiv(css = {
    backgroundColor = Color.darkCyan
    borderColor = Color.black
    color = if (open) Color.darkGray else Color.white
    borderRadius = 40.px
    height = 50.px
    width = 50.px
    textAlign = TextAlign.center
    verticalAlign = VerticalAlign.middle
}) {
    i { className = "fa fa-exclamation-circle" }
}


private fun ChildrenBuilder.tribeControlButtons() = span {
    className = styles["controlButtons"]
    tribeSelectButton()
    logoutButton()
    gqlButton()
    notificationSection()
}

private fun ChildrenBuilder.logoutButton() = Link {
    to = "/logout"
    child(CouplingButton(large, red, styles["logoutButton"]) {
        i(classes = "fa fa-sign-out-alt") {}
        span { +"Sign Out" }
    })
}

private fun ChildrenBuilder.gqlButton() = a {
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

private fun ChildrenBuilder.tribeSelectButton() = Link {
    to = "/tribes/"
    child(CouplingButton(large, className = styles["tribeSelectButton"]) {
        i(classes = "fa fa-arrow-circle-up") {}
        span { +"Tribe select" }
    })
}
