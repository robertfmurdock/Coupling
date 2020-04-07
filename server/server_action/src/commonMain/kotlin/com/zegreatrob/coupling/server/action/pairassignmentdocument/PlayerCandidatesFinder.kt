package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.PairingRule

interface PlayerCandidatesFinder {

    fun findCandidates(players: List<Player>, rule: PairingRule, player: Player) = players
        .filterNot { it == player }
        .filterByRule(rule, player.badge)
        .toTypedArray()

    private inline fun List<Player>.filterByRule(rule: PairingRule, badge: Int) = when (rule) {
        PairingRule.PreferDifferentBadge -> filter { otherPlayer -> otherPlayer.badge != badge }
        else -> this
    }

}
