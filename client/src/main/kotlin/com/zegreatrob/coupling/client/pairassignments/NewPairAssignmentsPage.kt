package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import react.RBuilder

object NewPairAssignmentsPage : ComponentProvider<PageProps>(), NewPairAssignmentsPageBuilder

val RBuilder.newPairAssignmentsPage get() = NewPairAssignmentsPage.captor(this)

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
            dataLoadProps(
                    query = { NewPairAssignmentsQuery(tribeId, pageProps.coupling, playerIds).perform() },
                    toProps = { _, (tribe, players, pairAssignments) ->
                        PairAssignmentsProps(tribe, players, pairAssignments, pageProps.pathSetter, pageProps.coupling)
                    }
            )
}
