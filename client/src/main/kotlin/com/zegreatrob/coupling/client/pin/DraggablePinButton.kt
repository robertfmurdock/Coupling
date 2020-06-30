package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactdnd.useDrag
import com.zegreatrob.coupling.model.pin.Pin
import kotlinx.html.classes
import org.w3c.dom.Node
import react.RBuilder
import react.RProps
import react.dom.span

const val pinDragItemType = "PAIR_PIN"

fun RBuilder.draggablePinButton(pin: Pin, scale: PinButtonScale) = child(
    DraggablePinButton, DraggablePinButtonProps(pin, scale)
)

data class DraggablePinButtonProps(val pin: Pin, val scale: PinButtonScale) : RProps

private val styles = useStyles("pin/DraggablePin")

val DraggablePinButton = reactFunction2<DraggablePinButtonProps> { (pin, scale) ->
    val (_, drag) = useDrag(itemType = pinDragItemType, itemId = pin._id!!, collect = { })
    val draggableRef = useRef<Node>(null)

    drag(draggableRef)

    span {
        attrs {
            ref = draggableRef
            classes += listOf(styles.className, styles["hoverZoom"])
        }
        pinButton(pin, scale, key = null, showTooltip = true)
    }
}
