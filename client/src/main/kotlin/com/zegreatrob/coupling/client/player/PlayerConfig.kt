package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.ConfigFrame.configFrame
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pairassignments.NullTraceIdProvider
import com.zegreatrob.coupling.client.player.PlayerConfigEditor.playerConfigEditor
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton
import react.RProps
import react.dom.div

object PlayerConfig : RComponent<PlayerConfigProps>(provider()), PlayerConfigRenderer,
    RepositoryCatalog by SdkSingleton

data class PlayerConfigProps(
    val tribe: Tribe,
    val player: Player,
    val players: List<Player>,
    val pathSetter: (String) -> Unit,
    val reload: () -> Unit
) : RProps

typealias PlayerConfigContext = StyledRContext<PlayerConfigProps, SimpleStyle>

interface PlayerConfigRenderer : StyledComponentRenderer<PlayerConfigProps, SimpleStyle>,
    WindowFunctions, SavePlayerCommandDispatcher, DeletePlayerCommandDispatcher, NullTraceIdProvider {

    override val playerRepository: PlayerRepository

    override val componentPath: String get() = "player/PlayerConfig"

    override fun PlayerConfigContext.render() = reactElement {
        val (tribe, player, players, pathSetter, reload) = props
        configFrame(styles.className) {
            playerConfigEditor(tribe, player, pathSetter, reload)
            div {
                playerRoster(
                    PlayerRosterProps(
                        players = players,
                        tribeId = tribe.id,
                        pathSetter = pathSetter,
                        className = styles["playerRoster"]
                    )
                )
            }
        }
    }

}
