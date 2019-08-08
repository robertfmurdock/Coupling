package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.external.react.ComponentBuilder
import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.EmptyProps
import com.zegreatrob.coupling.client.external.react.reactFunctionComponent
import com.zegreatrob.coupling.client.routing.DataLoadProps
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder


object WelcomePage : ComponentProvider<PageProps>(), WelcomePageBuilder

private val LoadedWelcome = dataLoadWrapper(Welcome)
private val RBuilder.loadedWelcome get() = LoadedWelcome.captor(this)

interface WelcomePageBuilder : ComponentBuilder<PageProps> {

    override fun build() = reactFunctionComponent<PageProps> {
        loadedWelcome(
            DataLoadProps { EmptyProps }
        )
    }
}
