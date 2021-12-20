package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactdnd.useDrag
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.client.reactFunction
import kotlinx.html.classes
import org.w3c.dom.Node
import react.Props
import react.RBuilder
import react.dom.attrs
import react.dom.span
import react.useRef

const val pinDragItemType = "PAIR_PIN"

fun RBuilder.draggablePinButton(pin: Pin, scale: PinButtonScale) = child(
    DraggablePinButton, DraggablePinButtonProps(pin, scale)
)

data class DraggablePinButtonProps(val pin: Pin, val scale: PinButtonScale) : Props

private val styles = useStyles("pin/DraggablePin")

val DraggablePinButton = reactFunction<DraggablePinButtonProps> { (pin, scale) ->
    val (_, drag) = useDrag<Unit>(itemType = pinDragItemType, itemId = pin.id!!)
    val draggableRef = useRef<Node>(null)

    drag(draggableRef)

    span {
        attrs {
            ref = draggableRef
            classes = classes + listOf(styles.className, styles["hoverZoom"])
        }
        pinButton(pin, scale, key = null, showTooltip = true)
    }
}
