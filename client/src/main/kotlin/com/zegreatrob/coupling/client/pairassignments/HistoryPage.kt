package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.*
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
