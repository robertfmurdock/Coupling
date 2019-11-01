package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.client.tribe.TribeBrowserProps
import com.zegreatrob.coupling.client.tribe.tribeBrowser
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.KtTribe
import react.RProps
import react.ReactElement
import react.dom.div

object RetiredPlayers : RComponent<RetiredPlayersProps>(provider()), RetiredPlayersBuilder

data class RetiredPlayersProps(
    val tribe: KtTribe,
    val retiredPlayers: List<Player>,
    val pathSetter: (String) -> Unit
) : RProps

interface RetiredPlayersCss {
    val className: String
    val header: String
}

interface RetiredPlayersBuilder : StyledComponentRenderer<RetiredPlayersProps, RetiredPlayersCss> {

    override val componentPath: String get() = "player/RetiredPlayers"

    override fun StyledRContext<RetiredPlayersProps, RetiredPlayersCss>.render(): ReactElement {
        val (tribe, players, pathSetter) = props
        return reactElement {
            div(classes = styles.className) {
                tribeBrowser(TribeBrowserProps(tribe, pathSetter))
                div(classes = styles.header) { +"Retired Players" }
                div {
                    players.forEach { player ->
                        playerCard(
                            PlayerCardProps(tribe.id, player, pathSetter, true, deselected = true),
                            key = player.id
                        )
                    }
                }
            }
        }
    }

}
