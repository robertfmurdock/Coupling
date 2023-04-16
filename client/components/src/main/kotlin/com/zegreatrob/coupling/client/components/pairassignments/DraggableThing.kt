package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.client.components.external.reactdnd.useDrag
import com.zegreatrob.coupling.client.components.external.reactdnd.useDrop
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.ntmFC
import csstype.Display
import emotion.react.css
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.useRef
import web.html.HTMLElement

data class DraggableThing(
    val itemType: String,
    val itemId: String,
    val dropCallback: (String) -> Unit,
    val handler: ChildrenBuilder.(isOver: Boolean) -> Unit,
) : DataPropsBind<DraggableThing>(draggableThing)

val draggableThing by ntmFC<DraggableThing> { (itemType, itemId, dropCallback, handler) ->
    val draggableRef = useRef<HTMLElement>(null)

    val (_, drag) = useDrag<Unit>(itemType = itemType, itemId = itemId)
    val (isOver, drop) = useDrop(
        acceptItemType = itemType,
        drop = { item -> dropCallback(item["id"].unsafeCast<String>()) },
        collect = { monitor -> monitor.isOver() },
    )
    drag(drop(draggableRef))

    div {
        css { display = Display.inlineBlock }
        ref = draggableRef
        handler(isOver)
    }
}
