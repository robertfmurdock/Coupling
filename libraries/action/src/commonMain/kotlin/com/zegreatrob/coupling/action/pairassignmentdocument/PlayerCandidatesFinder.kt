package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import kotools.types.collection.NotEmptyList

interface PlayerCandidatesFinder {

    fun findCandidates(players: NotEmptyList<Player>, rule: PairingRule, player: Player) = players
        .toList()
        .filterNot { it == player }
        .filterByRule(rule, player.badge)

    private fun List<Player>.filterByRule(rule: PairingRule, badge: Badge) = when (rule) {
        PairingRule.PreferDifferentBadge -> filter { otherPlayer -> otherPlayer.badge != badge }
        else -> this
    }
}
