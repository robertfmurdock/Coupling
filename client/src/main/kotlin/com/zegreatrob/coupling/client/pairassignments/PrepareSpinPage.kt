package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.ComponentBuilder
import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.reactFunctionComponent
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder

object PrepareSpinPage : ComponentProvider<PageProps>(), PrepareSpinPageBuilder

private val LoadedPairAssignments = dataLoadWrapper(PrepareSpin)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.captor(this)

interface PrepareSpinPageBuilder : ComponentBuilder<PageProps>, TribeDataSetQueryDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.tribeId

        if (tribeId != null) {
            loadedPairAssignments(
                    dataLoadProps(
                            query = { TribeDataSetQuery(tribeId).perform() },
                            toProps = { _, (tribe, players, history) ->
                                PrepareSpinProps(tribe, players, history, pageProps.pathSetter)
                            }
                    )
            )
        } else throw Exception("WHAT")
    }
}
