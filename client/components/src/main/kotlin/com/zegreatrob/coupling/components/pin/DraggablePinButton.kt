package com.zegreatrob.coupling.components.pin

import com.zegreatrob.coupling.components.PinButton
import com.zegreatrob.coupling.components.PinButtonScale
import com.zegreatrob.coupling.components.external.reactdnd.useDrag
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.AnimationIterationCount
import csstype.Display
import csstype.TransitionProperty
import csstype.TransitionTimingFunction
import csstype.ident
import csstype.s
import emotion.react.css
import react.dom.html.ReactHTML.span
import react.useRef
import web.html.HTMLElement

const val pinDragItemType = "PAIR_PIN"

data class DraggablePinButton(val pin: Pin, val scale: PinButtonScale) :
    DataPropsBind<DraggablePinButton>(draggablePinButton)

val draggablePinButton = tmFC<DraggablePinButton> { (pin, scale) ->
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
        add(PinButton(pin, scale, showTooltip = true))
    }
}
