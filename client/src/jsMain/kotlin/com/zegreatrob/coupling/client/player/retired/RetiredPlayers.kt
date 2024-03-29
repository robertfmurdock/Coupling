package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.components.Paths.playerConfigPage
import com.zegreatrob.coupling.client.components.party.PartyBrowser
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.client.components.pngPath
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import kotlinx.css.Color
import react.Props
import react.dom.html.ReactHTML.div
import react.router.dom.Link
import web.cssom.BackgroundRepeat
import web.cssom.Display
import web.cssom.FontSize
import web.cssom.NamedColor
import web.cssom.px
import web.cssom.url

external interface RetiredPlayersProps : Props {
    var party: PartyDetails
    var retiredPlayers: List<Player>
}

@ReactFunc
val RetiredPlayers by nfc<RetiredPlayersProps> { (party, players) ->
    div {
        PartyBrowser(party)
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
                    PlayerCard(player, deselected = true)
                }
            }
        }
    }
}
