package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactdnd.useDrag
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import kotlinx.html.classes
import org.w3c.dom.Node
import react.dom.attrs
import react.dom.span
import react.useRef

const val pinDragItemType = "PAIR_PIN"

data class DraggablePinButton(val pin: Pin, val scale: PinButtonScale) : DataProps<DraggablePinButton> {
    override val component: TMFC<DraggablePinButton> get() = draggablePinButton
}

private val styles = useStyles("pin/DraggablePin")

val draggablePinButton = reactFunction<DraggablePinButton> { (pin, scale) ->
    val (_, drag) = useDrag<Unit>(itemType = pinDragItemType, itemId = pin.id!!)
    val draggableRef = useRef<Node>(null)

    drag(draggableRef)

    span {
        attrs {
            ref = draggableRef
            classes = classes + listOf(styles.className, styles["hoverZoom"])
        }
        child(PinButton(pin, scale, showTooltip = true))
    }
}
