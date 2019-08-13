package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.SimpleComponentBuilder
import com.zegreatrob.coupling.client.external.react.buildBy
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder


object HistoryPage : ComponentProvider<PageProps>(), HistoryPageBuilder

private val LoadedPairAssignments = dataLoadWrapper(History)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.captor(this)

interface HistoryPageBuilder : SimpleComponentBuilder<PageProps>, HistoryQueryDispatcher {

    override fun build() = buildBy {
        val tribeId = props.tribeId

        if (tribeId != null) {
            reactElement {
                loadedPairAssignments(
                    dataLoadProps(
                        query = { HistoryQuery(tribeId).perform() },
                        toProps = { reload, (tribe, history) ->
                            HistoryProps(tribe, history, reload, props.pathSetter)
                        }
                    )
                )
            }
        } else throw Exception("WHAT")
    }
}
