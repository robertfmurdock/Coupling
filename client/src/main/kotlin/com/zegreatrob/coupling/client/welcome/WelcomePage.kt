package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.*
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
