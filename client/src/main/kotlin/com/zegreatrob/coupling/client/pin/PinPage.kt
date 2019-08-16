package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.DataLoadProps
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.common.entity.pin.Pin
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import react.RBuilder
import react.ReactElement

object PinListPage : RComponent<PageProps>(provider()), PinListPageBuilder

private val LoadedPinList = dataLoadWrapper(PinList)
private val RBuilder.loadedPinList get() = LoadedPinList.render(this)

interface PinListPageBuilder : SimpleComponentRenderer<PageProps>, PinListQueryDispatcher {

    override fun RContext<PageProps>.render(): ReactElement {
        val tribeId = props.tribeId

        return if (tribeId != null) {
            reactElement {
                loadedPinList(DataLoadProps {
                    tribeId.performPinListQuery()
                        .toPinListProps()
                })
            }
        } else throw Exception("WHAT")
    }

    private fun Pair<KtTribe, List<Pin>>.toPinListProps() = let { (tribe, retiredPlayers) ->
        PinListProps(
            tribe = tribe,
            pins = retiredPlayers
        )
    }

    private suspend fun TribeId.performPinListQuery() = PinListQuery(this)
        .perform()
}
