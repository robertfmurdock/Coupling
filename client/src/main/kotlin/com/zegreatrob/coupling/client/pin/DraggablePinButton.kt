package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactdnd.useDrag
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import kotlinx.html.classes
import org.w3c.dom.Node
import react.RBuilder
import react.RProps
import react.dom.span
import react.useRef

const val pinDragItemType = "PAIR_PIN"

fun RBuilder.draggablePinButton(pin: Pin, scale: PinButtonScale) = child(
    DraggablePinButton, DraggablePinButtonProps(pin, scale)
)

data class DraggablePinButtonProps(val pin: Pin, val scale: PinButtonScale) : RProps

private val styles = useStyles("pin/DraggablePin")

val DraggablePinButton = reactFunction<DraggablePinButtonProps> { (pin, scale) ->
    val (_, drag) = useDrag(itemType = pinDragItemType, itemId = pin._id!!, collect = { })
    val draggableRef = useRef<Node?>(null)

    drag(draggableRef)

    span {
        attrs {
            ref = draggableRef
            classes += listOf(styles.className, styles["hoverZoom"])
        }
        pinButton(pin, scale, key = null, showTooltip = true)
    }
}
