package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.pairassignments.NullTraceIdProvider
import com.zegreatrob.coupling.client.pairassignments.TribeDataSetQuery
import com.zegreatrob.coupling.client.pairassignments.TribeDataSetQueryDispatcher
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton
import react.RBuilder
import react.ReactElement

object PrepareSpinPage : RComponent<PageProps>(provider()), PrepareSpinPageBuilder,
    RepositoryCatalog by SdkSingleton

private val LoadedPairAssignments = dataLoadWrapper(PrepareSpin)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.render(this)

interface PrepareSpinPageBuilder : SimpleComponentRenderer<PageProps>, TribeDataSetQueryDispatcher,
    NullTraceIdProvider {

    override fun RContext<PageProps>.render(): ReactElement {
        val tribeId = props.tribeId

        return if (tribeId != null) {
            reactElement {
                loadedPairAssignments(
                    dataLoadProps(
                        query = { TribeDataSetQuery(tribeId).perform() },
                        toProps = { _, _, (tribe, players, history, pins) ->
                            PrepareSpinProps(tribe, players, history, pins, props.pathSetter)
                        }
                    )
                )
            }
        } else throw Exception("WHAT")
    }
}
