package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.external.react.ComponentBuilder
import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.buildByPls
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.client.pairassignments.TribeDataSetQuery
import com.zegreatrob.coupling.client.pairassignments.TribeDataSetQueryDispatcher
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder

object PrepareSpinPage : ComponentProvider<PageProps>(), PrepareSpinPageBuilder

private val LoadedPairAssignments = dataLoadWrapper(PrepareSpin)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.captor(this)

interface PrepareSpinPageBuilder : ComponentBuilder<PageProps>, TribeDataSetQueryDispatcher {

    override fun build() = buildByPls {
        val tribeId = props.tribeId

        if (tribeId != null) {
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
