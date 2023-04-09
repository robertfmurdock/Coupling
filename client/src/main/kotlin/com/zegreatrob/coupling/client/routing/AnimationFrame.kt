package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.components.animationsDisabledContext
import com.zegreatrob.react.dataloader.DataLoadState
import com.zegreatrob.react.dataloader.EmptyState
import csstype.ident
import csstype.integer
import csstype.s
import emotion.react.css
import react.FC
import react.PropsWithChildren
import react.create
import react.dom.html.ReactHTML.div
import react.useState

external interface AnimationFrameProps : PropsWithChildren {
    var state: DataLoadState<*>
}

enum class AnimationState {
    Start, Stop
}

val animationFrame = FC<AnimationFrameProps> { props ->
    var animationState by useState(AnimationState.Start)
    val shouldStartAnimation = props.state !is EmptyState && animationState === AnimationState.Start

    animationsDisabledContext.Consumer {
        children = { animationsDisabled: Boolean ->
            div.create {
                css {
                    if (shouldStartAnimation && !animationsDisabled) {
                        zIndex = integer(100)
                        animationDuration = 0.25.s
                        animationName = ident("spin-in")
                    }
                }
                onAnimationEnd = { animationState = AnimationState.Stop }
                +props.children
            }
        }
    }
}
