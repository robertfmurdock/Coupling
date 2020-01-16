package com.zegreatrob.coupling.client.pin


import PinButton
import PinButtonProps
import com.zegreatrob.coupling.client.external.react.FRComponent
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.pin.Pin
import react.RProps
import react.dom.div

data class PinCardProps(val pin: Pin) : RProps

external class PinCardStyles {
    val className: String
    val icon: String
}

object PinCard : FRComponent<PinCardProps>(provider()) {

    override fun render(props: PinCardProps) = reactElement {
        val (pin) = props
        val styles = useStyles<PinCardStyles>("pin/PinCard")

        div(styles.className) {
            child(PinButton(PinButtonProps(pin)))
            div(classes = "pin-name") {
                +(pin.name ?: "Unnamed pin")
            }
        }
    }

}
