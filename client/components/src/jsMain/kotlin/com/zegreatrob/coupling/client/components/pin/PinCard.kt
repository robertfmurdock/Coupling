package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.client.components.pngPath
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.ChildrenBuilder
import react.Props
import react.dom.html.ReactHTML.div
import react.router.dom.Link
import web.cssom.BackgroundRepeat
import web.cssom.Border
import web.cssom.BoxShadow
import web.cssom.ClassName
import web.cssom.Clear
import web.cssom.Color
import web.cssom.Display
import web.cssom.LineStyle
import web.cssom.Margin
import web.cssom.NamedColor
import web.cssom.None
import web.cssom.Overflow
import web.cssom.Position
import web.cssom.TextAlign
import web.cssom.px
import web.cssom.rgb
import web.cssom.url

external interface PinCardProps : Props {
    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var partyId: PartyId
    var pin: Pin
    var shouldLink: Boolean?
}

@ReactFunc
val PinCard by nfc<PinCardProps> { props ->
    val shouldLink = props.shouldLink ?: true
    val pin = props.pin
    optionalLink(shouldLink, props.partyId, pin) {
        div {
            css {
                position = Position.relative
                clear = Clear.both
                display = Display.inlineBlock
                overflow = Overflow.hidden
                border = Border(3.px, LineStyle.outset, NamedColor.black)
                backgroundColor = Color("#E0DCD2FF")
                backgroundImage = url(pngPath("overlay"))
                backgroundRepeat = BackgroundRepeat.repeatX
                textAlign = TextAlign.center
                textDecoration = None.none
                borderRadius = 6.px
                boxShadow = BoxShadow(1.px, 2.px, 2.px, rgb(0, 0, 0, 0.6))
                color = NamedColor.black
                margin = Margin(0.px, 2.px, 0.px, 2.px)
                top = 0.px
                padding = 2.px
            }
            PinButton(pin, PinButtonScale.Small, showTooltip = false)
            div {
                className = ClassName("pin-name")
                +pin.name
            }
        }
    }
}

private fun ChildrenBuilder.optionalLink(
    shouldLink: Boolean,
    partyId: PartyId,
    pin: Pin,
    handler: ChildrenBuilder.() -> Unit,
) {
    if (shouldLink) {
        Link {
            to = "/${partyId.value}/pin/${pin.id.value}"
            handler()
        }
    } else {
        handler()
    }
}
