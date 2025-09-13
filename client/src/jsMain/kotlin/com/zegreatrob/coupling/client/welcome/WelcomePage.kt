package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.components.welcome.Welcome
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.animationFrame
import com.zegreatrob.minreact.nfc
import com.zegreatrob.react.dataloader.ResolvedState
import js.lazy.Lazy

@Lazy
val WelcomePage by nfc<PageProps> {
    animationFrame {
        this.state = ResolvedState(Unit)
        Welcome()
    }
}
