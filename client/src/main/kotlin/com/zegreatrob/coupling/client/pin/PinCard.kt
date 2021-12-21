package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.DataProps
import react.RBuilder
import react.dom.div
import react.router.dom.Link

data class PinCardProps(
    val tribeId: TribeId,
    val pin: Pin,
    val shouldLink: Boolean = true
) : DataProps

private val styles = useStyles("pin/PinCard")

fun RBuilder.pinCard(tribeId: TribeId, pin: Pin, shouldLink: Boolean = true, key: String? = null) = child(
    PinCard, PinCardProps(tribeId, pin, shouldLink), key = key
)

val PinCard = reactFunction<PinCardProps> { (tribeId, pin, shouldLink) ->
    optionalLink(shouldLink, tribeId, pin) {
        div(styles.className) {
            pinButton(pin, key = null, showTooltip = false)
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
