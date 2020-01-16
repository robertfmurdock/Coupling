import com.zegreatrob.coupling.client.external.react.FRComponent
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.pin.Pin
import kotlinx.css.*
import kotlinx.html.classes
import react.RProps
import react.dom.i
import styled.css
import styled.styledDiv

enum class PinButtonScale(val faTag: String, val factor: Int) {
    Normal("fa-3x", 3), Large("fa-10x", 10)
}

data class PinButtonProps(val pin: Pin, val scale: PinButtonScale = PinButtonScale.Normal) : RProps

external class PinButtonStyles {
    val className: String
}

object PinButton : FRComponent<PinButtonProps>(provider()) {

    override fun render(props: PinButtonProps) = reactElement {
        val (pin, scale) = props
        val styles = useStyles<PinButtonStyles>("pin/PinButton")

        styledDiv {
            attrs {
                classes += styles.className
                css { scaledStyles(scale) }
            }

            i(scale.faTag) { attrs { classes += targetIcon(pin) } }
        }
    }

    private fun CSSBuilder.scaledStyles(scale: PinButtonScale) {
        padding((3.2 * scale.factor).px)
        borderWidth = (2 * scale.factor).px
        borderRadius = (12 * scale.factor).px
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