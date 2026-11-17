package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.external.marked.parse
import com.zegreatrob.minreact.nfc
import emotion.react.css
import kotlinx.browser.localStorage
import react.ChildrenBuilder
import react.Props
import react.RefObject
import react.create
import react.dom.DangerouslySetInnerHTML
import react.dom.createPortal
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.dialog
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.useId
import react.useRef
import react.useState
import web.cssom.AlignItems
import web.cssom.Auto
import web.cssom.Border
import web.cssom.ClassName
import web.cssom.Display
import web.cssom.Float
import web.cssom.FontWeight
import web.cssom.JustifyContent
import web.cssom.LineStyle
import web.cssom.NamedColor
import web.cssom.Overflow
import web.cssom.OverscrollBehavior
import web.cssom.Position
import web.cssom.TextAlign
import web.cssom.VerticalAlign
import web.cssom.ident
import web.cssom.number
import web.cssom.pct
import web.cssom.px
import web.cssom.rgb
import web.cssom.s
import web.cssom.vh
import web.cssom.vw
import web.dom.ElementId
import web.dom.document
import web.html.ButtonType
import web.html.HTMLDialogElement
import web.html.HtmlSource
import web.html.button

private fun saveNotificationLog(updatedThing: Array<String>) {
    localStorage.setItem("notification-log", JSON.stringify(updatedThing))
}

private fun loadNotificationLog() = localStorage.getItem("notification-log")?.let { JSON.parse<Array<String>>(it) } ?: emptyArray()

val NotificationButton by nfc<Props> {
    val animationsDisabled = useAnimationsDisabled()
    val dialogRef = useRef<HTMLDialogElement>(null)
    val titleId = useId()
    val recentInfoMd = MarkdownContent.content.recentInfoMd
    val (seenNotification, setSeenNotification) = useState {
        loadNotificationLog().contains(recentInfoMd.dateLineFromRecentInfo())
    }
    val onPopupClose = {
        saveNotificationLog(loadNotificationLog() + recentInfoMd.dateLineFromRecentInfo())
        setSeenNotification(true)
    }
    span {
        css {
            position = Position.relative
            left = 100.pct
        }
        span {
            css {
                float = Float.left
                position = Position.absolute
                top = (-5).px
                right = (-80).px
            }
            +notificationButton(seenNotification, animationsDisabled) { dialogRef.current?.showModal() }
        }
    }
    +createPortal(
        dialog.create {
            ref = dialogRef
            ariaLabelledBy = titleId
            onClose = { onPopupClose() }
            var pressedOutside = false
            onPointerDown = { event ->
                val bounds = event.currentTarget.getBoundingClientRect()
                pressedOutside = event.clientX < bounds.left || event.clientX > bounds.right ||
                    event.clientY < bounds.top || event.clientY > bounds.bottom
            }
            onPointerUp = { event ->
                val bounds = event.currentTarget.getBoundingClientRect()
                val outside = event.clientX < bounds.left || event.clientX > bounds.right ||
                    event.clientY < bounds.top || event.clientY > bounds.bottom
                if (pressedOutside && outside) event.currentTarget.close()
                pressedOutside = false
            }
            css {
                borderRadius = 30.px
                border = Border(1.px, LineStyle.solid, NamedColor.black)
                width = 50.vw
                maxWidth = 90.vw
                maxHeight = 80.vh
                overflowY = Auto.auto
                overscrollBehavior = OverscrollBehavior.contain
                "&::backdrop" {
                    backgroundColor = rgb(0, 0, 0, 0.5)
                    overflow = Overflow.hidden
                    overscrollBehavior = OverscrollBehavior.contain
                }
            }
            recentInformationContent(titleId, recentInfoMd, dialogRef)
        },
        document.body!!,
    )
}

private fun String.dateLineFromRecentInfo() = substring(indexOf("##"))
    .let { it.substring(0, it.indexOf("\n")) }
    .trim()

private fun ChildrenBuilder.recentInformationContent(titleId: ElementId, recentInfoMd: String, dialogRef: RefObject<HTMLDialogElement>) {
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
        h2 {
            id = titleId
            +"Recent information"
        }
        button {
            type = ButtonType.button
            autoFocus = true
            onClick = { dialogRef.current?.close() }
            +"Close"
        }
        div { dangerouslySetInnerHTML = DangerouslySetInnerHTML.invoke(HtmlSource(parse(recentInfoMd))) }
    }
}

private fun notificationButton(seenNotification: Boolean, animationsDisabled: Boolean, onOpen: () -> Unit) = button.create {
    type = ButtonType.button
    ariaLabel = "Recent information"
    onClick = { onOpen() }
    css {
        val buttonSize = if (seenNotification) 50.px else 75.px
        backgroundColor = if (seenNotification) NamedColor.darkcyan else NamedColor.crimson
        if (!seenNotification && !animationsDisabled) {
            animationName = ident("pulsate")
            animationIterationCount = number(4.0)
            animationDuration = 0.75.s
        }
        color = NamedColor.white
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
