package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.pairassignments.NullTraceIdProvider
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton
import react.RBuilder
import react.ReactElement

object StatisticsPage : RComponent<PageProps>(provider()), StatisticsPageBuilder,
    RepositoryCatalog by SdkSingleton

private val LoadedPairAssignments by lazy { dataLoadWrapper(TribeStatistics) }
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.render(this)

interface StatisticsPageBuilder : SimpleComponentRenderer<PageProps>, StatisticsQueryDispatcher, NullTraceIdProvider {

    override fun RContext<PageProps>.render(): ReactElement {
        val tribeId = props.tribeId

        return if (tribeId != null) {
            reactElement {
                loadedPairAssignments(
                    dataLoadProps(
                        query = { StatisticsQuery(tribeId).perform() },
                        toProps = { _, _, queryResult -> TribeStatisticsProps(queryResult, props.pathSetter) }
                    )
                )
            }
        } else throw Exception("WHAT")

    }
}
