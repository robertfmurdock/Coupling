package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.SimpleComponentBuilder
import com.zegreatrob.coupling.client.external.react.buildBy
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import react.RBuilder


object CurrentPairAssignmentsPage : ComponentProvider<PageProps>(), CurrentPairAssignmentsPageBuilder

private val LoadedPairAssignments = dataLoadWrapper(PairAssignments)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.captor(this)

interface CurrentPairAssignmentsPageBuilder : SimpleComponentBuilder<PageProps>, TribeDataSetQueryDispatcher {

    override fun build() = buildBy {
        val tribeId = props.tribeId

        if (tribeId != null) {
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
