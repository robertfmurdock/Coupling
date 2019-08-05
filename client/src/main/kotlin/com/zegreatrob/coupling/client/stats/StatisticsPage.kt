package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.*
import react.RBuilder

object StatisticsPage : ComponentProvider<PageProps>(), StatisticsPageBuilder

val RBuilder.statisticsPage get() = StatisticsPage.captor(this)

private val LoadedPairAssignments = dataLoadWrapper(TribeStatistics)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.captor(this)

interface StatisticsPageBuilder : ComponentBuilder<PageProps>, StatisticsQueryDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.tribeId

        if (tribeId != null) {
            loadedPairAssignments(
                    dataLoadProps(
                            query = { StatisticsQuery(tribeId, pageProps.coupling).perform() },
                            toProps = { _, queryResult -> TribeStatisticsProps(queryResult, pageProps.pathSetter) }
                    )
            )
        } else throw Exception("WHAT")
    }
}
