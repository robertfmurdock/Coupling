package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.ComponentBuilder
import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.reactFunctionComponent
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder


object HistoryPage : ComponentProvider<PageProps>(), HistoryPageBuilder

private val LoadedPairAssignments = dataLoadWrapper(History)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.captor(this)

interface HistoryPageBuilder : ComponentBuilder<PageProps>, HistoryQueryDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.tribeId

        if (tribeId != null) {
            loadedPairAssignments(
                    dataLoadProps(
                            query = { HistoryQuery(tribeId).perform() },
                            toProps = { reload, (tribe, history) ->
                                HistoryProps(tribe, history, reload, pageProps.pathSetter)
                            }
                    )
            )
        } else throw Exception("WHAT")
    }
}
