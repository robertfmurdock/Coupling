package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import react.RBuilder
import react.dom.div
import react.router.dom.Link

data class PinCard(val tribeId: TribeId, val pin: Pin, val shouldLink: Boolean = true) : DataProps<PinCard> {
    override val component: TMFC<PinCard> get() = pinCard
}

private val styles = useStyles("pin/PinCard")

val pinCard = reactFunction<PinCard> { (tribeId, pin, shouldLink) ->
    optionalLink(shouldLink, tribeId, pin) {
        div(styles.className) {
            child(PinButton(pin, PinButtonScale.Small, showTooltip = false))
            div(classes = "pin-name") {
                +pin.name
            }
        }
    }
}

private fun RBuilder.optionalLink(shouldLink: Boolean, tribeId: TribeId, pin: Pin, handler: RBuilder.() -> Unit) {
    if (shouldLink) {
        Link {
            attrs.to = "/${tribeId.value}/pin/${pin.id}"
            handler()
        }
    } else {
        handler()
    }
}
