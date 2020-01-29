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

object PinCard : FRComponent<PinCardProps>(provider()) {

    fun RBuilder.pinCard(tribeId: TribeId, pin: Pin, shouldLink: Boolean = true, key: String? = null) =
        child(PinCard(PinCardProps(tribeId, pin, shouldLink), key = key))

    override fun render(props: PinCardProps) = reactElement {
        val (tribeId, pin, shouldLink) = props
        val styles = useStyles("pin/PinCard")

        optionalLink(shouldLink, tribeId, pin) {
            div(styles.className) {
                pinButton(pin, key = null, showTooltip = false)
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
