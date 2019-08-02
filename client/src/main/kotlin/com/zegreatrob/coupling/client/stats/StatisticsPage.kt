package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.pairassignments.TribeDataSetQuery
import com.zegreatrob.coupling.client.pairassignments.TribeDataSetQueryDispatcher
import react.RBuilder


object StatisticsPage : ComponentProvider<PageProps>(), StatisticsPageBuilder

val RBuilder.statisticsPage get() = StatisticsPage.captor(this)

private val LoadedPairAssignments = dataLoadWrapper(TribeStatistics)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.captor(this)

interface StatisticsPageBuilder : ComponentBuilder<PageProps>, TribeDataSetQueryDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.tribeId

        if (tribeId != null) {
            loadedPairAssignments(
                    dataLoadProps(
                            query = { TribeDataSetQuery(tribeId, pageProps.coupling).perform() },
                            toProps = { _, (tribe, players, history) ->
                                TribeStatisticsProps(tribe, players, history, pageProps.pathSetter)
                            }
                    )
            )
        } else throw Exception("WHAT")
    }
}
