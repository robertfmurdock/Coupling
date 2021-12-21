package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.playerId
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.coupling.model.tribe.TribeId

private val LoadedPlayer = couplingDataLoader<PlayerConfig>()

val PlayerPage = tribePageFunction { props: PageProps, tribeId: TribeId ->
    child(dataLoadProps(
        LoadedPlayer,
        commander = props.commander,
        query = TribePlayerQuery(tribeId, props.playerId),
        toProps = { reload, commandFunc, (tribe, players, player) ->
            PlayerConfig(tribe, player, players, reload, commandFunc)
        }
    ), key = "${tribeId.value}-${props.playerId}")
}
