package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactdnd.useDrag
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import org.w3c.dom.Node
import react.dom.html.ReactHTML.span
import react.useRef

const val pinDragItemType = "PAIR_PIN"

data class DraggablePinButton(val pin: Pin, val scale: PinButtonScale) :
    DataPropsBind<DraggablePinButton>(draggablePinButton)

private val styles = useStyles("pin/DraggablePin")

val draggablePinButton = tmFC<DraggablePinButton> { (pin, scale) ->
    val (_, drag) = useDrag<Unit>(itemType = pinDragItemType, itemId = pin.id!!)
    val draggableRef = useRef<Node>(null)

    drag(draggableRef)

    span {
        ref = draggableRef
        className = ClassName("${styles.className} ${styles["hoverZoom"]}")
        add(PinButton(pin, scale, showTooltip = true))
    }
}
