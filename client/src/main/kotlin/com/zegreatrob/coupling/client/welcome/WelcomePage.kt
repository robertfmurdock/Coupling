package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.DataLoadProps
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder


object WelcomePage : ComponentProvider<PageProps>(), WelcomePageBuilder {
    override fun build() = buildByRender()
}

private val LoadedWelcome = dataLoadWrapper(Welcome)
private val RBuilder.loadedWelcome get() = LoadedWelcome.captor(this)

interface WelcomePageBuilder : SimpleComponentBuilder<PageProps>, ComponentRenderer<PageProps> {

    override fun PropsBuilder<PageProps>.render() = reactElement {
        loadedWelcome(
            DataLoadProps { EmptyProps }
        )
    }


}
