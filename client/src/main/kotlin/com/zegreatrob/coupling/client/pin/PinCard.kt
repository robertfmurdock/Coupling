package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.FRComponent
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pin.PinButton.pinButton
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

external class PinCardStyles {
    val className: String
    val icon: String
}

object PinCard : FRComponent<PinCardProps>(provider()) {

    override fun render(props: PinCardProps) = reactElement {
        val (tribeId, pin, shouldLink) = props
        val styles = useStyles<PinCardStyles>("pin/PinCard")

        optionalLink(shouldLink, tribeId, pin) {
            div(styles.className) {
                pinButton(pin, key = null)
                div(classes = "pin-name") {
                    +(pin.name ?: "Unnamed pin")
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

}
