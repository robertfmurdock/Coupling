package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.StyledComponentBuilder
import com.zegreatrob.coupling.client.external.react.buildBy
import com.zegreatrob.coupling.client.tribe.TribeBrowserProps
import com.zegreatrob.coupling.client.tribe.tribeBrowser
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import react.RProps
import react.dom.div

object RetiredPlayers : ComponentProvider<RetiredPlayersProps>(), RetiredPlayersBuilder

data class RetiredPlayersProps(
        val tribe: KtTribe,
        val retiredPlayers: List<Player>,
        val pathSetter: (String) -> Unit
) : RProps

interface RetiredPlayersCss {
    val className: String
    val header: String
}

interface RetiredPlayersBuilder : StyledComponentBuilder<RetiredPlayersProps, RetiredPlayersCss> {

    override val componentPath: String get() = "player/RetiredPlayers"

    override fun build() = buildBy {
        val (tribe, players, pathSetter) = props
        {
            div(classes = styles.className) {
                tribeBrowser(TribeBrowserProps(tribe, pathSetter))
                div(classes = styles.header) { +"Retired Players" }
                div {
                    players.forEach { player ->
                        playerCard(
                                PlayerCardProps(tribe.id, player, pathSetter, true, className = "disabled"),
                                key = player.id
                        )
                    }
                }
            }
        }
    }

}
