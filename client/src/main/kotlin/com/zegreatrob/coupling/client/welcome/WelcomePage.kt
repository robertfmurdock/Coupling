package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.routing.DataLoadProps
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder


private val LoadedWelcome = dataLoadWrapper(Welcome)
private val RBuilder.loadedWelcome get() = LoadedWelcome.render(this)

val WelcomePage = reactFunction<PageProps> { props ->
    loadedWelcome(DataLoadProps { _, scope -> WelcomeProps(commandFunc = props.commander.buildCommandFunc(scope)) })
}
