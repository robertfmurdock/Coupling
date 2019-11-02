package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.sdk.SdkRepositoryCatalog
import com.zegreatrob.coupling.client.sdk.RepositoryCatalog
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder
import react.ReactElement


object CurrentPairsPage : RComponent<PageProps>(provider()), CurrentPairAssignmentsPageBuilder,
    RepositoryCatalog by SdkRepositoryCatalog

private val LoadedPairAssignments = dataLoadWrapper(PairAssignments)

interface CurrentPairAssignmentsPageBuilder : SimpleComponentRenderer<PageProps>, TribeDataSetQueryDispatcher {

    override fun RContext<PageProps>.render(): ReactElement {
        val tribeId = props.tribeId

        return if (tribeId != null) {
            reactElement { pairAssignments(tribeId, props.pathSetter) }
        } else throw Exception("WHAT")
    }

    private fun RBuilder.pairAssignments(tribeId: TribeId, pathSetter: (String) -> Unit) =
        child(LoadedPairAssignments, dataLoadProps(tribeId, pathSetter))

    private fun dataLoadProps(
        tribeId: TribeId,
        pathSetter: (String) -> Unit
    ) =
        com.zegreatrob.coupling.client.routing.dataLoadProps(
            query = { TribeDataSetQuery(tribeId).perform() },
            toProps = { _, (tribe, players, history) ->
                PairAssignmentsProps(tribe!!, players, history.firstOrNull(), pathSetter)
            }
        )
}
