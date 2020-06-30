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

private val styles = useStyles<SimpleStyle>("DraggableThing")

val DraggableThing = reactFunction<DraggableThingProps> { (itemType, itemId, dropCallback, handler) ->
    val draggableRef = useRef<Node>(null)

    val (_, drag) = useDrag(itemType = itemType, itemId = itemId, collect = { })
    val (isOver, drop) = useDrop(
        acceptItemType = itemType,
        drop = { item -> dropCallback(item["id"].unsafeCast<String>()) },
        collect = { monitor -> monitor.isOver() }
    )
    drag(drop(draggableRef))

    div(classes = styles.className) {
        attrs { ref = draggableRef }
        handler(isOver)
    }
}

fun RBuilder.draggableThing(
    itemType: String,
    itemId: String,
    dropCallback: (String) -> Unit,
    handler: RBuilder.(isOver: Boolean) -> Unit
) = child(DraggableThing, DraggableThingProps(itemType, itemId, dropCallback, handler))
