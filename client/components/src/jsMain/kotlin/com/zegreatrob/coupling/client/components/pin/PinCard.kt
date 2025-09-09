package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.client.components.CouplingImages
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.ChildrenBuilder
import react.Props
import react.dom.html.ReactHTML
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
        ReactHTML.div {
            css {
                position = Position.Companion.relative
                clear = Clear.Companion.both
                display = Display.Companion.inlineBlock
                overflow = Overflow.Companion.hidden
                border = Border(3.px, LineStyle.Companion.outset, NamedColor.Companion.black)
                backgroundColor = Color("#E0DCD2FF")
                backgroundImage = url(CouplingImages.images.overlayPng)
                backgroundRepeat = BackgroundRepeat.Companion.repeatX
                textAlign = TextAlign.Companion.center
                textDecoration = None.Companion.none
                borderRadius = 6.px
                boxShadow = BoxShadow(1.px, 2.px, 2.px, rgb(0, 0, 0, 0.6))
                color = NamedColor.Companion.black
                margin = Margin(0.px, 2.px, 0.px, 2.px)
                top = 0.px
                padding = 2.px
            }
            PinButton(pin, PinButtonScale.Small, showTooltip = false)
            ReactHTML.div {
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
