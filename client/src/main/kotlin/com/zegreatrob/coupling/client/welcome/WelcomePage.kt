package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.components.welcome.Welcome
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.animationFrame
import com.zegreatrob.minreact.add
import com.zegreatrob.react.dataloader.ResolvedState
import react.FC

val WelcomePage = FC<PageProps> {
    animationFrame {
        this.state = ResolvedState(Unit)
        add(Welcome())
    }
}
