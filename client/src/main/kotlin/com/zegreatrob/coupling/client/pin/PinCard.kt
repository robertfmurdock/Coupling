package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.child
import com.zegreatrob.coupling.client.external.react.reactFunction2
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder
import react.RProps
import react.dom.div
import react.router.dom.routeLink

data class PinCardProps(
    val tribeId: TribeId,
    val pin: Pin,
    val shouldLink: Boolean = true
) : RProps

private val styles = useStyles("pin/PinCard")

fun RBuilder.pinCard(tribeId: TribeId, pin: Pin, shouldLink: Boolean = true, key: String? = null) = child(
    PinCard, PinCardProps(tribeId, pin, shouldLink), key = key
)

val PinCard = reactFunction2<PinCardProps> { (tribeId, pin, shouldLink) ->
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
        routeLink(to = "/${tribeId.value}/pin/${pin._id}", handler = handler)
    } else {
        handler()
    }
}
