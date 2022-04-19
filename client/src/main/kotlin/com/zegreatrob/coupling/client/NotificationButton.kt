package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.loadMarkdownString
import com.zegreatrob.coupling.client.external.reactmarkdown.Markdown
import com.zegreatrob.coupling.client.external.reactpopup.popup
import csstype.ClassName
import kotlinx.browser.localStorage
import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.Float
import kotlinx.css.FontWeight
import kotlinx.css.JustifyContent
import kotlinx.css.Position
import kotlinx.css.TextAlign
import kotlinx.css.VerticalAlign
import kotlinx.css.alignItems
import kotlinx.css.backgroundColor
import kotlinx.css.borderColor
import kotlinx.css.borderRadius
import kotlinx.css.color
import kotlinx.css.display
import kotlinx.css.float
import kotlinx.css.fontSize
import kotlinx.css.fontWeight
import kotlinx.css.height
import kotlinx.css.justifyContent
import kotlinx.css.marginBottom
import kotlinx.css.marginLeft
import kotlinx.css.marginRight
import kotlinx.css.position
import kotlinx.css.properties.IterationCount
import kotlinx.css.properties.animation
import kotlinx.css.properties.s
import kotlinx.css.px
import kotlinx.css.right
import kotlinx.css.textAlign
import kotlinx.css.top
import kotlinx.css.verticalAlign
import kotlinx.css.width
import react.FC
import react.Props
import react.RBuilder
import react.dom.html.ReactHTML.i
import react.useState
import styled.styledDiv
import kotlin.js.json

private fun saveNotificationLog(updatedThing: Array<String>) {
    localStorage.setItem("notification-log", JSON.stringify(updatedThing))
}

private fun loadNotificationLog() =
    localStorage.getItem("notification-log")?.let { JSON.parse<Array<String>>(it) } ?: emptyArray()

val NotificationButton = FC<Props> {
    val recentInfoMd = loadMarkdownString("recent-info")
    val (seenNotification, setSeenNotification) = useState {
        loadNotificationLog().contains(recentInfoMd.dateLineFromRecentInfo())
    }
    val onPopupClose = {
        saveNotificationLog(loadNotificationLog() + recentInfoMd.dateLineFromRecentInfo())
        setSeenNotification(true)
    }
    cssSpan(css = { position = Position.relative }) {
        cssSpan(css = {
            float = Float.left
            position = Position.absolute
            top = (-5).px
            right = (-80).px
        }) {
            +popupRecentInfo(seenNotification, recentInfoMd, onPopupClose)
        }
    }
}

private fun String.dateLineFromRecentInfo() = substring(indexOf("##"))
    .let { it.substring(0, it.indexOf("\n")) }
    .trim()

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
        display = Display.flex
        borderColor = Color.black
        borderRadius = 40.px
        textAlign = TextAlign.center
        verticalAlign = VerticalAlign.middle
        justifyContent = JustifyContent.center
        alignItems = Align.center
    }
) {
    i { className = ClassName("fa fa-exclamation-circle ${if (seenNotification) "" else "fa-2x"}") }
}
