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
import kotlin.js.Json
import kotlin.js.json

external interface DraggableThingProps : Props {
    var itemType: String
    var itemId: String
    var dropCallback: (String) -> Unit
    var handler: (isOver: Boolean) -> ReactNode
    var endCallback: (itemId: String, dropResult: Json?) -> Unit
}

@ReactFunc
val DraggableThing by nfc<DraggableThingProps> { (itemType, itemId, dropCallback, handler, endCallback) ->
    val draggableRef = useRef<HTMLElement>(null)

    val (_, drag) = useDrag<Unit>(itemType = itemType, itemId = itemId, endCallback = endCallback)
    val (isOver, drop) = useDrop(
        acceptItemType = itemType,
        drop = { item ->
            dropCallback(item["id"].unsafeCast<String>())
            json("dropTargetId" to itemId)
        },
        collect = { monitor -> monitor.isOver() },
    )
    drag(drop(draggableRef))

    div {
        css { display = Display.inlineBlock }
        ref = draggableRef
        +handler(isOver)
    }
}

external interface DroppableThingProps : Props {
    var itemType: String
    var dropCallback: (String) -> Unit
    var handler: (isOver: Boolean) -> ReactNode
}

@ReactFunc
val DroppableThing by nfc<DroppableThingProps> { (itemType, dropCallback, handler) ->
    val draggableRef = useRef<HTMLElement>(null)

    val (isOver, drop) = useDrop(
        acceptItemType = itemType,
        drop = { item ->
            dropCallback(item["id"].unsafeCast<String>())
            undefined
        },
        collect = { monitor -> monitor.isOver() },
    )
    drop(draggableRef)

    div {
        css { display = Display.inlineBlock }
        ref = draggableRef
        +handler(isOver)
    }
}
