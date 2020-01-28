package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.FRComponent
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pin.DraggablePinButton.draggablePinButton
import com.zegreatrob.coupling.client.pin.PinButton.pinButton
import com.zegreatrob.coupling.model.pin.Pin
import kotlinx.css.marginLeft
import kotlinx.css.px
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import styled.css
import styled.styledDiv

data class PinSectionProps(
    val pinList: List<Pin>,
    val scale: PinButtonScale = PinButtonScale.Small,
    val canDrag: Boolean,
    val className: String
) : RProps

external class PinSectionStyles {
    val className: String
}

object PinSection : FRComponent<PinSectionProps>(provider()) {

    fun RBuilder.pinSection(
        pinList: List<Pin>,
        scale: PinButtonScale = PinButtonScale.Small,
        canDrag: Boolean = false,
        className: String = ""
    ) = child(PinSection(PinSectionProps(pinList, scale, canDrag, className)))

    override fun render(props: PinSectionProps) = with(props) {
        val styles = useStyles<PinSectionStyles>("pin/PinSection")

        reactElement {
            styledDiv {
                attrs {
                    classes += styles.className
                    css { marginLeft = -(pinList.size * 12 * scale.factor).px }
                }
                pinList.map { pin ->
                    if (canDrag)
                        draggablePinButton(pin, scale)
                    else
                        pinButton(pin, scale, key = null)
                }
            }
        }
    }

}