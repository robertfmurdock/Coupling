package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.ClassName
import web.cssom.Position
import web.cssom.pct
import web.cssom.px
import web.cssom.unaryMinus

external interface PinSectionProps : Props {
    var pinList: List<Pin>
    var scale: PinButtonScale?
    var canDrag: Boolean?
    var className: ClassName?
}

@ReactFunc
val PinSection by nfc<PinSectionProps> { props ->
    val (pinList) = props
    val scale = props.scale ?: PinButtonScale.Small
    val canDrag = props.canDrag ?: false
    val className = props.className ?: ClassName("")
    div {
        css(className) {
            marginLeft = -(pinList.size * 12 * scale.factor).px
            position = Position.absolute
            bottom = 10.px
            left = 50.pct
        }
        pinList.map { pin ->
            if (canDrag) {
                DraggablePinButton(pin, scale)
            } else {
                PinButton(pin, scale, showTooltip = true)
            }
        }
    }
}
