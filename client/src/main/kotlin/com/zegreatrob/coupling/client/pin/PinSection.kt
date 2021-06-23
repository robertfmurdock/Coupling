package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.reactFunction
import kotlinx.css.marginLeft
import kotlinx.css.px
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.dom.attrs
import styled.css
import styled.styledDiv

data class PinSectionProps(
    val pinList: List<Pin>,
    val scale: PinButtonScale = PinButtonScale.Small,
    val canDrag: Boolean,
    val className: String
) : RProps

fun RBuilder.pinSection(
    pinList: List<Pin>,
    scale: PinButtonScale = PinButtonScale.Small,
    canDrag: Boolean = false,
    className: String = ""
) = child(PinSection, PinSectionProps(pinList, scale, canDrag, className), {})

private val styles = useStyles("pin/PinSection")

val PinSection =
    reactFunction<PinSectionProps> { (pinList, scale, canDrag, className) ->
        styledDiv {
            attrs {
                classes = classes + styles.className + className
                css { marginLeft = -(pinList.size * 12 * scale.factor).px }
            }
            pinList.map { pin ->
                if (canDrag)
                    draggablePinButton(pin, scale)
                else
                    pinButton(pin, scale, key = null, showTooltip = true)
            }
        }
    }
