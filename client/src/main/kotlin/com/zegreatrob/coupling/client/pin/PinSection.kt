package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import kotlinx.css.marginLeft
import kotlinx.css.px
import kotlinx.html.classes
import react.RBuilder
import react.dom.attrs
import styled.css
import styled.styledDiv

data class PinSection(
    val pinList: List<Pin>,
    val scale: PinButtonScale = PinButtonScale.Small,
    val canDrag: Boolean,
    val className: String
) : DataProps<PinSection> {
    override val component: TMFC<PinSection> get() = pinSection
}

fun RBuilder.pinSection(
    pinList: List<Pin>,
    scale: PinButtonScale = PinButtonScale.Small,
    canDrag: Boolean = false,
    className: String = ""
) = child(PinSection(pinList, scale, canDrag, className))

private val styles = useStyles("pin/PinSection")

val pinSection = reactFunction<PinSection> { (pinList, scale, canDrag, className) ->
    styledDiv {
        attrs {
            classes = classes + styles.className + className
            css { marginLeft = -(pinList.size * 12 * scale.factor).px }
        }
        pinList.map { pin ->
            if (canDrag)
                child(DraggablePinButton(pin, scale))
            else
                child(PinButton(pin, scale, showTooltip = true), key = null)
        }
    }
}
