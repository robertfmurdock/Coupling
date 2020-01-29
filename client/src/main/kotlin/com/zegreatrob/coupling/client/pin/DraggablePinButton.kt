package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactdnd.useDrag
import com.zegreatrob.coupling.client.pin.PinButton.pinButton
import com.zegreatrob.coupling.model.pin.Pin
import kotlinx.html.classes
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
        val styles = useStyles<DraggablePinButtonStyles>("pin/DraggablePin")
        val (_, drag) = useDrag(itemType = pinDragItemType, itemId = pin._id!!, collect = { })
        val draggableRef = useRef<Node>(null)

        drag(draggableRef)

        reactElement {
            span {
                attrs {
                    ref = draggableRef
                    classes += listOf(styles.className, styles.hoverZoom)
                }
                pinButton(pin, scale, key = null, showTooltip = true)
            }
        }
    }

}

external interface DraggablePinButtonStyles {
    val className: String
    val hoverZoom: String
}

data class DraggablePinButtonProps(val pin: Pin, val scale: PinButtonScale) : RProps