package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.sdk.SdkSingleton
import com.zegreatrob.coupling.client.sdk.RepositoryCatalog
import react.RBuilder
import react.ReactElement

object StatisticsPage : RComponent<PageProps>(provider()), StatisticsPageBuilder,
    RepositoryCatalog by SdkSingleton

private val LoadedPairAssignments = dataLoadWrapper(TribeStatistics)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.render(this)

interface StatisticsPageBuilder : SimpleComponentRenderer<PageProps>, StatisticsQueryDispatcher {

    override fun RContext<PageProps>.render(): ReactElement {
        val tribeId = props.tribeId

        return if (tribeId != null) {
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
