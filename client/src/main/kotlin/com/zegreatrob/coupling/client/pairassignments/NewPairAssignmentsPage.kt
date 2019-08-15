package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import react.RBuilder

object NewPairAssignmentsPage : ComponentProvider<PageProps>(provider()), NewPairAssignmentsPageBuilder

private val LoadedPairAssignments = dataLoadWrapper(PairAssignments)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.captor(this)

interface NewPairAssignmentsPageBuilder : SimpleComponentBuilder<PageProps>, NewPairAssignmentsQueryDispatcher {

    override fun build() = buildBy {
        val tribeId = props.tribeId

        if (tribeId != null) {
            val playerIds = props.search.getAll("player").toList()

            reactElement { loadedPairAssignments(dataLoadProps(tribeId, props, playerIds)) }
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
