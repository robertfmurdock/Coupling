package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.DataLoadProps
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder

object WelcomePage : RComponent<PageProps>(provider()), WelcomePageRenderer

private val LoadedWelcome = dataLoadWrapper(Welcome)
private val RBuilder.loadedWelcome get() = LoadedWelcome.render(this)

interface WelcomePageRenderer : SimpleComponentRenderer<PageProps> {

    override fun RContext<PageProps>.render() = reactElement {
        loadedWelcome(
            DataLoadProps { EmptyProps }
        )
    }

}
