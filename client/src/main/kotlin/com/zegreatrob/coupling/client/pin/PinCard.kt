package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.router.dom.Link

data class PinCard(val partyId: PartyId, val pin: Pin, val shouldLink: Boolean = true) : DataPropsBind<PinCard>(pinCard)

private val styles = useStyles("pin/PinCard")

val pinCard = tmFC<PinCard> { (partyId, pin, shouldLink) ->
    optionalLink(shouldLink, partyId, pin) {
        div {
            className = styles.className
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
