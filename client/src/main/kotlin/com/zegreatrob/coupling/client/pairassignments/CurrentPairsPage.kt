package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import react.RBuilder
import react.ReactElement


object CurrentPairsPage : RComponent<PageProps>(provider()), CurrentPairAssignmentsPageBuilder

private val LoadedPairAssignments = dataLoadWrapper(PairAssignments)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.render(this)

interface CurrentPairAssignmentsPageBuilder : SimpleComponentRenderer<PageProps>, TribeDataSetQueryDispatcher {

    override fun RContext<PageProps>.render(): ReactElement {
        val tribeId = props.tribeId

        return if (tribeId != null) {
            reactElement { loadedPairAssignments(dataLoadProps(tribeId, props)) }
        } else throw Exception("WHAT")
    }

    private fun dataLoadProps(tribeId: TribeId, pageProps: PageProps) =
        com.zegreatrob.coupling.client.routing.dataLoadProps(
            query = { TribeDataSetQuery(tribeId).perform() },
            toProps = { _, (tribe, players, history) ->
                PairAssignmentsProps(tribe, players, history.firstOrNull(), pageProps.pathSetter)
            }
        )
}
