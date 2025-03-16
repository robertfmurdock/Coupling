package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinId
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
import kotlin.js.Json

external interface PinSectionProps : Props {
    var pinList: List<Pin>
    var scale: PinButtonScale?
    var className: ClassName?
    var endCallback: ((PinId, Json?) -> Unit)?
}

@ReactFunc
val PinSection by nfc<PinSectionProps> { props ->
    val (pinList) = props
    val scale = props.scale ?: PinButtonScale.Small
    val endCallback = props.endCallback
    val className = props.className ?: ClassName("")
    div {
        css(className) {
            marginLeft = -(pinList.size * 12 * scale.factor).px
            position = Position.absolute
            bottom = 10.px
            left = 50.pct
        }
        pinList.map { pin ->
            if (endCallback != null) {
                DraggablePinButton(pin, scale, endCallback)
            } else {
                PinButton(pin, scale, showTooltip = true)
            }
        }
    }
}
