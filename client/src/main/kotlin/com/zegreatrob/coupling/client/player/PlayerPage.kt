package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.coupling.model.tribe.TribeId

private val LoadedPlayer = dataLoadWrapper(PlayerConfig)

val PlayerPage = tribePageFunction { props: PageProps, tribeId: TribeId ->
    child(LoadedPlayer, dataLoadProps(
        commander = props.commander,
        query = TribePlayerQuery(tribeId, props.playerId),
        toProps = { reload, commandFunc, (tribe, players, player) ->
            PlayerConfigProps(tribe, player, players, props.pathSetter, reload, commandFunc)
        }
    )) { props.playerId?.let { attrs { key = it } } }

}
