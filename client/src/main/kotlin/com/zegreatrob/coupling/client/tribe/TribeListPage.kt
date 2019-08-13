package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.SimpleComponentBuilder
import com.zegreatrob.coupling.client.external.react.buildBy
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.client.routing.DataLoadProps
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder

object TribeListPage : ComponentProvider<PageProps>(), TribeListPageBuilder

private val LoadedTribeList = dataLoadWrapper(TribeList)
private val RBuilder.loadedTribeList get() = LoadedTribeList.captor(this)

interface TribeListPageBuilder : SimpleComponentBuilder<PageProps>, TribeListQueryDispatcher {

    override fun build() = buildBy {
        reactElement {
            loadedTribeList(
                DataLoadProps {
                    val tribes = performTribeQuery()
                    TribeListProps(
                        tribes = tribes,
                        pathSetter = props.pathSetter
                    )
                }
            )
        }
    }

    private suspend fun performTribeQuery() = TribeListQuery.perform()

}
