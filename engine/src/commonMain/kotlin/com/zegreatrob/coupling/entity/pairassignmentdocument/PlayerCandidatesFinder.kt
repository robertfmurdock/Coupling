package com.zegreatrob.coupling.entity.pairassignmentdocument

import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.PairingRule

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
