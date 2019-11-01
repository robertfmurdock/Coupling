package com.zegreatrob.coupling.server.entity.pairassignmentdocument

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.PairingRule

interface PlayerCandidatesFinder {

    fun findCandidates(players: List<Player>, rule: PairingRule, player: Player) = players
            .filterNot { it == player }
            .filterByRule(rule, player.badge)
            .toTypedArray()

    private fun List<Player>.filterByRule(rule: PairingRule, badge: Int?) = when (rule) {
        PairingRule.PreferDifferentBadge -> filter { otherPlayer -> otherPlayer.badge !== badge }
        else -> this
    }

}
