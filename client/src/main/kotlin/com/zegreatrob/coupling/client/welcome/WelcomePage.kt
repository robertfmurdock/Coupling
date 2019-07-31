package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.player.PageProps
import react.RBuilder


object WelcomePage : ComponentProvider<PageProps>(), WelcomePageBuilder

val RBuilder.welcomePage get() = WelcomePage.captor(this)

private val LoadedWelcome = dataLoadWrapper(Welcome)
private val RBuilder.loadedWelcome get() = LoadedWelcome.captor(this)

interface WelcomePageBuilder : ComponentBuilder<PageProps> {

    override fun build() = reactFunctionComponent<PageProps> {
        loadedWelcome(
                DataLoadProps { EmptyProps }
        )
    }
}
