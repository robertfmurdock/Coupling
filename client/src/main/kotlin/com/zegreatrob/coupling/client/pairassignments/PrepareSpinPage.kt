package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.*
import react.RBuilder


object PrepareSpinPage : ComponentProvider<PageProps>(), PrepareSpinPageBuilder

val RBuilder.prepareSpinPage get() = PrepareSpinPage.captor(this)

private val LoadedPairAssignments = dataLoadWrapper(PrepareSpin)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.captor(this)

interface PrepareSpinPageBuilder : ComponentBuilder<PageProps>, TribeDataSetQueryDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.tribeId

        if (tribeId != null) {
            loadedPairAssignments(
                    dataLoadProps(
                            query = { TribeDataSetQuery(tribeId, pageProps.coupling).perform() },
                            toProps = { _, (tribe, players, history) ->
                                PrepareSpinProps(tribe, players, history, pageProps.pathSetter)
                            }
                    )
            )
        } else throw Exception("WHAT")
    }

}
