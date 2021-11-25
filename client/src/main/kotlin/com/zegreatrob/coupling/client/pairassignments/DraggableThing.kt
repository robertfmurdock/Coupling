package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.SimpleStyle
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactdnd.useDrag
import com.zegreatrob.coupling.client.external.reactdnd.useDrop
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import org.w3c.dom.Node
import react.RBuilder
import react.Props
import react.dom.attrs
import react.dom.div
import react.useRef

data class DraggableThingProps(
    val itemType: String,
    val itemId: String,
    val dropCallback: (String) -> Unit,
    val handler: RBuilder.(isOver: Boolean) -> Unit
) : Props

private val styles = useStyles<SimpleStyle>("DraggableThing")

val DraggableThing = reactFunction<DraggableThingProps> { (itemType, itemId, dropCallback, handler) ->
    val draggableRef = useRef<Node>(null)

    val (_, drag) = useDrag<Unit>(itemType = itemType, itemId = itemId)
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
