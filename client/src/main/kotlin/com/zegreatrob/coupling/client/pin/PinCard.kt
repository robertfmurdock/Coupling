package com.zegreatrob.coupling.client.pin


import com.zegreatrob.coupling.client.external.react.FRComponent
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.pin.Pin
import kotlinx.html.classes
import react.RProps
import react.dom.div
import react.dom.i

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
            div(styles.icon) {
                i("fa-3x") { attrs { classes += targetIcon(pin) } }
            }
            div(classes = "pin-name") {
                +(pin.name ?: "Unnamed pin")
            }
        }
    }

    private fun targetIcon(pin: Pin): String {
        var targetIcon = if (pin.icon.isNullOrEmpty()) "fa-skull" else pin.icon!!
        if (!targetIcon.startsWith("fa")) {
            targetIcon = "fa-$targetIcon"
        }
        var fontAwesomeStyle = "fa"
        val split = targetIcon.split(" ")
        if (split.size > 1) {
            fontAwesomeStyle = ""
        }

        targetIcon = "$fontAwesomeStyle $targetIcon"
        return targetIcon
    }
}
