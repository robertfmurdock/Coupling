package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.external.react.render
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder

private val LoadedPlayer = dataLoadWrapper(PlayerConfig)
private val RBuilder.loadedPlayer get() = LoadedPlayer.render(this)

val PlayerPage = tribePageFunction { props, tribeId ->
    loadedPlayer(tribeId, props)
}

private fun RBuilder.loadedPlayer(tribeId: TribeId, props: PageProps) = with(props) {
    loadedPlayer(dataLoadProps(
        commander = commander,
        query = TribePlayerQuery(tribeId, playerId),
        toProps = { reload, commandFunc, (tribe, players, player) ->
            PlayerConfigProps(tribe!!, player, players, pathSetter, reload, commandFunc)
        }
    )) { playerId?.let { attrs { key = it } } }
}
