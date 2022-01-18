package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.CardHeader
import com.zegreatrob.coupling.client.Paths.playerConfigPage
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.tmFC

private val styles = useStyles("player/PlayerCard")

data class PlayerCardHeader(
    val tribeId: TribeId,
    val player: Player,
    val linkToConfig: Boolean,
    val size: Int
) : DataProps<PlayerCardHeader> {
    override val component: TMFC<PlayerCardHeader> get() = playerCardHeader
}

private val playerCardHeader = tmFC<PlayerCardHeader> { props ->
    val (tribeId, player, linkToConfig, size) = props
    CardHeader {
        this.size = size
        this.className = styles["header"]
        this.linkUrl = if (linkToConfig) tribeId.with(player).playerConfigPage() else null
        this.headerContent = player.name
    }
}
