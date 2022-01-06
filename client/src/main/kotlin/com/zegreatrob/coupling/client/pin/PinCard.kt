package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.router.dom.Link

data class PinCard(val tribeId: TribeId, val pin: Pin, val shouldLink: Boolean = true) : DataProps<PinCard> {
    override val component: TMFC<PinCard> get() = pinCard
}

private val styles = useStyles("pin/PinCard")

val pinCard = tmFC<PinCard> { (tribeId, pin, shouldLink) ->
    optionalLink(shouldLink, tribeId, pin) {
        div {
            className = styles.className
            child(PinButton(pin, PinButtonScale.Small, showTooltip = false))
            div {
                className = "pin-name"
                +pin.name
            }
        }
    }
}

private fun ChildrenBuilder.optionalLink(
    shouldLink: Boolean,
    tribeId: TribeId,
    pin: Pin,
    handler: ChildrenBuilder.() -> Unit
) {
    if (shouldLink) {
        Link {
            to = "/${tribeId.value}/pin/${pin.id}"
            handler()
        }
    } else {
        handler()
    }
}
