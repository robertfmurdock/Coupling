package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.client.components.PinButton
import com.zegreatrob.coupling.client.components.PinButtonScale
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.ntmFC
import emotion.react.css
import react.dom.html.ReactHTML.div
import web.cssom.ClassName
import web.cssom.Position
import web.cssom.pct
import web.cssom.px
import web.cssom.unaryMinus

data class PinSection(
    val pinList: List<Pin>,
    val scale: PinButtonScale = PinButtonScale.Small,
    val canDrag: Boolean = false,
    val className: ClassName = ClassName(""),
) : DataPropsBind<PinSection>(pinSection)

val pinSection by ntmFC<PinSection> { (pinList, scale, canDrag, className) ->
    div {
        css(className) {
            marginLeft = -(pinList.size * 12 * scale.factor).px
            position = Position.absolute
            bottom = 10.px
            left = 50.pct
        }
        pinList.map { pin ->
            if (canDrag) {
                add(DraggablePinButton(pin, scale))
            } else {
                PinButton(pin, scale, showTooltip = true)
            }
        }
    }
}
