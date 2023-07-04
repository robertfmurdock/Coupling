package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.client.components.external.reactdnd.useDrag
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.span
import react.useRef
import web.cssom.AnimationIterationCount
import web.cssom.Display
import web.cssom.TransitionProperty
import web.cssom.TransitionTimingFunction
import web.cssom.ident
import web.cssom.s
import web.html.HTMLElement

const val pinDragItemType = "PAIR_PIN"

external interface DraggablePinButtonProps : Props {
    var pin: Pin
    var scale: PinButtonScale
}

@ReactFunc
val DraggablePinButton by nfc<DraggablePinButtonProps> { props ->
    val pin = props.pin
    val scale = props.scale
    val (_, drag) = useDrag<Unit>(itemType = pinDragItemType, itemId = pin.id!!)
    val draggableRef = useRef<HTMLElement>(null)

    drag(draggableRef)

    span {
        ref = draggableRef
        css {
            display = Display.inlineBlock
            transitionDuration = 0.4.s
            transitionProperty = TransitionProperty.all
            transitionTimingFunction = TransitionTimingFunction.easeIn
            animationDuration = 0.25.s
            animationName = ident("pulsate")
            animationIterationCount = AnimationIterationCount.infinite
        }
        PinButton(pin = pin, scale = scale, showTooltip = true)
    }
}
