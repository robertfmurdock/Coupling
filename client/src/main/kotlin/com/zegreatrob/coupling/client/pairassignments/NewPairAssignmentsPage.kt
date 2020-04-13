package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.coupling.sdk.SdkSingleton
import react.RBuilder
import react.ReactElement

object NewPairAssignmentsPage : RComponent<PageProps>(provider()), NewPairAssignmentsPageBuilder, Sdk by SdkSingleton

private val LoadedPairAssignments = dataLoadWrapper(PairAssignments)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.render(this)

interface NewPairAssignmentsPageBuilder : SimpleComponentRenderer<PageProps>, NewPairAssignmentsQueryDispatcher,
    NullTraceIdProvider {

    override fun RContext<PageProps>.render(): ReactElement {
        val tribeId = props.tribeId

        return if (tribeId != null) {
            val playerIds = props.search.getAll("player").toList()
            val pinIds = props.search.getAll("pin").toList()

            reactElement { loadedPairAssignments(dataLoadProps(tribeId, playerIds, pinIds, props.pathSetter)) }
        } else throw Exception("WHAT")
    }

    private fun dataLoadProps(
        tribeId: TribeId,
        playerIds: List<String>,
        pinIds: List<String>,
        pathSetter: (String) -> Unit
    ) = dataLoadProps(
        query = { NewPairAssignmentsQuery(tribeId, playerIds, pinIds).perform() },
        toProps = { _, (tribe, players, pairAssignments) ->
            PairAssignmentsProps(tribe!!, players, pairAssignments, pathSetter = pathSetter)
        }
    )
}
