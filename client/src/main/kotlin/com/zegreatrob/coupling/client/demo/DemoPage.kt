package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.frameRunner
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.minreact.reactFunction

val DemoPage = reactFunction<PageProps> {
    frameRunner(DemoAnimationState.generateSequence(), 1.0) {

    }
}
