package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactdnd.useDrag
import com.zegreatrob.coupling.client.pin.PinButton.pinButton
import com.zegreatrob.coupling.model.pin.Pin
import org.w3c.dom.Node
import react.RBuilder
import react.RProps
import react.dom.span

const val pinDragItemType = "PAIR_PIN"

object DraggablePinButton : FRComponent<DraggablePinButtonProps>(provider()) {

    fun RBuilder.draggablePinButton(pin: Pin, scale: PinButtonScale) = child(
        DraggablePinButton.component.rFunction, DraggablePinButtonProps(pin, scale)
    )

    override fun render(props: DraggablePinButtonProps) = with(props) {
        val styles = useStyles<SimpleStyle>("pin/DraggablePin")
        val (_, drag) = useDrag(itemType = pinDragItemType, itemId = pin._id!!, collect = { })
        val draggableRef = useRef<Node>(null)

        drag(draggableRef)

        reactElement {
            span(classes = styles.className) {
                attrs { ref = draggableRef }
                pinButton(pin, scale)
            }
        }
    }

}

data class DraggablePinButtonProps(val pin: Pin, val scale: PinButtonScale) : RProps