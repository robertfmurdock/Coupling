package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.pairassignments.TribeDataSetQuery
import com.zegreatrob.coupling.client.pairassignments.TribeDataSetQueryDispatcher
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder
import react.ReactElement

object PrepareSpinPage : RComponent<PageProps>(provider()), PrepareSpinPageBuilder

private val LoadedPairAssignments = dataLoadWrapper(PrepareSpin)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.render(this)

interface PrepareSpinPageBuilder : SimpleComponentRenderer<PageProps>, TribeDataSetQueryDispatcher {

    override fun RContext<PageProps>.render(): ReactElement {
        val tribeId = props.tribeId

        return if (tribeId != null) {
            reactElement {
                loadedPairAssignments(
                    dataLoadProps(
                        query = { TribeDataSetQuery(tribeId).perform() },
                        toProps = { _, (tribe, players, history) ->
                            PrepareSpinProps(tribe, players, history, props.pathSetter)
                        }
                    )
                )
            }
        } else throw Exception("WHAT")
    }
}
