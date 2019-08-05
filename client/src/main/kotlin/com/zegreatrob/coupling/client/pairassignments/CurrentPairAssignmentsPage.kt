package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import react.RBuilder


object CurrentPairAssignmentsPage : ComponentProvider<PageProps>(), CurrentPairAssignmentsPageBuilder

val RBuilder.currentPairAssignmentsPage get() = CurrentPairAssignmentsPage.captor(this)

private val LoadedPairAssignments = dataLoadWrapper(PairAssignments)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.captor(this)

interface CurrentPairAssignmentsPageBuilder : ComponentBuilder<PageProps>, TribeDataSetQueryDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.tribeId

        if (tribeId != null) {
            loadedPairAssignments(dataLoadProps(tribeId, pageProps))
        } else throw Exception("WHAT")
    }

    private fun dataLoadProps(tribeId: TribeId, pageProps: PageProps) = dataLoadProps(
            query = { TribeDataSetQuery(tribeId).perform() },
            toProps = { _, (tribe, players, history) ->
                PairAssignmentsProps(tribe, players, history.firstOrNull(), pageProps.pathSetter, pageProps.coupling)
            }
    )
}
