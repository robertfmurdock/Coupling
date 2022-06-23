package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.components.PinButton
import com.zegreatrob.coupling.components.PinButtonScale
import com.zegreatrob.coupling.components.pngPath
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.BackgroundRepeat
import csstype.Border
import csstype.BoxShadow
import csstype.ClassName
import csstype.Clear
import csstype.Color
import csstype.Display
import csstype.LineStyle
import csstype.Margin
import csstype.NamedColor
import csstype.None
import csstype.Overflow
import csstype.Position
import csstype.TextAlign
import csstype.px
import csstype.rgba
import csstype.url
import emotion.react.css
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.router.dom.Link

data class PinCard(val partyId: PartyId, val pin: Pin, val shouldLink: Boolean = true) : DataPropsBind<PinCard>(pinCard)

val pinCard = tmFC<PinCard> { (partyId, pin, shouldLink) ->
    optionalLink(shouldLink, partyId, pin) {
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
                boxShadow = BoxShadow(1.px, 2.px, 2.px, rgba(0, 0, 0, 0.6))
                color = NamedColor.black
                margin = Margin(0.px, 2.px, 0.px, 2.px)
                top = 0.px
                padding = 2.px
            }
            add(PinButton(pin, PinButtonScale.Small, showTooltip = false))
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
    handler: ChildrenBuilder.() -> Unit
) {
    if (shouldLink) {
        Link {
            to = "/${partyId.value}/pin/${pin.id}"
            handler()
        }
    } else {
        handler()
    }
}
