package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.CardHeader
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.tmFC

private val styles = useStyles("player/PlayerCard")

data class PlayerCardHeader(val player: Player, val size: Int) : DataPropsBind<PlayerCardHeader>(playerCardHeader)

private val playerCardHeader = tmFC<PlayerCardHeader> { props ->
    val (player, size) = props
    CardHeader {
        this.size = size
        this.className = styles["header"]
        this.headerContent = player.name
    }
}
