package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.Frame
import com.zegreatrob.coupling.client.frameRunner
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.minreact.reactFunction

val DemoPage = reactFunction<PageProps> {
    frameRunner(emptySequence<Frame<Any>>(), 1.0) {

    }
}
