package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.red
import com.zegreatrob.coupling.client.dom.white
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.loadMarkdownString
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactmarkdown.Markdown
import com.zegreatrob.coupling.client.external.reactpopup.popup
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.sdk.EndpointFinder.gqlEndpoint
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.browser.localStorage
import kotlinx.css.*
import kotlinx.css.properties.IterationCount
import kotlinx.css.properties.animation
import kotlinx.css.properties.s
import react.ChildrenBuilder
import react.RBuilder
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.span
import react.router.dom.Link
import react.useState
import styled.styledDiv
import kotlin.js.json

data class TribeBrowser(val tribe: Tribe) : DataProps<TribeBrowser> {
    override val component: TMFC<TribeBrowser> get() = tribeBrowser
}

private val styles = useStyles("tribe/TribeBrowser")

val tribeBrowser = tmFC<TribeBrowser> { (tribe) ->
    val recentInfoMd = loadMarkdownString("recent-info")
    val (seenNotification, setSeenNotification) = useState {
        loadNotificationLog().contains(recentInfoMd.dateLineFromRecentInfo())
    }
    val onPopupClose = {
        saveNotificationLog(loadNotificationLog() + recentInfoMd.dateLineFromRecentInfo())
        setSeenNotification(true)
    }

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
                tribeControlButtons(seenNotification, recentInfoMd, onPopupClose)
            }
        }
    }
}

private fun saveNotificationLog(updatedThing: Array<String>) {
    localStorage.setItem("notification-log", JSON.stringify(updatedThing))
}

private fun loadNotificationLog() =
    localStorage.getItem("notification-log")?.let { JSON.parse<Array<String>>(it) } ?: emptyArray()

private fun ChildrenBuilder.notificationSection(
    seenNotification: Boolean,
    recentInfoMd: String,
    onPopupClose: () -> Unit
) = cssSpan(css = { position = Position.relative }) {
    cssSpan(css = {
        float = Float.left
        position = Position.absolute
        top = (-5).px
        right = (-80).px
    }) {
        +popupRecentInfo(seenNotification, recentInfoMd, onPopupClose)
    }
}

private fun popupRecentInfo(seenNotification: Boolean, recentInfoMd: String, onClose: () -> Unit) = popup(
    trigger = { open -> notificationButton(open, seenNotification) },
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
            Markdown { +recentInfoMd }
        }
    },
    contentStyle = json(
        "borderRadius" to "30px",
        "borderColor" to "black",
        "borderWidth" to "1px"
    ),
    onClose = onClose
)

private fun String.dateLineFromRecentInfo() = substring(indexOf("##"))
    .let { it.substring(0, it.indexOf("\n")) }
    .trim()

private fun notificationButton(open: Boolean, seenNotification: Boolean) = bridge(
    RBuilder::styledDiv,
    {},
    {},
    css = {
        val buttonSize = if (seenNotification) 50.px else 75.px
        backgroundColor = if (seenNotification) Color.darkCyan else Color.crimson
        if (!seenNotification) {
            animation("pulsate", 0.75.s, iterationCount = IterationCount.infinite)
        }
        color = if (open) Color.darkGray else Color.white
        height = buttonSize
        width = buttonSize
        display = Display.flex;
        borderColor = Color.black
        borderRadius = 40.px
        textAlign = TextAlign.center
        verticalAlign = VerticalAlign.middle
        justifyContent = JustifyContent.center
        alignItems = Align.center
    }) {
    i { className = "fa fa-exclamation-circle ${if (seenNotification) "" else "fa-2x"}" }
}

private fun ChildrenBuilder.tribeControlButtons(seenNotification: Boolean, recentInfoMd: String, onClose: () -> Unit) =
    span {
        className = styles["controlButtons"]
        tribeSelectButton()
        logoutButton()
        gqlButton()
        notificationSection(seenNotification, recentInfoMd, onClose)
    }

private fun ChildrenBuilder.logoutButton() = Link {
    to = "/logout"
    child(CouplingButton(large, red, styles["logoutButton"])) {
        i { className = "fa fa-sign-out-alt" }
        span { +"Sign Out" }
    }
}

private fun ChildrenBuilder.gqlButton() = a {
    href = gqlEndpoint
    child(CouplingButton(large, white, styles["gqlButton"])) {
        img {
            src = svgPath("graphql")
            height = 18.0
            width = 18.0
        }
    }
}

private fun ChildrenBuilder.tribeSelectButton() = Link {
    to = "/tribes/"
    child(CouplingButton(large, className = styles["tribeSelectButton"])) {
        i { className = "fa fa-arrow-circle-up" }
        span { +"Tribe select" }
    }
}
