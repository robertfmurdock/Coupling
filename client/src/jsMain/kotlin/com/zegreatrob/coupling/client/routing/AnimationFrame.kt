package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.components.animationsDisabledContext
import com.zegreatrob.minreact.nfc
import com.zegreatrob.react.dataloader.DataLoadState
import com.zegreatrob.react.dataloader.EmptyState
import emotion.react.css
import react.PropsWithChildren
import react.create
import react.dom.html.ReactHTML.div
import react.useState
import web.cssom.ident
import web.cssom.integer
import web.cssom.s

external interface AnimationFrameProps : PropsWithChildren {
    var state: DataLoadState<*>
}

enum class AnimationState {
    Start,
    Stop,
}

val animationFrame by nfc<AnimationFrameProps> { props ->
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
