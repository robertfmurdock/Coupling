package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.Paths.playerConfigPage
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.party.PartyBrowser
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.client.pngPath
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.BackgroundRepeat
import csstype.Display
import csstype.FontSize
import csstype.NamedColor
import csstype.px
import csstype.url
import emotion.react.css
import kotlinx.css.Color
import react.dom.html.ReactHTML.div
import react.key
import react.router.dom.Link

data class RetiredPlayers(val party: Party, val retiredPlayers: List<Player>) :
    DataPropsBind<RetiredPlayers>(com.zegreatrob.coupling.client.player.retired.retiredPlayers)

private val styles = useStyles("player/RetiredPlayers")

val retiredPlayers = tmFC<RetiredPlayers> { (party, players) ->
    div {
        className = styles.className
        add(PartyBrowser(party))
        div {
            css {
                display = Display.inlineBlock
                borderRadius = 25.px
                padding = 10.px
                margin = 10.px
                fontSize = FontSize.xxLarge
                backgroundRepeat = BackgroundRepeat.repeatX
                backgroundImage = url(pngPath("overlay"))
                backgroundColor = NamedColor.steelblue
                asDynamic()["text-fill-color"] = Color.white
                asDynamic()["text-stroke-width"] = 1.5.px
                asDynamic()["text-stroke-color"] = NamedColor.darkred
            }
            +"Retired Players"
        }
        div {
            players.forEach { player ->
                Link {
                    to = party.id.with(player).playerConfigPage()
                    draggable = false
                    key = player.id
                    add(PlayerCard(player, deselected = true))
                }
            }
        }
    }
}
