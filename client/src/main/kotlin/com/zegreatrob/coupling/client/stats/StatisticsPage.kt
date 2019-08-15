package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder

object StatisticsPage : ComponentProvider<PageProps>(provider()), StatisticsPageBuilder

private val LoadedPairAssignments = dataLoadWrapper(TribeStatistics)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.captor(this)

interface StatisticsPageBuilder : SimpleComponentBuilder<PageProps>, StatisticsQueryDispatcher {

    override fun build() = buildBy {
        val tribeId = props.tribeId

        if (tribeId != null) {
            reactElement {
                loadedPairAssignments(
                    dataLoadProps(
                        query = { StatisticsQuery(tribeId).perform() },
                        toProps = { _, queryResult -> TribeStatisticsProps(queryResult, props.pathSetter) }
                    )
                )
            }
        } else throw Exception("WHAT")
    }
}
