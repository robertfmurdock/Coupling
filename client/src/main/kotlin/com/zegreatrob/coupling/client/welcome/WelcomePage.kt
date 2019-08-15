package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.DataLoadProps
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder

object WelcomePage : ComponentProvider<PageProps>(provider()), WelcomePageRenderer

private val LoadedWelcome = dataLoadWrapper(Welcome)
private val RBuilder.loadedWelcome get() = LoadedWelcome.captor(this)

interface WelcomePageRenderer : SimpleComponentRenderer<PageProps> {

    override fun PropsBuilder<PageProps>.render() = reactElement {
        loadedWelcome(
            DataLoadProps { EmptyProps }
        )
    }

}
