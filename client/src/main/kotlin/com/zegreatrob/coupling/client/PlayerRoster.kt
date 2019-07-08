package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.html.classes
import react.RBuilder
import react.dom.div
import react.key

fun RBuilder.playerRoster(
        label: String?,
        players: List<Player>,
        tribeId: TribeId,
        pathSetter: (String) -> Unit,
        className: String?
) {
    div {
        attrs {
            classes = setOf("react-player-roster", className ?: "")
        }
        div(classes = "roster-header") {
            +(label ?: "Players")
        }

        players.forEach { player ->
            playerCard {
                attrs {
                    this.key = player.id ?: ""
                    this.tribeId = tribeId.value
                    this.player = player
                    this.pathSetter = pathSetter
                    this.disabled = false
                }
            }
        }
    }
}
