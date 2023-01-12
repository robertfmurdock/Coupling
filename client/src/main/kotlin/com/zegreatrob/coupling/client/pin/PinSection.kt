package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.components.PinButton
import com.zegreatrob.coupling.components.PinButtonScale
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import csstype.Position
import csstype.pct
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

val pinSection = tmFC<PinSection> { (pinList, scale, canDrag, className) ->
    div {
        css(className) {
            marginLeft = -(pinList.size * 12 * scale.factor).px
            position = Position.absolute
            bottom = 10.px
            left = 50.pct
        }
        pinList.map { pin ->
            add(
                if (canDrag) {
                    DraggablePinButton(pin, scale)
                } else {
                    PinButton(pin, scale, showTooltip = true)
                }
            )
        }
    }
}
