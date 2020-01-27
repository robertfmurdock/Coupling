package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactdnd.useDrag
import com.zegreatrob.coupling.client.external.reactdnd.useDrop
import org.w3c.dom.Node
import react.RBuilder
import react.RProps
import react.dom.div

data class DraggableThingProps(
    val itemType: String,
    val itemId: String,
    val dropCallback: (String) -> Unit,
    val handler: RBuilder.(isOver: Boolean) -> Unit
) : RProps

object DraggableThing : FRComponent<DraggableThingProps>(provider()) {

    fun RBuilder.draggableThing(
        itemType: String,
        itemId: String,
        dropCallback: (String) -> Unit,
        handler: RBuilder.(isOver: Boolean) -> Unit
    ) = child(DraggableThing.component.rFunction, DraggableThingProps(itemType, itemId, dropCallback, handler))

    override fun render(props: DraggableThingProps) = with(props) {
        val styles = useStyles<SimpleStyle>("DraggableThing")
        val draggableRef = useRef<Node>(null)

        val (_, drag) = useDrag(itemType = itemType, itemId = itemId, collect = { })
        val (isOver, drop) = useDrop(
            acceptItemType = itemType,
            drop = { item -> dropCallback(item["id"].unsafeCast<String>()) },
            collect = { monitor -> monitor.isOver() }
        )
        drag(drop(draggableRef))

        reactElement {
            div(classes = styles.className) {
                attrs { ref = draggableRef }
                handler(isOver)
            }
        }
    }
}
