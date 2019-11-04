package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.coupling.sdk.SdkSingleton
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder
import react.ReactElement

object NewPairAssignmentsPage : RComponent<PageProps>(provider()), NewPairAssignmentsPageBuilder, Sdk by SdkSingleton

private val LoadedPairAssignments = dataLoadWrapper(PairAssignments)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.render(this)

interface NewPairAssignmentsPageBuilder : SimpleComponentRenderer<PageProps>, NewPairAssignmentsQueryDispatcher {

    override fun RContext<PageProps>.render(): ReactElement {
        val tribeId = props.tribeId

        return if (tribeId != null) {
            val playerIds = props.search.getAll("player").toList()

            reactElement { loadedPairAssignments(dataLoadProps(tribeId, props, playerIds)) }
        } else throw Exception("WHAT")
    }

    private fun dataLoadProps(tribeId: TribeId, pageProps: PageProps, playerIds: List<String>) =
        com.zegreatrob.coupling.client.routing.dataLoadProps(
            query = { NewPairAssignmentsQuery(tribeId, playerIds).perform() },
            toProps = { _, (tribe, players, pairAssignments) ->
                PairAssignmentsProps(tribe!!, players, pairAssignments, pageProps.pathSetter)
            }
        )
}
