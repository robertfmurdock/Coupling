package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.client.components.PinButton
import com.zegreatrob.coupling.client.components.PinButtonScale
import com.zegreatrob.coupling.client.components.pngPath
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.ntmFC
import emotion.react.css
import react.ChildrenBuilder
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

data class PinCard(val partyId: PartyId, val pin: Pin, val shouldLink: Boolean = true) : DataPropsBind<PinCard>(pinCard)

val pinCard by ntmFC<PinCard> { (partyId, pin, shouldLink) ->
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
            to = "/${partyId.value}/pin/${pin.id}"
            handler()
        }
    } else {
        handler()
    }
}
