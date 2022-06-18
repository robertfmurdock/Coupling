package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import csstype.px
import csstype.unaryMinus
import emotion.react.css
import react.dom.html.ReactHTML.div

data class PinSection(
    val pinList: List<Pin>,
    val scale: PinButtonScale = PinButtonScale.Small,
    val canDrag: Boolean = false,
    val className: ClassName = ClassName("")
) : DataPropsBind<PinSection>(pinSection)

private val styles = useStyles("pin/PinSection")

val pinSection = tmFC<PinSection> { (pinList, scale, canDrag, className) ->
    div {
        css(styles.className, className) {
            marginLeft = -(pinList.size * 12 * scale.factor).px
        }
        pinList.map { pin ->
            add(
                if (canDrag)
                    DraggablePinButton(pin, scale)
                else
                    PinButton(pin, scale, showTooltip = true)
            )
        }
    }
}
