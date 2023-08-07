package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.client.components.external.reactdnd.useDrag
import com.zegreatrob.coupling.client.components.external.reactdnd.useDrop
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.useRef
import web.cssom.Display
import web.html.HTMLElement

external interface DraggableThingProps : Props {
    var itemType: String
    var itemId: String
    var dropCallback: (String) -> Unit
    var handler: (isOver: Boolean) -> ReactNode
}

@ReactFunc
val DraggableThing by nfc<DraggableThingProps> { (itemType, itemId, dropCallback, handler) ->
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
        +handler(isOver)
    }
}
