package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.buildCommandFunc
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.pairassignments.NullTraceIdProvider
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton
import react.RBuilder
import react.ReactElement

object HistoryPage : RComponent<PageProps>(provider()), HistoryPageBuilder, RepositoryCatalog by SdkSingleton

private val LoadedPairAssignments = dataLoadWrapper(History)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.render(this)

interface HistoryPageBuilder : SimpleComponentRenderer<PageProps>, HistoryQueryDispatcher, NullTraceIdProvider {

    override fun RContext<PageProps>.render(): ReactElement {
        val tribeId = props.tribeId

        return if (tribeId != null) {
            reactElement {
                loadedPairAssignments(
                    dataLoadProps(
                        query = { HistoryQuery(tribeId).perform() },
                        toProps = { reload, scope, (tribe, history) ->
                            HistoryProps(
                                tribe!!,
                                history,
                                reload,
                                props.pathSetter,
                                buildCommandFunc(scope, CommandDispatcher)
                            )
                        }
                    )
                )
            }
        } else throw Exception("WHAT")
    }
}
