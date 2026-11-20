package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.client.components.external.reactdnd.useDrag
import com.zegreatrob.coupling.client.components.external.reactdnd.useDrop
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import csstype.PropertiesBuilder
import emotion.react.css
import react.Props
import react.ReactNode
import react.RefObject
import react.dom.html.HTMLAttributes
import react.dom.html.ReactHTML
import react.useRef
import web.cssom.Display
import web.html.HTMLElement
import kotlin.js.Json
import kotlin.js.json

external interface DraggableThingProps : Props {
    var itemType: String
    var itemId: String
    var dropCallback: (String) -> Unit
    var endCallback: (itemId: String, dropResult: Json?) -> Unit
    var css: (PropertiesBuilder) -> Unit
    var children: (isOver: Boolean) -> ReactNode
    var onDragStarted: (() -> Unit)?
    var dragSource: RefObject<HTMLElement>
}

@ReactFunc
val DraggableThing by nfc<DraggableThingProps> { props ->
    val (itemType, itemId, dropCallback, endCallback, _, handler) = props
    val dropTarget = useRef<HTMLElement>(null)
    val dragged = useRef(false)

    val (_, drag) = useDrag<Unit>(itemType = itemType, itemId = itemId, endCallback = endCallback)
    val (isOver, drop) = useDrop(
        acceptItemType = itemType,
        drop = { item ->
            dropCallback(item["id"].unsafeCast<String>())
            json("dropTargetId" to itemId)
        },
        collect = { monitor -> monitor.isOver() },
    )
    drag(props.dragSource)
    drop(dropTarget)

    ReactHTML.span {
        css(props.css)
        ref = dropTarget
        props.onDragStarted?.let { suppressClickAfterDrag(dragged, it) }
        +handler(isOver)
    }
}

internal fun <T : HTMLElement> HTMLAttributes<T>.suppressClickAfterDrag(dragged: RefObject<Boolean>, onDragStarted: () -> Unit) {
    onPointerDownCapture = { dragged.current = false }
    onDragStartCapture = {
        dragged.current = true
        onDragStarted()
    }
    onClickCapture = { event ->
        if (dragged.current == true && event.detail != 0) {
            event.preventDefault()
            event.stopPropagation()
        }
    }
}

external interface DroppableThingProps : Props {
    var itemType: String
    var dropCallback: (String) -> Unit
    var children: (isOver: Boolean) -> ReactNode
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

    ReactHTML.div {
        css { display = Display.inlineBlock }
        ref = draggableRef
        +handler(isOver)
    }
}
