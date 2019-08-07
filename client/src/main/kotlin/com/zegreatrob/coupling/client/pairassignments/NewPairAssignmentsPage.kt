package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.ComponentBuilder
import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.reactFunctionComponent
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import react.RBuilder

object NewPairAssignmentsPage : ComponentProvider<PageProps>(), NewPairAssignmentsPageBuilder

private val LoadedPairAssignments = dataLoadWrapper(PairAssignments)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.captor(this)

interface NewPairAssignmentsPageBuilder : ComponentBuilder<PageProps>, NewPairAssignmentsQueryDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.tribeId

        if (tribeId != null) {
            val playerIds = pageProps.search.getAll("player").toList()

            loadedPairAssignments(dataLoadProps(tribeId, pageProps, playerIds))
        } else throw Exception("WHAT")
    }

    private fun dataLoadProps(tribeId: TribeId, pageProps: PageProps, playerIds: List<String>) =
            com.zegreatrob.coupling.client.routing.dataLoadProps(
                    query = { NewPairAssignmentsQuery(tribeId, playerIds).perform() },
                    toProps = { _, (tribe, players, pairAssignments) ->
                        PairAssignmentsProps(tribe, players, pairAssignments, pageProps.pathSetter)
                    }
            )
}
