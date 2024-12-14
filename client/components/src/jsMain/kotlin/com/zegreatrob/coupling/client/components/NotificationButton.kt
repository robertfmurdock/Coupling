package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.external.marked.parse
import com.zegreatrob.coupling.client.components.external.reactpopup.popup
import com.zegreatrob.minreact.nfc
import emotion.react.css
import js.objects.jso
import kotlinx.browser.localStorage
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.useState
import web.cssom.AlignItems
import web.cssom.AnimationIterationCount
import web.cssom.ClassName
import web.cssom.Display
import web.cssom.Float
import web.cssom.FontWeight
import web.cssom.JustifyContent
import web.cssom.NamedColor
import web.cssom.Position
import web.cssom.TextAlign
import web.cssom.VerticalAlign
import web.cssom.ident
import web.cssom.px
import web.cssom.s
import kotlin.js.json

private fun saveNotificationLog(updatedThing: Array<String>) {
    localStorage.setItem("notification-log", JSON.stringify(updatedThing))
}

private fun loadNotificationLog() = localStorage.getItem("notification-log")?.let { JSON.parse<Array<String>>(it) } ?: emptyArray()

val NotificationButton by nfc<Props> {
    val recentInfoMd = loadMarkdownString("recent-info")
    val (seenNotification, setSeenNotification) = useState {
        loadNotificationLog().contains(recentInfoMd.dateLineFromRecentInfo())
    }
    val onPopupClose = {
        saveNotificationLog(loadNotificationLog() + recentInfoMd.dateLineFromRecentInfo())
        setSeenNotification(true)
    }
    span {
        css { position = Position.relative }
        span {
            css {
                float = Float.left
                position = Position.absolute
                top = (-5).px
                right = (-80).px
            }
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
        div {
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
            div { dangerouslySetInnerHTML = jso { __html = parse(recentInfoMd) } }
        }
    },
    contentStyle = json(
        "borderRadius" to "30px",
        "borderColor" to "black",
        "borderWidth" to "1px",
    ),
    onClose = onClose,
)

private fun notificationButton(open: Boolean, seenNotification: Boolean) = div.create {
    css {
        val buttonSize = if (seenNotification) 50.px else 75.px
        backgroundColor = if (seenNotification) NamedColor.darkcyan else NamedColor.crimson
        if (!seenNotification) {
            animationName = ident("pulsate")
            animationIterationCount = AnimationIterationCount.infinite
            animationDuration = 0.75.s
        }
        color = if (open) NamedColor.darkgray else NamedColor.white
        height = buttonSize
        width = buttonSize
        display = Display.flex
        borderColor = NamedColor.black
        borderRadius = 40.px
        textAlign = TextAlign.center
        verticalAlign = VerticalAlign.middle
        justifyContent = JustifyContent.center
        alignItems = AlignItems.center
    }
    i { className = ClassName("fa fa-exclamation-circle ${if (seenNotification) "" else "fa-2x"}") }
}
